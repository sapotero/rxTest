package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.f2prateek.rx.preferences.Preference;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RLinksEntity;
import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.db.requery.models.RStepEntity;
import sapotero.rxtest.db.requery.models.control_labels.RControlLabelsEntity;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.db.requery.models.exemplars.RExemplarEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Card;
import sapotero.rxtest.retrofit.models.document.ControlLabel;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.document.Exemplar;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.retrofit.models.document.Signer;
import sapotero.rxtest.retrofit.models.document.Step;
import timber.log.Timber;

public class UpdateProcessedDocumentsJob extends BaseJob {

  public static final int PRIORITY = 1;

  private String processed_folder;

  private Preference<String> LOGIN = null;
  private Preference<String> TOKEN = null;
  private Preference<String> HOST;

  private String uid;
  private String TAG = this.getClass().getSimpleName();
  private DocumentInfo document;


  public UpdateProcessedDocumentsJob(String uid, String processed_folder) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uid = uid;
    this.processed_folder = processed_folder;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {

    HOST  = settings.getString("settings_username_host");
    LOGIN = settings.getString("login");
    TOKEN = settings.getString("token");

    Retrofit retrofit = new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl(HOST.get() + "v3/documents/")
      .client(okHttpClient)
      .build();

    DocumentService documentService = retrofit.create( DocumentService.class );

    Observable<DocumentInfo> info = documentService.getInfo(
      uid,
      LOGIN.get(),
      TOKEN.get()
    );

    info
      .subscribeOn( Schedulers.io() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        doc -> {
          document = doc;
          Timber.tag(TAG).d("recv title - %s", doc.getTitle() );
          Timber.tag(TAG).d("actions - %s", new Gson().toJson( doc.getOperations() ) );

          update( exist(doc.getUid()) );

          if ( doc.getLinks() != null && doc.getLinks().size() > 0 ){

            for (String link: doc.getLinks()) {
              jobManager.addJobInBackground( new UpdateLinkJob( link ) );
            }

          }

          if ( doc.getRoute() != null && doc.getRoute().getSteps().size() > 0 ){
            for (Step step: doc.getRoute().getSteps() ) {
              if ( step.getCards() != null && step.getCards().size() > 0){
                for (Card card: step.getCards() ) {
                  if (card.getUid() != null) {
                    jobManager.addJobInBackground( new UpdateLinkJob( card.getUid() ) );
                  }
                }
              }
            }
          }

        },
        error -> {
          error.printStackTrace();
        }

      );
  }



  @NonNull
  private Boolean exist(String uid){

    boolean result = false;

    Integer count = dataStore
      .count(RDocumentEntity.UID)
      .where(RDocumentEntity.UID.eq("p"+uid))
      .get().value();

    if( count != 0 ){
      result = true;
    }

    Timber.tag(TAG).v("exist " + result );

    return result;
  }

  @NonNull
  private Observable<RDocumentEntity> create(DocumentInfo d){


    RDocumentEntity rd = new RDocumentEntity();
    rd.setUid( "p"+ d.getUid() );
    rd.setUser( LOGIN.get() );
    rd.setFilter( "" );
    rd.setMd5( d.getMd5() );
    rd.setSortKey( d.getSortKey() );
    rd.setTitle( d.getTitle() );
    rd.setRegistrationNumber( d.getRegistrationNumber() );
    rd.setRegistrationDate( d.getRegistrationDate() );
    rd.setUrgency( d.getUrgency() );
    rd.setShortDescription( d.getShortDescription() );
    rd.setComment( d.getComment() );
    rd.setExternalDocumentNumber( d.getExternalDocumentNumber() );
    rd.setReceiptDate( d.getReceiptDate() );
    rd.setViewed( d.getViewed() );
    rd.setProcessed(true);

    rd.setFolder(processed_folder);
    rd.setFromProcessedFolder(true);

    if ( d.getSigner().getOrganisation() != null && !Objects.equals(d.getSigner().getOrganisation(), "")){
      rd.setOrganization( d.getSigner().getOrganisation() );
    } else {
      rd.setOrganization("Без организации" );
    }

    RSignerEntity signer = new RSignerEntity();
    signer.setUid( d.getSigner().getId() );
    signer.setName( d.getSigner().getName() );
    signer.setOrganisation( d.getSigner().getOrganisation() );
    signer.setType( d.getSigner().getType() );

    rd.setSigner( signer );

    return dataStore.insert( rd ).toObservable();
  }

  private void update(Boolean exist){

    if (exist) {
      updateDocumentInfo();
    } else {
      create(document)
        .subscribeOn( Schedulers.io() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          this::createNewDocument,
          Throwable::printStackTrace
        );
    }
  }

  private void createNewDocument(RDocumentEntity documentEntity){

      RDocumentEntity rDoc;

      if (documentEntity != null){
        rDoc = documentEntity;
      } else {
        rDoc = dataStore
          .select(RDocumentEntity.class)
          .where(RDocumentEntity.UID.eq( "p"+document.getUid() ))
          .get()
          .first();
      }

      Timber.tag(TAG).v("createNewDocument " + rDoc.getRegistrationNumber() );
      Timber.tag(TAG).v("getImages " + document.getImages().size() );
      Timber.tag(TAG).v("getDecisions " + rDoc.getDecisions().size() );
      Timber.tag(TAG).v("getExemplars " + rDoc.getExemplars().size() );
      Timber.tag(TAG).v("getControlLabels " + rDoc.getControlLabels().size() );

      if (document.getSigner() != null){
        Signer _signer = document.getSigner();
        RSignerEntity signer = new RSignerEntity();
        signer.setUid( _signer.getId() );
        signer.setName( _signer.getName() );
        signer.setOrganisation( _signer.getOrganisation() );
        signer.setType( _signer.getType() );

        rDoc.setSigner( signer );
      }

      rDoc.setFolder(processed_folder);
      rDoc.setFromProcessedFolder(true);
      rDoc.setUser( LOGIN.get() );

      if ( document.getDecisions() != null && document.getDecisions().size() >= 1 ){
        rDoc.getDecisions().clear();
        for (Decision d: document.getDecisions() ) {

          RDecisionEntity decision = new RDecisionEntity();
          decision.setUid( d.getId() );
          decision.setLetterhead(d.getLetterhead());
          decision.setApproved(d.getApproved());
          decision.setSigner(d.getSigner());
          decision.setSignerId(d.getSignerId());
          decision.setAssistantId(d.getAssistantId());
          decision.setSignerBlankText(d.getSignerBlankText());
          decision.setSignerIsManager(d.getSignerIsManager());
          decision.setSignerPositionS(d.getSignerPositionS());
          decision.setComment(d.getComment());
          decision.setDate(d.getDate());
          decision.setUrgencyText(d.getUrgencyText());
          decision.setShowPosition(d.getShowPosition());
          decision.setSignBase64(d.getSignBase64());
          decision.setRed(d.getRed());

          if ( d.getBlocks() != null && d.getBlocks().size() >= 1 ){

            for (Block b: d.getBlocks() ) {
              RBlockEntity block = new RBlockEntity();
              block.setNumber(b.getNumber());
              block.setText(b.getText());
              block.setAppealText(b.getAppealText());
              block.setTextBefore(b.getTextBefore());
              block.setHidePerformers(b.getHidePerformers());
              block.setToCopy(b.getToCopy());
              block.setToFamiliarization(b.getToFamiliarization());

              if ( b.getPerformers() != null && b.getPerformers().size() >= 1 ) {

                for (Performer p : b.getPerformers()) {
                  RPerformerEntity performer = new RPerformerEntity();

                  performer.setNumber(p.getNumber());
                  performer.setPerformerId(p.getPerformerId());
                  performer.setPerformerType(p.getPerformerType());
                  performer.setPerformerText(p.getPerformerText());
                  performer.setPerformerGender(p.getPerformerGender());
                  performer.setOrganizationText(p.getOrganizationText());
                  performer.setIsOriginal(p.getIsOriginal());
                  performer.setIsResponsible(p.getIsResponsible());

                  performer.setBlock(block);
                  block.getPerformers().add(performer);
                }
              }


              block.setDecision(decision);
              decision.getBlocks().add(block);
            }

          }

          //FIX DECISION
          decision.setDocument(rDoc);
          rDoc.getDecisions().add(decision);
        }
      }

      if ( document.getExemplars() != null && document.getExemplars().size() >= 1 ){
        for (Exemplar e: document.getExemplars() ) {
          RExemplarEntity exemplar = new RExemplarEntity();
          exemplar.setNumber(String.valueOf(e.getNumber()));
          exemplar.setIsOriginal(e.getIsOriginal());
          exemplar.setStatusCode(e.getStatusCode());
          exemplar.setAddressedToId(e.getAddressedToId());
          exemplar.setAddressedToName(e.getAddressedToName());
          exemplar.setDate(e.getDate());
          exemplar.setDocument(rDoc);
          rDoc.getExemplars().add(exemplar);
        }
      }

      if ( document.getImages() != null && document.getImages().size() >= 1 ){
        for (Image i: document.getImages() ) {
          RImageEntity image = new RImageEntity();
          image.setTitle(i.getTitle());
          image.setNumber(i.getNumber());
          image.setMd5(i.getMd5());
          image.setSize(i.getSize());
          image.setPath(i.getPath());
          image.setContentType(i.getContentType());
          image.setSigned(i.getSigned());
          image.setDocument(rDoc);
          image.setLoading(false);
          image.setComplete(false);
          image.setError(false);
          rDoc.getImages().add(image);
        }
      }

      if ( document.getControlLabels() != null && document.getControlLabels().size() >= 1 ){
        for (ControlLabel l: document.getControlLabels() ) {
          RControlLabelsEntity label = new RControlLabelsEntity();
          label.setCreatedAt(l.getCreatedAt());
          label.setOfficialId(l.getOfficialId());
          label.setOfficialName(l.getOfficialName());
          label.setSkippedOfficialId(l.getSkippedOfficialId());
          label.setSkippedOfficialName(l.getSkippedOfficialName());
          label.setState(l.getState());
          label.setDocument(rDoc);
          rDoc.getControlLabels().add(label);
        }
      }

      if ( document.getRoute() != null  ){
        RRouteEntity route = new RRouteEntity();
        route.setText( document.getRoute().getTitle() );


        for (Step step: document.getRoute().getSteps() ) {

          RStepEntity r_step = new RStepEntity();
          r_step.setTitle( step.getTitle() );
          r_step.setNumber( step.getNumber() );

          if ( step.getPeople() != null && step.getPeople().size() > 0 ){
            r_step.setPeople(  new Gson().toJson( step.getPeople() )  );
          }
          if ( step.getCards() != null && step.getCards().size() > 0 ){
            r_step.setCards(  new Gson().toJson( step.getCards() )  );
          }
          if ( step.getAnotherApprovals() != null && step.getAnotherApprovals().size() > 0 ){
            r_step.setAnother_approvals(  new Gson().toJson( step.getAnotherApprovals() )  );
          }

          route.getSteps().add( r_step );
        }

        rDoc.setRoute( route );

      }

      if ( document.getLinks() != null){
        for (String _link: document.getLinks()) {

          RLinksEntity link = new RLinksEntity();
          link.setUid(_link);

          rDoc.getLinks().add(link);
        }
      }

      if ( document.getInfoCard() != null){
        rDoc.setInfoCard( document.getInfoCard() );
      }

      rDoc.setFilter( "" );



      dataStore.update(rDoc)
        .toObservable()
        .subscribeOn( Schedulers.io() )
        .observeOn( Schedulers.io() )
        .subscribe(
          result -> {
            Timber.tag(TAG).d("updated " + result.getUid());

            if ( result.getImages() != null && result.getImages().size() > 0 ){

              for (RImage _image : result.getImages()) {

                RImageEntity image = (RImageEntity) _image;
                jobManager.addJobInBackground( new DownloadFileJob(HOST.get(), image.getPath(), image.getMd5()+"_"+image.getTitle(), image.getId() ) );
              }

            }
          },
          error ->{
            error.printStackTrace();
          }
        );
  }

  private void updateDocumentInfo(){


    RDocumentEntity doc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq("p"+uid))
      .get().first();

    if ( !Objects.equals( document.getMd5(), doc.getMd5() ) ){
      Timber.tag("MD5").d("not equal %s - %s",document.getMd5(), doc.getMd5() );

      doc.setMd5( document.getMd5() );

      if (document.getSigner() != null){
        RSignerEntity signer = (RSignerEntity) doc.getSigner();
        signer.setUid( document.getSigner().getId() );
        signer.setName( document.getSigner().getName() );
        signer.setOrganisation( document.getSigner().getOrganisation() );
        signer.setType( document.getSigner().getType() );
      }

      doc.setFromProcessedFolder(true);
      doc.setProcessed(true);
      doc.setFolder(processed_folder);
      doc.setUser( LOGIN.get() );

      if ( document.getDecisions() != null && document.getDecisions().size() >= 1 ){
        doc.getDecisions().clear();
        for (Decision d: document.getDecisions() ) {

          RDecisionEntity decision = new RDecisionEntity();
          decision.setUid( d.getId() );
          decision.setLetterhead(d.getLetterhead());
          decision.setApproved(d.getApproved());
          decision.setSigner(d.getSigner());
          decision.setSignerId(d.getSignerId());
          decision.setAssistantId(d.getAssistantId());
          decision.setSignerBlankText(d.getSignerBlankText());
          decision.setSignerIsManager(d.getSignerIsManager());
          decision.setSignerPositionS(d.getSignerPositionS());
          decision.setComment(d.getComment());
          decision.setDate(d.getDate());
          decision.setUrgencyText(d.getUrgencyText());
          decision.setShowPosition(d.getShowPosition());
          decision.setSignBase64(d.getSignBase64());
          decision.setRed(d.getRed());

          if ( d.getBlocks() != null && d.getBlocks().size() >= 1 ){

            for (Block b: d.getBlocks() ) {
              RBlockEntity block = new RBlockEntity();
              block.setNumber(b.getNumber());
              block.setText(b.getText());
              block.setAppealText(b.getAppealText());
              block.setTextBefore(b.getTextBefore());
              block.setHidePerformers(b.getHidePerformers());
              block.setToCopy(b.getToCopy());
              block.setToFamiliarization(b.getToFamiliarization());

              if ( b.getPerformers() != null && b.getPerformers().size() >= 1 ) {

                for (Performer p : b.getPerformers()) {
                  RPerformerEntity performer = new RPerformerEntity();

                  performer.setNumber(p.getNumber());
                  performer.setPerformerId(p.getPerformerId());
                  performer.setPerformerType(p.getPerformerType());
                  performer.setPerformerText(p.getPerformerText());
                  performer.setPerformerGender(p.getPerformerGender());
                  performer.setOrganizationText(p.getOrganizationText());
                  performer.setIsOriginal(p.getIsOriginal());
                  performer.setIsResponsible(p.getIsResponsible());

                  performer.setBlock(block);
                  block.getPerformers().add(performer);
                }
              }


              block.setDecision(decision);
              decision.getBlocks().add(block);
            }

          }

          //FIX DECISION
          decision.setDocument(doc);
          doc.getDecisions().add(decision);
        }
      }

      if ( document.getRoute() != null  ){
        RRouteEntity route = (RRouteEntity) doc.getRoute();
        route.setText( document.getRoute().getTitle() );


        for (Step step: document.getRoute().getSteps() ) {

          RStepEntity r_step = new RStepEntity();
          r_step.setTitle( step.getTitle() );
          r_step.setNumber( step.getNumber() );

          if ( step.getPeople() != null && step.getPeople().size() > 0 ){
            r_step.setPeople(  new Gson().toJson( step.getPeople() )  );
          }
          if ( step.getCards() != null && step.getCards().size() > 0 ){
            r_step.setCards(  new Gson().toJson( step.getCards() )  );
          }
          if ( step.getAnotherApprovals() != null && step.getAnotherApprovals().size() > 0 ){
            r_step.setAnother_approvals(  new Gson().toJson( step.getAnotherApprovals() )  );
          }

          route.getSteps().add( r_step );
        }

      }

      if ( document.getExemplars() != null && document.getExemplars().size() >= 1 ){
        doc.getExemplars().clear();
        for (Exemplar e: document.getExemplars() ) {
          RExemplarEntity exemplar = new RExemplarEntity();
          exemplar.setNumber(String.valueOf(e.getNumber()));
          exemplar.setIsOriginal(e.getIsOriginal());
          exemplar.setStatusCode(e.getStatusCode());
          exemplar.setAddressedToId(e.getAddressedToId());
          exemplar.setAddressedToName(e.getAddressedToName());
          exemplar.setDate(e.getDate());
          exemplar.setDocument(doc);
          doc.getExemplars().add(exemplar);
        }
      }

      if ( document.getImages() != null && document.getImages().size() >= 1 ){
        doc.getImages().clear();
        for (Image i: document.getImages() ) {
          RImageEntity image = new RImageEntity();
          image.setTitle(i.getTitle());
          image.setNumber(i.getNumber());
          image.setMd5(i.getMd5());
          image.setSize(i.getSize());
          image.setPath(i.getPath());
          image.setContentType(i.getContentType());
          image.setSigned(i.getSigned());
          image.setDocument(doc);
          image.setLoading(false);
          image.setComplete(false);
          image.setError(false);
          doc.getImages().add(image);
        }
      }

      if ( document.getControlLabels() != null && document.getControlLabels().size() >= 1 ){
        doc.getControlLabels().clear();
        for (ControlLabel l: document.getControlLabels() ) {
          RControlLabelsEntity label = new RControlLabelsEntity();
          label.setCreatedAt(l.getCreatedAt());
          label.setOfficialId(l.getOfficialId());
          label.setOfficialName(l.getOfficialName());
          label.setSkippedOfficialId(l.getSkippedOfficialId());
          label.setSkippedOfficialName(l.getSkippedOfficialName());
          label.setState(l.getState());
          label.setDocument(doc);
          doc.getControlLabels().add(label);
        }
      }

      if ( document.getLinks() != null){
        doc.getLinks().clear();
        for (String _link: document.getLinks()) {
          RLinksEntity link = new RLinksEntity();
          link.setUid(_link);
          doc.getLinks().add(link);
        }
      }

      if ( document.getInfoCard() != null){
        doc.setInfoCard( document.getInfoCard() );
      }

      dataStore
        .update( doc )
        .subscribeOn( Schedulers.io() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          result -> {
            Timber.tag("MD5").d("updateDocumentInfo " + result.getMd5());

            if ( result.getImages() != null && result.getImages().size() > 0  ){

              for (RImage _image : result.getImages()) {

                RImageEntity image = (RImageEntity) _image;
                jobManager.addJobInBackground( new DownloadFileJob(HOST.get(), image.getPath(), image.getMd5()+"_"+image.getTitle(), image.getId() ) );
              }

            }
          },
          error ->{
            error.printStackTrace();
          }
        );

      EventBus.getDefault().post( new UpdateCurrentDocumentEvent( "p"+doc.getUid() ) );
    } else {
      Timber.tag("MD5").d("equal");
    }
  }

  @Override
  protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
    return RetryConstraint.createExponentialBackoff(runCount, 1000);
  }
  @Override
  protected void onCancel(@CancelReason int cancelReason, @Nullable Throwable throwable) {
    // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
  }
}

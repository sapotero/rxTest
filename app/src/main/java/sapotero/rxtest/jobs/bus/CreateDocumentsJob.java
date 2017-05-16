package sapotero.rxtest.jobs.bus;

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
import sapotero.rxtest.db.requery.models.RLinks;
import sapotero.rxtest.db.requery.models.RLinksEntity;
import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.db.requery.models.RStep;
import sapotero.rxtest.db.requery.models.RStepEntity;
import sapotero.rxtest.db.requery.models.actions.RActionEntity;
import sapotero.rxtest.db.requery.models.control_labels.RControlLabelsEntity;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.db.requery.models.exemplars.RExemplarEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.adapter.UpdateDocumentAdapterEvent;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.jobs.utils.JobCounter;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Card;
import sapotero.rxtest.retrofit.models.document.ControlLabel;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.document.DocumentInfoAction;
import sapotero.rxtest.retrofit.models.document.Exemplar;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.retrofit.models.document.Step;
import timber.log.Timber;

public class CreateDocumentsJob extends BaseJob {

  public static final int PRIORITY = 1;
  private boolean shared = false;
  private boolean not_processed;
  private String status;
  private String journal;

  private Boolean onControl;
  private Boolean isProcessed = null;
  private Boolean isFavorites = null;

  private Preference<String> LOGIN = null;
  private Preference<String> TOKEN = null;
  private Preference<String> HOST;

  private Fields.Status filter;
  private String uid;
  private String TAG = this.getClass().getSimpleName();
  private DocumentInfo document;

  private int jobCount;

  public CreateDocumentsJob(String uid, String journal, String status, boolean shared) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uid = uid;
    this.shared = shared;

    if (journal != null) {
      String[] index = journal.split("_production_db_");
      this.journal = index[0];
    }

    this.status  = status;
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
          create( doc );

          EventBus.getDefault().post( new StepperLoadDocumentEvent(doc.getUid()) );
        },
        error -> {
          Timber.tag(TAG).e(error);
          EventBus.getDefault().post( new StepperLoadDocumentEvent("Error downloading document info on create") );
        }

      );
  }

  private void create(DocumentInfo document){


    RDocumentEntity doc = new RDocumentEntity();
    doc.setFromProcessedFolder( false );
    doc.setFromFavoritesFolder( false );
    doc.setUid( document.getUid() );
    doc.setFromLinks( false );
    doc.setUser( LOGIN.get() );

    doc.setMd5( document.getMd5() );
    doc.setSortKey( document.getSortKey() );
    doc.setTitle( document.getTitle() );
    doc.setRegistrationNumber( document.getRegistrationNumber() );
    doc.setRegistrationDate( document.getRegistrationDate() );
    doc.setUrgency( document.getUrgency() );
    doc.setShortDescription( document.getShortDescription() );
    doc.setComment( document.getComment() );
    doc.setExternalDocumentNumber( document.getExternalDocumentNumber() );
    doc.setReceiptDate( document.getReceiptDate() );
    doc.setViewed( document.getViewed() );



    Timber.tag(TAG).d("signer %s", new Gson().toJson( document.getSigner() ) );

    doc.setMd5( document.getMd5() );

    if (document.getSigner() != null){
      RSignerEntity signer = new RSignerEntity();
      signer.setUid( document.getSigner().getId() );
      signer.setName( document.getSigner().getName() );
      signer.setOrganisation( document.getSigner().getOrganisation() );
      signer.setType( document.getSigner().getType() );

      doc.setSigner(signer);
    }

    if ( document.getSigner().getOrganisation() != null && !Objects.equals(document.getSigner().getOrganisation(), "")){
      doc.setOrganization( document.getSigner().getOrganisation() );
    } else {
      doc.setOrganization("Без организации" );
    }

    doc.setUser( LOGIN.get() );
    doc.setFavorites(false);
    doc.setProcessed(false);
    doc.setControl(false);
    doc.setFromLinks( false );
    doc.setFromProcessedFolder( false );
    doc.setFromFavoritesFolder( false );
    doc.setChanged( false );


    Boolean red = false;
    Boolean with_decision = false;

    if ( document.getDecisions() != null && document.getDecisions().size() >= 1 ){
      with_decision = true;

      for (Decision d: document.getDecisions() ) {
        RDecisionEntity decision = new RDecisionEntity();
        decision.setUid( d.getId() );
        decision.setLetterhead(d.getLetterhead());
        decision.setApproved(d.getApproved());
        decision.setTemporary(false);
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
        decision.setLetterheadFontSize(d.getLetterhead());
        decision.setPerformerFontSize(d.getPerformersFontSize());

        if (d.getRed()){
          red= true;
        }

        if ( d.getBlocks() != null && d.getBlocks().size() >= 1 ){

          for (Block b: d.getBlocks() ) {
            RBlockEntity block = new RBlockEntity();
            block.setNumber(b.getNumber());
            block.setText(b.getText());
            block.setFontSize(b.getFontSize());
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
                performer.setIsOrganization(p.getOrganization());

                performer.setBlock(block);
                block.getPerformers().add(performer);
              }
            }


            block.setDecision(decision);
            decision.getBlocks().add(block);
          }

        }

        decision.setDocument(doc);
        doc.getDecisions().add(decision);
      }
    }

    doc.setWithDecision(with_decision);
    doc.setRed(red);

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
      for (Image i: document.getImages() ) {
        RImageEntity image = new RImageEntity();
        image.setTitle(i.getTitle());
        image.setNumber(i.getNumber());
        image.setMd5(i.getMd5());
        image.setSize(i.getSize());
        image.setPath(i.getPath());
        image.setContentType(i.getContentType());
        image.setSigned(i.getSigned());
        image.setCreatedAt(i.getCreatedAt());
        image.setImageId( i.getPath().substring(11, 35) );
        image.setDocument(doc);
        image.setLoading(false);
        image.setComplete(false);
        image.setError(false);
        doc.getImages().add(image);
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
        label.setDocument(doc);
        doc.getControlLabels().add(label);
      }
    }

    if ( document.getActions() != null && document.getActions().size() > 0 ){
      for (DocumentInfoAction act: document.getActions() ) {
        RActionEntity action = new RActionEntity();

        action.setOfficialId(act.getOfficialId());
        action.setAddressedToId(act.getAddressedToId());
        action.setAction(act.getAction());
        action.setActionDescription(act.getActionDescription());
        action.setUpdatedAt(act.getUpdatedAt());
        action.setToS(act.getToS());

        action.setDocument(doc);
        doc.getActions().add(action);
      }
    }

    if ( document.getLinks() != null){
      for (String _link: document.getLinks()) {
        RLinksEntity link = new RLinksEntity();
        link.setUid(_link);
        doc.getLinks().add(link);
      }
    }

    if ( document.getInfoCard() != null){
      doc.setInfoCard( document.getInfoCard() );
    }


    doc.setFilter(status);
    doc.setDocumentType(journal);

    if (shared || Objects.equals(doc.getAddressedToType(), "group")) {
      doc.setAddressedToType("group");
    } else {
      doc.setAddressedToType("");
    }

    dataStore
      .insert( doc )
      .toObservable()
      .subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        result -> {
          EventBus.getDefault().post( new UpdateCurrentDocumentEvent( doc.getUid() ) );

          jobCount = 0;

          if ( result.getImages() != null && result.getImages().size() > 0 ){
            for (RImage _image : result.getImages()) {
              jobCount++;
              RImageEntity image = (RImageEntity) _image;
              jobManager.addJobInBackground( new DownloadFileJob(HOST.get(), image.getPath(), image.getMd5()+"_"+image.getTitle(), image.getId() ) );
            }
          }

          if ( doc.getLinks() != null && doc.getLinks().size() > 0 ){
            for (RLinks _link: doc.getLinks()) {
              jobCount++;
              RLinksEntity link = (RLinksEntity) _link;
              jobManager.addJobInBackground( new UpdateLinkJob( link.getUid() ) );
            }
          }

          if ( doc.getRoute() != null ){
            for (RStep _step: ((RRouteEntity) doc.getRoute()).getSteps() ) {
              RStepEntity step = (RStepEntity) _step;

              if ( step.getCards() != null ){
                Card[] cards = new Gson().fromJson(step.getCards(), Card[].class);

                if (cards.length > 0){
                  for (Card card: cards ) {
                    if (card.getUid() != null) {
                      jobCount++;
                      jobManager.addJobInBackground( new UpdateLinkJob( card.getUid() ) );
                    }
                  }
                }
              }
            }
          }

          JobCounter jobCounter = new JobCounter(settings);
          jobCounter.addJobCount(jobCount);

          EventBus.getDefault().post( new UpdateDocumentAdapterEvent( result.getUid(), result.getDocumentType(), result.getFilter() ) );


        },
        error -> {
          Timber.tag(TAG).e(error);
        }

      );
  }


  @Override
  protected RetryConstraint shouldReRunOnThrowable(Throwable throwable, int runCount, int maxRunCount) {
    return RetryConstraint.createExponentialBackoff(runCount, 1000);
  }
  @Override
  protected void onCancel(@CancelReason int cancelReason, @Nullable Throwable throwable) {
    // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
    EventBus.getDefault().post( new StepperLoadDocumentEvent("Error creating document (job cancelled)") );
  }
}

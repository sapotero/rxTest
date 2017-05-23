package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.mapper.ActionMapper;
import sapotero.rxtest.db.mapper.ControlLabelMapper;
import sapotero.rxtest.db.mapper.DecisionMapper;
import sapotero.rxtest.db.mapper.DocumentMapper;
import sapotero.rxtest.db.mapper.ExemplarMapper;
import sapotero.rxtest.db.mapper.ImageMapper;
import sapotero.rxtest.db.mapper.SignerMapper;
import sapotero.rxtest.db.mapper.StepMapper;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RLinksEntity;
import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.db.requery.models.RStepEntity;
import sapotero.rxtest.db.requery.models.actions.RActionEntity;
import sapotero.rxtest.db.requery.models.control_labels.RControlLabelsEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.exemplars.RExemplarEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Card;
import sapotero.rxtest.retrofit.models.document.ControlLabel;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.document.DocumentInfoAction;
import sapotero.rxtest.retrofit.models.document.Exemplar;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.retrofit.models.document.Signer;
import sapotero.rxtest.retrofit.models.document.Step;
import timber.log.Timber;

public class UpdateProcessedDocumentsJob extends BaseJob {

  public static final int PRIORITY = 1;

  private String processed_folder;

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

    Retrofit retrofit = new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl(settings.getHost() + "v3/documents/")
      .client(okHttpClient)
      .build();

    DocumentService documentService = retrofit.create( DocumentService.class );

    Observable<DocumentInfo> info = documentService.getInfo(
      uid,
      settings.getLogin(),
      settings.getToken()
    );

    info
      .subscribeOn( Schedulers.io() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        doc -> {
          document = doc;
          Timber.tag(TAG).d("recv title - %s", doc.getTitle() );

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
      .where(RDocumentEntity.UID.eq(uid))
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
    DocumentMapper documentMapper = new DocumentMapper();

    documentMapper.simpleFieldsToEntity(rd, d);
    documentMapper.setFieldsInEntity(rd, false, "", "", false);
    rd.setFolder(processed_folder);
    rd.setProcessed(true);
    rd.setFromProcessedFolder(true);

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
          .where(RDocumentEntity.UID.eq( document.getUid() ))
          .get()
          .first();
      }

      if (document.getSigner() != null){
        Signer _signer = document.getSigner();
        RSignerEntity signer = new SignerMapper().toEntity(_signer);
        rDoc.setSigner( signer );
      }

      rDoc.setFolder(processed_folder);
      rDoc.setFromProcessedFolder(true);
      rDoc.setProcessed(true);

      rDoc.setUser( settings.getLogin() );

      if ( document.getDecisions() != null && document.getDecisions().size() >= 1 ){
        rDoc.getDecisions().clear();
        for (Decision d: document.getDecisions() ) {
          RDecisionEntity decision = new DecisionMapper().toEntity(d);
          decision.setDocument(rDoc);
          rDoc.getDecisions().add(decision);
        }
      }

      if ( document.getExemplars() != null && document.getExemplars().size() >= 1 ){
        ExemplarMapper exemplarMapper = new ExemplarMapper();

        for (Exemplar e: document.getExemplars() ) {
          RExemplarEntity exemplar = exemplarMapper.toEntity(e);
          exemplar.setDocument(rDoc);
          rDoc.getExemplars().add(exemplar);
        }
      }

      if ( document.getImages() != null && document.getImages().size() >= 1 ){
        ImageMapper imageMapper = new ImageMapper();

        for (Image i: document.getImages() ) {
          RImageEntity image = imageMapper.toEntity(i);
          image.setDocument(rDoc);
          rDoc.getImages().add(image);
        }
      }

      if ( document.getControlLabels() != null && document.getControlLabels().size() >= 1 ){
        ControlLabelMapper controlLabelMapper = new ControlLabelMapper();

        for (ControlLabel l: document.getControlLabels() ) {
          RControlLabelsEntity label = controlLabelMapper.toEntity(l);
          label.setDocument(rDoc);
          rDoc.getControlLabels().add(label);
        }
      }

      if ( document.getRoute() != null  ){
        RRouteEntity route = new RRouteEntity();
        route.setText( document.getRoute().getTitle() );

        StepMapper stepMapper = new StepMapper();

        for (Step step: document.getRoute().getSteps() ) {
          RStepEntity r_step = stepMapper.toEntity(step);
          r_step.setRoute(route);
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
      rDoc.setFolder(processed_folder);
      rDoc.setFromProcessedFolder(true);
      rDoc.setProcessed(true);



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
                jobManager.addJobInBackground( new DownloadFileJob(settings.getHost(), image.getPath(), image.getMd5()+"_"+image.getTitle(), image.getId() ) );
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
      .where(RDocumentEntity.UID.eq(uid))
      .get().first();

    if ( !Objects.equals( document.getMd5(), doc.getMd5() ) ){
      Timber.tag("MD5").d("not equal %s - %s",document.getMd5(), doc.getMd5() );

      doc.setMd5( document.getMd5() );

      if (document.getSigner() != null){
        RSignerEntity signer = (RSignerEntity) doc.getSigner();
        new SignerMapper().updateEntity(signer, document.getSigner());
      }

      doc.setUser( settings.getLogin() );

      if ( document.getDecisions() != null && document.getDecisions().size() >= 1 ){
        doc.getDecisions().clear();
        for (Decision d: document.getDecisions() ) {
          RDecisionEntity decision = new DecisionMapper().toEntity(d);
          decision.setDocument(doc);
          doc.getDecisions().add(decision);
        }
      }

      if ( document.getRoute() != null  ){
        RRouteEntity route = (RRouteEntity) doc.getRoute();
        route.setText( document.getRoute().getTitle() );

        StepMapper stepMapper = new StepMapper();

        for (Step step: document.getRoute().getSteps() ) {
          RStepEntity r_step = stepMapper.toEntity(step);
          r_step.setRoute(route);
          route.getSteps().add( r_step );
        }
      }

      if ( document.getExemplars() != null && document.getExemplars().size() >= 1 ){
        doc.getExemplars().clear();
        ExemplarMapper exemplarMapper = new ExemplarMapper();

        for (Exemplar e: document.getExemplars() ) {
          RExemplarEntity exemplar = exemplarMapper.toEntity(e);
          exemplar.setDocument(doc);
          doc.getExemplars().add(exemplar);
        }
      }

      if ( document.getImages() != null && document.getImages().size() >= 1 ){
        doc.getImages().clear();
        ImageMapper imageMapper = new ImageMapper();

        for (Image i: document.getImages() ) {
          RImageEntity image = imageMapper.toEntity(i);
          image.setDocument(doc);
          doc.getImages().add(image);
        }
      }

      if ( document.getControlLabels() != null && document.getControlLabels().size() >= 1 ){
        doc.getControlLabels().clear();
        ControlLabelMapper controlLabelMapper = new ControlLabelMapper();

        for (ControlLabel l: document.getControlLabels() ) {
          RControlLabelsEntity label = controlLabelMapper.toEntity(l);
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

      if ( document.getActions() != null && document.getActions().size() >= 1 ){
        doc.getActions().clear();
        ActionMapper actionMapper = new ActionMapper();

        for (DocumentInfoAction act: document.getActions() ) {
          RActionEntity action = actionMapper.toEntity(act);
          action.setDocument(doc);
          doc.getActions().add(action);
        }
      }

      doc.setFromProcessedFolder(true);
      doc.setProcessed(true);
      doc.setFolder(processed_folder);

      dataStore
        .update( doc )
        .subscribeOn( Schedulers.io() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          result -> {
            Timber.tag("MD5").d("updateDocumentInfo " + result.getMd5());

            EventBus.getDefault().post( new StepperLoadDocumentEvent(doc.getUid()) );

            if ( result.getImages() != null && result.getImages().size() > 0  ){

              for (RImage _image : result.getImages()) {

                RImageEntity image = (RImageEntity) _image;
                jobManager.addJobInBackground( new DownloadFileJob(settings.getHost(), image.getPath(), image.getMd5()+"_"+image.getTitle(), image.getId() ) );
              }

            }
          },
          error ->{
            error.printStackTrace();
          }
        );

      EventBus.getDefault().post( new UpdateCurrentDocumentEvent( doc.getUid() ) );
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

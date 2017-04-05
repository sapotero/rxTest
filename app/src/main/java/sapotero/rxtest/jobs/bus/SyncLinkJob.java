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
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import timber.log.Timber;

public class SyncLinkJob extends BaseJob {

  public static final int PRIORITY = 1;

  private Preference<String> LOGIN = null;
  private Preference<String> TOKEN = null;
  private Preference<String> HOST;

  private Fields.Status filter;
  private String uid;
  private String TAG = this.getClass().getSimpleName();

  public SyncLinkJob(String uid) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uid = uid;
    this.filter = Fields.Status.LINK;

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
          Timber.tag(TAG).d("recv title - %s", doc.getTitle() );
          Timber.tag(TAG).d("actions - %s", new Gson().toJson( doc.getOperations() ) );
          update( doc, exist(doc.getUid()) );

          EventBus.getDefault().post( new StepperLoadDocumentEvent(doc.getUid()) );


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
    rd.setUid( d.getUid() );
    rd.setUser( LOGIN.get() );
    rd.setFilter( filter.toString() );
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

    rd.setFavorites(true);
    rd.setProcessed(true);
    rd.setFolder("");
    rd.setControl(false);
    rd.setFromLinks(true);

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

  private void update(DocumentInfo document, Boolean exist){


    Timber.tag(TAG).d("create title - %s | %s", document.getTitle(), filter.toString() );

    if (!exist){
      create(document)
        .subscribeOn( Schedulers.io() )
        .observeOn( Schedulers.io() )
        .subscribe(data -> {
          Timber.tag(TAG).v("addByOne " + data.getTitle() );
          setData( data, document, false);


        },
        error -> {
          error.printStackTrace();
        });
    }
    else {
      setData(null, document, true);
    }
  }

  private void setData(RDocumentEntity documentEntity, DocumentInfo document, boolean md5Equal){

    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.FROM_LINKS, true)
      .where(RDecisionEntity.UID.eq(document.getUid()))
      .get().value();
//      .toObservable()
//      .subscribeOn( Schedulers.io() )
//      .observeOn( Schedulers.io() )
//      .subscribe(
//        result -> {
//          Timber.tag(TAG).d("updated %s",result.getUid());
//
//          if ( result.getImages() != null && result.getImages().size() > 0  ){
//
//            for (RImage _image : result.getImages()) {
//
//              RImageEntity image = (RImageEntity) _image;
//              jobManager.addJobInBackground( new DownloadFileJob(HOST.get(), image.getPath(), image.getMd5()+"_"+image.getTitle(), image.getId() ) );
//            }
//
//          }
//        },
//        error ->{
//          Timber.tag(TAG).d("error %s ", error);
//        }
//      );
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

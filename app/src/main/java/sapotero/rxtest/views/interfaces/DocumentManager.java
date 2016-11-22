package sapotero.rxtest.views.interfaces;

import android.content.Context;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import timber.log.Timber;

public class DocumentManager {

  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject JobManager jobManager;
  @Inject SingleEntityStore<Persistable> dataStore;

  private Preference<String> UID;

  private CompositeSubscription subscription;
  private final Context context;

  private final String TAG = this.getClass().getSimpleName();

  private RDocumentEntity document;

  private String state;
  private String type;

  Callback callback;
  private DocumentType document_type;

  public interface Callback {
    void onGetStateSuccess();
    void onGetStateError();
  }

  public DocumentManager(Context context) {
    this.context = context;
    EsdApplication.getComponent(context).inject(this);
    document_type = new DocumentType(context);
    initialize();
  }

  public void registerCallBack(DocumentManager.Callback callback){
    this.callback = callback;
    getCurrentState();
  }

  private void initialize() {
    UID = settings.getString("info.uid");
  }

  private void unsubscribe(){
    if ( subscription != null && subscription.hasSubscriptions() ){
      subscription.unsubscribe();
    }
    subscription = new CompositeSubscription();
  }

  private void getCurrentState() {

    if (UID.get() == null) {
      callback.onGetStateError();
    }

    findDocument();

    if (document != null) {

      String journal_type = document_type.getByUID( document.getUid() );
      Timber.e( "JOURNAL_TYPE: %s" , journal_type);

      setState(document.getFilter());
      setType( journal_type );
    }

    callback.onGetStateSuccess();
  }

  private void findDocument() {
    this.document = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq( UID.get() ))
      .get()
      .toObservable()
      .toBlocking().first();
  }

  public String getCurrentDocumentNumber(){
    return UID.get();
  }

  private void setType(String type) {
    this.type = type;
  }
  public String getType(){
    return type;
  }

  private void setState(String state) {
    this.state = state;
  }
  public String getState(){
    return state;
  }


}

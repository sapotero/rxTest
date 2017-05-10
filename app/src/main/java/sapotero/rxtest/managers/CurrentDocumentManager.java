package sapotero.rxtest.managers;

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
import sapotero.rxtest.db.requery.utils.Fields;
import timber.log.Timber;

public class CurrentDocumentManager {

  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  private Preference<String> UID;

  private CompositeSubscription subscription;
  private final Context context;

  private final String TAG = this.getClass().getSimpleName();

  private RDocumentEntity document;

  private String state;
  private String type;

//  private DocumentType document_type;
  Callback callback;

  public interface Callback {
    void onGetStateSuccess();
    void onGetStateError();
  }

  public void registerCallBack(CurrentDocumentManager.Callback callback){
    this.callback = callback;
    getCurrentState();
  }

  public CurrentDocumentManager(Context context) {
    this.context = context;
    EsdApplication.getDataComponent().inject(this);
    initialize();
  }

  private void initialize() {
    UID = settings.getString("activity_main_menu.uid");
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

      Fields.Journal journal = Fields.getJournalByUid( document.getUid() );
      Timber.tag(TAG).e( "JOURNAL_TYPE: %s" , journal.getType() );

      setState(document.getFilter());
      setType( String.valueOf(journal.getType()) );
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

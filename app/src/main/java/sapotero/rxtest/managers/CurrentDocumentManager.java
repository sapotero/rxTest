package sapotero.rxtest.managers;

import android.content.Context;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.utils.ISettings;
import timber.log.Timber;

public class CurrentDocumentManager {

  @Inject ISettings settings;
  @Inject SingleEntityStore<Persistable> dataStore;

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
  }

  private void unsubscribe(){
    if ( subscription != null && subscription.hasSubscriptions() ){
      subscription.unsubscribe();
    }
    subscription = new CompositeSubscription();
  }

  private void getCurrentState() {

    if ( settings.getUid().equals("") ) {
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
      .where(RDocumentEntity.UID.eq( settings.getUid() ))
      .get()
      .toObservable()
      .toBlocking().first();
  }

  public String getCurrentDocumentNumber(){
    return settings.getUid();
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

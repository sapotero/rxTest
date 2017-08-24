package sapotero.rxtest.db.requery.utils;

import java.util.List;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.utils.RStateEntity;
import timber.log.Timber;


// Сохраняет и восстанавливает состояние документа при входе и выходе из режима замещения
public class DocumentStateSaver {

  @Inject SingleEntityStore<Persistable> dataStore;

  public DocumentStateSaver() {
    EsdApplication.getDataComponent().inject(this);
  }

  public void saveDocumentState(RDocumentEntity doc, String currentLogin, String TAG) {
    createUpdateDocumentStateForLogin( doc, doc.getUser(), TAG );
    dropDocumentState( doc );
    createUpdateDocumentStateForLogin( doc, currentLogin, TAG );
  }

  private void createUpdateDocumentStateForLogin(RDocumentEntity doc, String login, String TAG) {
    RStateEntity stateEntity = dataStore
      .select( RStateEntity.class )
      .where( RStateEntity.UID.eq( doc.getUid() ) )
      .and( RStateEntity.USER.eq( login ) )
      .get().firstOrNull();

    if ( stateEntity != null ) {
      saveFields( stateEntity, doc );
      dataStore
        .update( stateEntity )
        .toObservable()
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          result -> Timber.tag(TAG).d("Updated document state in RStateEntity table"),
          error -> Timber.tag(TAG).e(error)
        );

    } else {
      stateEntity = new RStateEntity();
      stateEntity.setUid( doc.getUid() );
      stateEntity.setUser( login );
      saveFields( stateEntity, doc );
      dataStore
        .insert( stateEntity )
        .toObservable()
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          result -> Timber.tag(TAG).d("Added document state to RStateEntity table"),
          error -> Timber.tag(TAG).e(error)
        );
    }
  }

  private void dropDocumentState(RDocumentEntity doc) {
    dataStore
      .update(RDocumentEntity.class)
      .set( RDocumentEntity.FILTER, "")
      .set( RDocumentEntity.DOCUMENT_TYPE, "")
      .set( RDocumentEntity.CONTROL, false)
      .set( RDocumentEntity.FAVORITES, false)
      .set( RDocumentEntity.PROCESSED, false)
      .set( RDocumentEntity.FROM_FAVORITES_FOLDER, false)
      .set( RDocumentEntity.FROM_PROCESSED_FOLDER, false)
      .set( RDocumentEntity.FROM_LINKS, false)
      .set( RDocumentEntity.REJECTED, false )
      .set( RDocumentEntity.RETURNED, false )
      .set( RDocumentEntity.AGAIN, false )
      .where(RDocumentEntity.UID.eq( doc.getUid() ))
      .get()
      .value();
  }

  private void saveFields(RStateEntity stateEntity, RDocumentEntity doc) {
    stateEntity.setFilter( doc.getFilter() );
    stateEntity.setDocumentType( doc.getDocumentType() );
    stateEntity.setControl( doc.isControl() );
    stateEntity.setFavorites( doc.isFavorites() );
    stateEntity.setProcessed( doc.isProcessed() );
    stateEntity.setFromFavoritesFolder( doc.isFromFavoritesFolder() );
    stateEntity.setFromProcessedFolder( doc.isFromProcessedFolder() );
    stateEntity.setFromLinks( doc.isFromLinks() );
    stateEntity.setReturned( doc.isReturned() );
    stateEntity.setRejected( doc.isRejected() );
    stateEntity.setAgain( doc.isAgain() );
  }

  public void saveRestoreDocumentStates(String currentLogin, String previousLogin, String TAG) {
    saveDocumentStates( previousLogin, TAG );
    restoreDocumentStates( currentLogin, TAG );
  }

  private void saveDocumentStates(String login, String TAG) {
    List<RStateEntity> stateEntityList = getSavedStates( login );

    for ( RStateEntity stateEntity : stateEntityList ) {
      RDocumentEntity doc = getDocumentEntity( stateEntity.getUid() );

      if ( doc != null ) {
        createUpdateDocumentStateForLogin( doc, login, TAG );
      }
    }
  }

  private List<RStateEntity> getSavedStates(String login) {
    return dataStore
      .select( RStateEntity.class )
      .where( RStateEntity.USER.eq( login ) )
      .get().toList();
  }

  private RDocumentEntity getDocumentEntity(String uid) {
    return dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq( uid ))
      .get().firstOrNull();
  }

  private void restoreDocumentStates(String login, String TAG) {
    List<RStateEntity> stateEntityList = getSavedStates( login );

    for ( RStateEntity stateEntity : stateEntityList ) {
      restoreDocumentState( stateEntity, login, TAG );
    }
  }

  private void restoreDocumentState(RStateEntity stateEntity, String login, String TAG) {
    RDocumentEntity doc = getDocumentEntity( stateEntity.getUid() );

    if ( doc != null ) {
      restoreFields( stateEntity, doc, login );
      dataStore
        .update( doc )
        .toObservable()
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          result -> Timber.tag(TAG).d("Restored document state from RStateEntity table"),
          error -> Timber.tag(TAG).e(error)
        );
    }
  }

  private void restoreFields(RStateEntity stateEntity, RDocumentEntity doc, String login) {
    doc.setUser( login );
    doc.setFilter( stateEntity.getFilter() );
    doc.setDocumentType( stateEntity.getDocumentType() );
    doc.setControl( stateEntity.isControl() );
    doc.setFavorites( stateEntity.isFavorites() );
    doc.setProcessed( stateEntity.isProcessed() );
    doc.setFromFavoritesFolder( stateEntity.isFromFavoritesFolder() );
    doc.setFromProcessedFolder( stateEntity.isFromProcessedFolder() );
    doc.setFromLinks( stateEntity.isFromLinks() );
    doc.setReturned( stateEntity.isReturned() );
    doc.setRejected( stateEntity.isRejected() );
    doc.setAgain( stateEntity.isAgain() );
  }
}

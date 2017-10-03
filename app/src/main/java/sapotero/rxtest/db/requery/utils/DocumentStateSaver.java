package sapotero.rxtest.db.requery.utils;

import java.util.List;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.utils.RStateEntity;
import timber.log.Timber;

// resolved https://tasks.n-core.ru/browse/MVDESD-12618
// Режим замещения

// Сохраняет и восстанавливает состояние документа при входе и выходе из режима замещения
public class DocumentStateSaver {

  @Inject SingleEntityStore<Persistable> dataStore;

  public DocumentStateSaver() {
    EsdApplication.getDataComponent().inject(this);
  }

  // Если документа нет в памяти, но он есть в базе и пользователь не равен текущему, то:
  // 1. Сохраняем состояние документа с тем логином, который в документе
  // 2. Очищаем состояние документа
  // 3. Сохраняем состояние документа с текущим логином
  // (нужно, чтобы для этого документа было сохранено состояние при обратном переключении режима)
  public void saveDocumentState(RDocumentEntity doc, String currentLogin, String TAG) {
    createUpdateDocumentStateForLogin( doc, doc.getUser(), TAG );
    dropDocumentState( doc, TAG );
    RDocumentEntity doc2 = getDocumentEntity( doc.getUid() ); // берем из базы обновленный документ после очищения
    if ( doc2 != null ) {
      createUpdateDocumentStateForLogin( doc2, currentLogin, TAG );
    }
  }

  private void createUpdateDocumentStateForLogin(RDocumentEntity doc, String login, String TAG) {
    RStateEntity stateEntity = dataStore
      .select( RStateEntity.class )
      .where( RStateEntity.UID.eq( doc.getUid() ) )
      .and( RStateEntity.USER.eq( login ) )
      .get().firstOrNull();

    if ( stateEntity != null ) {
      dataStore
        .update( RStateEntity.class )
        .set( RStateEntity.FILTER, doc.getFilter() )
        .set( RStateEntity.DOCUMENT_TYPE, doc.getDocumentType() )
        .set( RStateEntity.CONTROL, doc.isControl() )
        .set( RStateEntity.FAVORITES, doc.isFavorites() )
        .set( RStateEntity.PROCESSED, doc.isProcessed() )
        .set( RStateEntity.FROM_FAVORITES_FOLDER, doc.isFromFavoritesFolder() )
        .set( RStateEntity.FROM_PROCESSED_FOLDER, doc.isFromProcessedFolder() )
        .set( RStateEntity.FROM_LINKS, doc.isFromLinks() )
        .set( RStateEntity.RETURNED, doc.isReturned() )
        .set( RStateEntity.REJECTED, doc.isRejected() )
        .set( RStateEntity.AGAIN, doc.isAgain() )
        .set( RStateEntity.UPDATED_AT, doc.getUpdatedAt() )
        .set( RStateEntity.RED, doc.isRed() )
        .where( RStateEntity.UID.eq( doc.getUid() ))
        .and( RStateEntity.USER.eq( login ))
        .get()
        .value();

      Timber.tag(TAG).d("Updated document state in RStateEntity table %s for %s", doc.getUid(), login);

    } else {
      dataStore
        .insert( RStateEntity.class )
        .value( RStateEntity.UID, doc.getUid() )
        .value( RStateEntity.USER, login )
        .value( RStateEntity.FILTER, doc.getFilter() )
        .value( RStateEntity.DOCUMENT_TYPE, doc.getDocumentType() )
        .value( RStateEntity.CONTROL, doc.isControl() )
        .value( RStateEntity.FAVORITES, doc.isFavorites() )
        .value( RStateEntity.PROCESSED, doc.isProcessed() )
        .value( RStateEntity.FROM_FAVORITES_FOLDER, doc.isFromFavoritesFolder() )
        .value( RStateEntity.FROM_PROCESSED_FOLDER, doc.isFromProcessedFolder() )
        .value( RStateEntity.FROM_LINKS, doc.isFromLinks() )
        .value( RStateEntity.RETURNED, doc.isReturned() )
        .value( RStateEntity.REJECTED, doc.isRejected() )
        .value( RStateEntity.AGAIN, doc.isAgain() )
        .value( RStateEntity.UPDATED_AT, doc.getUpdatedAt() )
        .value( RStateEntity.RED, doc.isRed() )
        .get()
        .firstOrNull();

      Timber.tag(TAG).d("Added document state to RStateEntity table %s for %s", doc.getUid(), login);
    }
  }

  private void dropDocumentState(RDocumentEntity doc, String TAG) {
    Timber.tag(TAG).d("Drop document state for %s", doc.getUid());

    dataStore
      .update( RDocumentEntity.class)
      .set( RDocumentEntity.FILTER, "")
      .set( RDocumentEntity.DOCUMENT_TYPE, "")
      .set( RDocumentEntity.CONTROL, false)
      .set( RDocumentEntity.FAVORITES, false)
      .set( RDocumentEntity.PROCESSED, false)
      .set( RDocumentEntity.FROM_FAVORITES_FOLDER, false)
      .set( RDocumentEntity.FROM_PROCESSED_FOLDER, false)
      .set( RDocumentEntity.FROM_LINKS, false)
      .set( RDocumentEntity.RETURNED, false )
      .set( RDocumentEntity.REJECTED, false )
      .set( RDocumentEntity.AGAIN, false )
      .set( RDocumentEntity.UPDATED_AT, null )
      .set( RDocumentEntity.RED, false )
      .where(RDocumentEntity.UID.eq( doc.getUid() ))
      .get()
      .value();
  }

  // При переключении режима:
  // 1. Сохраняем состояние документа для логина, из режима которого выходим
  // 2. Восстанавливаем состояние документа для логина, в режим которого входим
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
    dataStore
      .update( RDocumentEntity.class )
      .set( RDocumentEntity.USER, login )
      .set( RDocumentEntity.FILTER, stateEntity.getFilter() )
      .set( RDocumentEntity.DOCUMENT_TYPE, stateEntity.getDocumentType() )
      .set( RDocumentEntity.CONTROL, stateEntity.isControl() )
      .set( RDocumentEntity.FAVORITES, stateEntity.isFavorites() )
      .set( RDocumentEntity.PROCESSED, stateEntity.isProcessed() )
      .set( RDocumentEntity.FROM_FAVORITES_FOLDER, stateEntity.isFromFavoritesFolder() )
      .set( RDocumentEntity.FROM_PROCESSED_FOLDER, stateEntity.isFromProcessedFolder() )
      .set( RDocumentEntity.FROM_LINKS, stateEntity.isFromLinks() )
      .set( RDocumentEntity.RETURNED, stateEntity.isReturned() )
      .set( RDocumentEntity.REJECTED, stateEntity.isRejected() )
      .set( RDocumentEntity.AGAIN, stateEntity.isAgain() )
      .set( RDocumentEntity.UPDATED_AT, stateEntity.getUpdatedAt() )
      .set( RDocumentEntity.RED, stateEntity.isRed() )
      .where( RDocumentEntity.UID.eq( stateEntity.getUid() ) )
      .get()
      .value();

    Timber.tag(TAG).d("Restored document state from RStateEntity table %s for %s", stateEntity.getUid(), login);
  }
}

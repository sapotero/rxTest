package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import sapotero.rxtest.db.mapper.DocumentMapper;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.db.requery.models.utils.RReturnedRejectedAgainEntity;
import sapotero.rxtest.db.requery.utils.Deleter;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.utils.memory.fields.DocumentType;
import timber.log.Timber;

// Updates ordinary documents, projects, documents from favorite folder and documents from processed folder
public class UpdateDocumentJob extends DocumentJob {

  public static final int PRIORITY = 1;

  private String TAG = this.getClass().getSimpleName();

  private String uid;
  private String index  = null;
  private String filter = null;
  private Boolean forceUpdate       = false;
  private Boolean forceProcessed    = false;
  private Boolean forceDropFavorite = false;
  private DocumentType documentType = DocumentType.DOCUMENT;

  private int oldSignerId;
  private int oldRouteId;

  private boolean fromLinks = false;

  public UpdateDocumentJob(String uid, String login, String currentUserId) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uid = uid;
    this.login = login;
    this.currentUserId = currentUserId;
  }

  public UpdateDocumentJob(String uid, String index, String filter, String login, String currentUserId) {
    super( new Params(PRIORITY).requireNetwork().persist().addTags("DocJob") );

    Timber.tag(TAG).e( "create %s - %s / %s", uid, index, filter );

    this.uid = uid;
    this.index = getJournalName(index);
    this.filter = filter;
    this.login = login;
    this.currentUserId = currentUserId;

    // если создаем с указанием типа журнала и статуса
    // то принудительно обновляем документ
    this.forceUpdate = true;
  }

  public UpdateDocumentJob(String uid, String index, String filter, boolean forceProcessed, String login, String currentUserId) {
    super( new Params(PRIORITY).requireNetwork().persist().addTags("DocJob") );

    Timber.tag(TAG).e( "create %s - %s / %s", uid, index, filter );

    this.uid = uid;
    this.index = getJournalName(index);
    this.filter = filter;
    this.login = login;
    this.currentUserId = currentUserId;

    this.forceProcessed = forceProcessed;

    // Чтобы убрать документ в обработанные, принудительно его обновляем
    this.forceUpdate = true;
  }

  public UpdateDocumentJob(String uid, DocumentType documentType, String login, String currentUserId) {
    super( new Params(PRIORITY).requireNetwork().persist().addTags("DocJob") );

    this.uid = uid;
    this.documentType = documentType;
    this.login = login;
    this.currentUserId = currentUserId;
  }

  public UpdateDocumentJob(String uid, DocumentType documentType, boolean forceDropFavorite, String login, String currentUserId) {
    super( new Params(PRIORITY).requireNetwork().persist().addTags("DocJob") );

    this.uid = uid;
    this.documentType = documentType;
    this.login = login;
    this.currentUserId = currentUserId;
    this.forceDropFavorite = forceDropFavorite;
    this.forceUpdate = true;
  }

  public UpdateDocumentJob(String uid, boolean fromLinks, String login, String currentUserId) {
    super( new Params(PRIORITY).requireNetwork().persist() );

    this.uid = uid;
    this.fromLinks = fromLinks;
    this.login = login;
    this.currentUserId = currentUserId;

    // если ссылка, то обновляем принудительно, чтобы загрузились образы
    this.forceUpdate = true;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: Starting job");

    RDocumentEntity documentExisting = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(uid))
      .get().firstOrNull();

    if ( documentExisting != null && documentExisting.isChanged() != null && !documentExisting.isChanged() ) {
      Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: Loading document");
      loadDocument(uid, TAG);
    } else {
      Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: Document has Sync label, quit loading");
      EventBus.getDefault().post( new StepperLoadDocumentEvent( uid ) );
    }
  }

  @Override
  public void doAfterLoad(DocumentInfo documentReceived) {
    Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: doAfterLoad");

    RDocumentEntity documentExisting = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(uid))
      .get().firstOrNull();

    if ( documentExisting != null && documentExisting.isChanged() != null && !documentExisting.isChanged() ) {
      Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: Starting update");

      // Force update, if document exists and it must be favorite, because it is from favorites folder
      if ( documentExisting.isFavorites() != null && !documentExisting.isFavorites() && documentType == DocumentType.FAVORITE ) {
        forceUpdate = true;
      }

//      // Force update, if document exists and it must be from processed folder, but is not
//      if ( documentExisting.isFromProcessedFolder() != null && !documentExisting.isFromProcessedFolder() && documentType == DocumentType.PROCESSED ) {
//        forceUpdate = true;
//      }

      if ( !Objects.equals( documentReceived.getMd5(), documentExisting.getMd5() ) || forceUpdate ) {
        Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: MD5 not equal, updating document");
        Timber.tag(TAG).d( "MD5 not equal %s - %s", documentReceived.getMd5(), documentExisting.getMd5() );

        saveIdsToDelete( documentExisting );

        DocumentMapper documentMapper = mappers.getDocumentMapper();
        documentMapper.setBaseFields( documentExisting, documentReceived );
        documentMapper.setJournal( documentExisting, index );
        documentMapper.setFilter( documentExisting, filter );

        deleteLinkedDataPartOne( documentExisting );

        boolean isFromProcessedFolder = Boolean.TRUE.equals( documentExisting.isFromProcessedFolder() );
        documentMapper.setNestedFields( documentExisting, documentReceived, isFromProcessedFolder );

        boolean isSetProcessedFalse = true;

        if ( isFromProcessedFolder ) {
          // если документ из папки обработанных, то не убираем из обработанных
          isSetProcessedFalse = false;
        }

        if ( filter == null && index == null) {
          // если не указаны статус и журнал, то не убираем из обработанных
          isSetProcessedFalse = false;
        }

        if ( filter != null && Objects.equals(documentExisting.getFilter(), filter) ) {
          // если указан статус, и статус не изменился, то не убираем из обработанных
          isSetProcessedFalse = false;
        }

        if ( index != null && Objects.equals(documentExisting.getDocumentType(), index) ) {
          // если указан журнал, и журнал не изменился, то не убираем из обработанных
          isSetProcessedFalse = false;
        }

        if ( isSetProcessedFalse ) {
          // если прилетело обновление и документ не из папки обработанных и указаны статус или журнал и хотя бы один из них изменился - уберем из обработанных
          documentExisting.setProcessed( false );
        }

        // Если документ адресован текущему пользователю, то убрать из обработанных и из папки обработанных и из папки избранных
        // (например, документ возвращен текущему пользователю после отклонения)
        if ( addressedToCurrentUser( documentReceived, documentExisting, documentMapper ) ) {
          Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: Set processed = false");
          documentExisting.setProcessed( false );
          documentExisting.setFromProcessedFolder( false );
          documentExisting.setFromFavoritesFolder( false );

          setReturnedRejectedAgainLabel( documentExisting );
        }

        if ( forceProcessed ) {
          Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: Set processed = true");
          clearReturnedRejectedAgainLabels( documentExisting );
          documentExisting.setProcessed( true );
        }

//        if ( documentType == DocumentType.PROCESSED ) {
//          documentExisting.setProcessed( true );
//          documentExisting.setFromProcessedFolder( true );
//        }

        if ( documentType == DocumentType.FAVORITE ) {
          documentExisting.setFavorites( true );
        }

        if ( forceDropFavorite ) {
          documentExisting.setFavorites( false );
          documentExisting.setFromFavoritesFolder( false );
        }

        documentExisting.setFromLinks( fromLinks );
        documentExisting.setChanged( false );

        Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: writing update to data store");
        updateDocument( documentReceived, documentExisting, TAG );

      } else {
        Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: MD5 equal");
        Timber.tag(TAG).d("MD5 equal");
      }
    } else {
      Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: Document has Sync label, quit updating in DB");
    }
  }

  private void setReturnedRejectedAgainLabel(RDocumentEntity documentExisting) {
    clearReturnedRejectedAgainLabels( documentExisting );

    RReturnedRejectedAgainEntity returnedRejectedAgainEntity = dataStore
      .select( RReturnedRejectedAgainEntity.class )
      .where( RReturnedRejectedAgainEntity.DOCUMENT_UID.eq( documentExisting.getUid() ) )
      .and( RReturnedRejectedAgainEntity.USER.eq( login ) )
      .get().firstOrNull();

    if ( returnedRejectedAgainEntity != null && Objects.equals( documentExisting.getFilter(), returnedRejectedAgainEntity.getStatus() ) ) {
      switch (returnedRejectedAgainEntity.getDocumentCondition()) {
        case PROCESSED:
          documentExisting.setReturned( true );
          break;
        case REJECTED:
          documentExisting.setAgain( true );
          break;
      }
    }
  }

  private void clearReturnedRejectedAgainLabels(RDocumentEntity documentExisting) {
    documentExisting.setReturned( false );
    documentExisting.setRejected( false );
    documentExisting.setAgain( false );
  }

  @Override
  public void doAfterUpdate(RDocumentEntity document) {
    deleteLinkedDataPartTwo();

    if (document != null && !fromLinks) {
      Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: doAfterUpdate");
      Timber.tag(TAG).e( "doAfterUpdate %s - %s / %s", uid, filter, index );
      store.process( document, filter, index );
    }
  }

  private void saveIdsToDelete(RDocumentEntity document) {
    oldSignerId = getOldSignerId( document );
    oldRouteId = getOldRouteId( document );
  }

  private int getOldSignerId(RDocumentEntity document) {
    if ( exist( document.getSigner() ) ) {
      return ((RSignerEntity) document.getSigner()).getId();
    } else {
      return 0;
    }
  }

  private int getOldRouteId(RDocumentEntity document) {
    if ( exist( document.getRoute() ) ) {
      return ((RRouteEntity) document.getRoute()).getId();
    } else {
      return 0;
    }
  }

  private void deleteLinkedDataPartOne(RDocumentEntity document) {
    Deleter deleter = new Deleter();

    deleter.deleteDecisions( document, TAG );
    deleter.deleteExemplars( document, TAG );
    deleter.deleteImages( document, TAG );
    deleter.deleteControlLabels( document, TAG );
    deleter.deleteActions( document, TAG );
    deleter.deleteLinks( document, TAG );
  }

  private void deleteLinkedDataPartTwo() {
    Deleter deleter = new Deleter();

    deleter.deleteSigner( oldSignerId, TAG );
    deleter.deleteRoute( oldRouteId, TAG );
  }

  @Override
  protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
    return RetryConstraint.createExponentialBackoff(runCount, 1000);
  }

  @Override
  protected void onCancel(@CancelReason int cancelReason, @Nullable Throwable throwable) {
    // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
    EventBus.getDefault().post( new StepperLoadDocumentEvent("Error updating document (job cancelled)") );
  }
}

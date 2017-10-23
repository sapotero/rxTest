package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;
import java.util.Set;

import sapotero.rxtest.db.mapper.DocumentMapper;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
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

    RDocumentEntity documentExisting = getDocumentExisting();

    if ( documentExisting != null && documentExisting.isChanged() != null && !documentExisting.isChanged() ) {
      Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: Loading document");
      loadDocument(uid, TAG);
    } else {
      Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: Document has Sync label, quit loading");
      Timber.tag(TAG).d("documentExisting == null ? %s", documentExisting == null);
      EventBus.getDefault().post( new StepperLoadDocumentEvent( uid ) );
    }
  }

  private RDocumentEntity getDocumentExisting() {
    return dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(uid))
      .get().firstOrNull();
  }

  @Override
  public void doAfterLoad(DocumentInfo documentReceived) {
    if ( !Objects.equals( login, settings.getLogin() ) ) {
      // Обрабатываем загруженный документ только если логин не сменился (режим замещения)
      Timber.tag(TAG).d("Login changed, quit doAfterLoad %s", uid);
      return;
    }

    Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: doAfterLoad");

    RDocumentEntity documentExisting = getDocumentExisting();

    if ( documentExisting != null && documentExisting.isChanged() != null && !documentExisting.isChanged() ) {
      Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: Starting update");

      // Force update, if document exists and it must be favorite, because it is from favorites folder
      if ( documentExisting.isFavorites() != null && !documentExisting.isFavorites() && documentType == DocumentType.FAVORITE ) {
        forceUpdate = true;
      }

      if ( !Objects.equals( documentReceived.getMd5(), documentExisting.getMd5() ) || forceUpdate ) {
        Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: MD5 not equal, updating document");
        Timber.tag(TAG).d( "MD5 not equal %s - %s", documentReceived.getMd5(), documentExisting.getMd5() );

        // Create new RDocumentEntity and copy state from the existing one
        RDocumentEntity documentNew = new RDocumentEntity();
        copyDocumentState( documentNew, documentExisting );
        boolean isFromProcessedFolder = Boolean.TRUE.equals( documentExisting.isFromProcessedFolder() );

        // Update fields of the new entity with loaded data
        DocumentMapper documentMapper = new DocumentMapper().withLogin(login).withCurrentUserId(currentUserId);
        documentMapper.setBaseFields( documentNew, documentReceived );
        documentMapper.setJournal( documentNew, index );
        documentMapper.setFilter( documentNew, filter );
        documentMapper.setNestedFields( documentNew, documentReceived, isFromProcessedFolder );

        intersectImages( documentExisting, documentNew );

        // Delete existing entity and all linked data from DB
        new Deleter().deleteDocument( documentExisting, false, TAG );

        boolean isSetProcessedFalse = true;

        if ( isFromProcessedFolder ) {
          // если документ из папки обработанных, то не убираем из обработанных
          isSetProcessedFalse = false;
        }

        if ( filter == null && index == null) {
          // если не указаны статус и журнал, то не убираем из обработанных
          isSetProcessedFalse = false;
        }

        if ( filter != null && Objects.equals(documentNew.getFilter(), filter) ) {
          // если указан статус, и статус не изменился, то не убираем из обработанных
          isSetProcessedFalse = false;
        }

        if ( index != null && Objects.equals(documentNew.getDocumentType(), index) ) {
          // если указан журнал, и журнал не изменился, то не убираем из обработанных
          isSetProcessedFalse = false;
        }

        if ( isSetProcessedFalse ) {
          // если прилетело обновление и документ не из папки обработанных и указаны статус или журнал и хотя бы один из них изменился - уберем из обработанных
          documentNew.setProcessed( false );
        }

        // Если документ адресован текущему пользователю, то убрать из обработанных и из папки обработанных и из папки избранных
        // (например, документ возвращен текущему пользователю после отклонения)
        if ( addressedToCurrentUser( documentReceived, documentNew, documentMapper ) ) {
          Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: Set processed = false");
          documentNew.setProcessed( false );
          documentNew.setFromProcessedFolder( false );
          documentNew.setFromFavoritesFolder( false );

          setReturnedRejectedAgainLabel( documentNew );
        }

        if ( forceProcessed ) {
          Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: Set processed = true");
          clearReturnedRejectedAgainLabels( documentNew );
          documentNew.setProcessed( true );
        }

        if ( documentType == DocumentType.FAVORITE ) {
          documentNew.setFavorites( true );
        }

        if ( forceDropFavorite ) {
          documentNew.setFavorites( false );
          documentNew.setFromFavoritesFolder( false );
        }

        documentNew.setFromLinks( fromLinks );
        documentNew.setChanged( false );

        // Insert new entity and linked data into DB if existing entity has been properly deleted
        if ( getDocumentExisting() == null ) {
          Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: writing update to data store");
          insert( documentReceived, documentNew, false, true, TAG );
        }

      } else {
        Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: MD5 equal");
        Timber.tag(TAG).d("MD5 equal");
      }
    } else {
      Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: Document has Sync label, quit updating in DB");
      Timber.tag(TAG).d("documentExisting == null ? %s", documentExisting == null);
    }
  }

  private void copyDocumentState(RDocumentEntity documentNew, RDocumentEntity documentExisting) {
    documentNew.setFavorites( documentExisting.isFavorites() );
    documentNew.setProcessed( documentExisting.isProcessed() );
    documentNew.setControl( documentExisting.isControl() );
    documentNew.setFromLinks( documentExisting.isFromLinks() );
    documentNew.setFromProcessedFolder( documentExisting.isFromProcessedFolder() );
    documentNew.setFromFavoritesFolder( documentExisting.isFromFavoritesFolder() );
    documentNew.setChanged( documentExisting.isChanged() );
    documentNew.setReturned( documentExisting.isReturned() );
    documentNew.setRejected( documentExisting.isRejected() );
    documentNew.setAgain( documentExisting.isAgain() );
    documentNew.setRed( documentExisting.isRed() );
    documentNew.setUpdatedAt( documentExisting.getUpdatedAt() );
    documentNew.setProcessedDate( documentExisting.getProcessedDate() );
    documentNew.setFilter( documentExisting.getFilter() );
    documentNew.setDocumentType( documentExisting.getDocumentType() );
    documentNew.setAddressedToType( documentExisting.getAddressedToType() );
  }

  // resolved https://tasks.n-core.ru/browse/MPSED-2213
  // Сделать интерсект образов. Загружать только те образы, у которых поменялся MD5 (или которых раньше не было в документе)
  private void intersectImages(RDocumentEntity documentExisting, RDocumentEntity documentNew) {
    if ( documentExisting.getImages() != null && documentNew.getImages() != null ) {

      // Mark image files to be deleted
      for ( RImage _image : documentExisting.getImages() ) {
        RImageEntity existingImageEntity = (RImageEntity) _image;
        if ( existingImageEntity.getImageId() != null ) {
          // If existing image is not present in new document, mark it to be deleted
          if ( getImageByUid( documentNew.getImages(), existingImageEntity.getImageId() ) == null ) {
            existingImageEntity.setToDeleteFile( true );
          }
        }
      }

      // Mark image files to be loaded
      for ( RImage _image : documentNew.getImages() ) {
        RImageEntity newImageEntity = (RImageEntity) _image;
        // At first mark new image file not to be loaded
        newImageEntity.setToLoadFile( false );

        if ( newImageEntity.getImageId() != null ) {
          RImageEntity existingImageEntity = getImageByUid( documentExisting.getImages(), newImageEntity.getImageId() );

          // If image file exists
          if ( existingImageEntity != null ) {
            // And MD5 changed, mark new image file to be loaded and old image file to be deleted
            if ( !Objects.equals( existingImageEntity.getMd5(), newImageEntity.getMd5() ) ) {
              existingImageEntity.setToDeleteFile( true );
              newImageEntity.setToLoadFile( true );

            } else if ( existingImageEntity.isNoFreeSpace() != null && existingImageEntity.isNoFreeSpace() ) {
              // If MD5 not changed, but image file has not been loaded due to lack of free space, mark image file to be loaded
              newImageEntity.setToLoadFile( true );
            }

          } else {
            // If image file does not exist, mark it to be loaded
            newImageEntity.setToLoadFile( true );
          }
        }
      }

    }
  }

  private RImageEntity getImageByUid(Set<RImage> images, String imageId) {
    RImageEntity result = null;

    for ( RImage _image : images ) {
      RImageEntity imageEntity = (RImageEntity) _image;
      if ( Objects.equals( imageEntity.getImageId(), imageId ) ) {
        result = imageEntity;
        break;
      }
    }

    return result;
  }

  private void setReturnedRejectedAgainLabel(RDocumentEntity document) {
    clearReturnedRejectedAgainLabels( document );

    RReturnedRejectedAgainEntity returnedRejectedAgainEntity = dataStore
      .select( RReturnedRejectedAgainEntity.class )
      .where( RReturnedRejectedAgainEntity.DOCUMENT_UID.eq( document.getUid() ) )
      .and( RReturnedRejectedAgainEntity.USER.eq( login ) )
      .get().firstOrNull();

    if ( returnedRejectedAgainEntity != null && Objects.equals( document.getFilter(), returnedRejectedAgainEntity.getStatus() ) ) {
      switch (returnedRejectedAgainEntity.getDocumentCondition()) {
        case PROCESSED:
          document.setReturned( true );
          break;
        case REJECTED:
          document.setAgain( true );
          break;
      }
    }
  }

  private void clearReturnedRejectedAgainLabels(RDocumentEntity document) {
    document.setReturned( false );
    document.setRejected( false );
    document.setAgain( false );
  }

  @Override
  public void doAfterUpdate(RDocumentEntity document) {
    if (document != null && !fromLinks) {
      Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: doAfterUpdate");
      Timber.tag(TAG).e( "doAfterUpdate %s - %s / %s", uid, filter, index );
      store.process( document, filter, index );
    }
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

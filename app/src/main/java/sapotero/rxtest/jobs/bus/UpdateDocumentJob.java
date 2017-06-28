package sapotero.rxtest.jobs.bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.CancelReason;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Objects;

import sapotero.rxtest.db.mapper.DocumentMapper;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RLinksEntity;
import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.db.requery.models.RStepEntity;
import sapotero.rxtest.db.requery.models.actions.RActionEntity;
import sapotero.rxtest.db.requery.models.control_labels.RControlLabelsEntity;
import sapotero.rxtest.db.requery.models.decisions.RBlock;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecision;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.db.requery.models.exemplars.RExemplarEntity;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.document.Exemplar;
import sapotero.rxtest.retrofit.models.document.Status;
import sapotero.rxtest.utils.memory.fields.DocumentType;
import timber.log.Timber;

// Updates ordinary documents, projects, documents from favorite folder and documents from processed folder
public class UpdateDocumentJob extends DocumentJob {

  public static final int PRIORITY = 1;

  private String TAG = this.getClass().getSimpleName();

  private String uid;
  private String index  = null;
  private String filter = null;
  private Boolean forceUpdate    = false;
  private Boolean forceProcessed = false;
  private DocumentType documentType = DocumentType.DOCUMENT;

  private int oldSignerId;
  private int oldRouteId;


  public UpdateDocumentJob(String uid) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uid = uid;
  }

  public UpdateDocumentJob(String uid, String index, String filter) {
    super( new Params(PRIORITY).requireNetwork().persist() );

    Timber.tag(TAG).e( "create %s - %s / %s", uid, index, filter );

    this.uid = uid;
    this.index = getJournalName(index);
    this.filter = filter;

    // если создаем с указанием типа журнала и статуса
    // то принудительно обновляем документ
    this.forceUpdate = true;
  }

  public UpdateDocumentJob(String uid, String index, String filter, boolean forceProcessed) {
    super( new Params(PRIORITY).requireNetwork().persist() );

    Timber.tag(TAG).e( "create %s - %s / %s", uid, index, filter );

    this.uid = uid;
    this.index = getJournalName(index);
    this.filter = filter;

    this.forceProcessed = forceProcessed;
  }

  public UpdateDocumentJob(String uid, DocumentType documentType) {
    super( new Params(PRIORITY).requireNetwork().persist() );

    this.uid = uid;
    this.documentType = documentType;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: Load document");

    loadDocument(uid, TAG);
  }

  @Override
  public void doAfterLoad(DocumentInfo documentReceived) {
    Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: doAfterLoad");

    RDocumentEntity documentExisting = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(uid))
      .get().firstOrNull();

    if ( exist( documentExisting ) ) {
//      // Force update, if document exists and it must be from favorites folder, but is not
//      if ( documentExisting.isFromFavoritesFolder() != null && !documentExisting.isFromFavoritesFolder() && documentType == DocumentType.FAVORITE ) {
//        forceUpdate = true;
//      }
//
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

        // Если документ адресован текущему пользователю, то убрать из обработанных и из папки обработанных
        // (например, документ возвращен текущему пользователю после отклонения)
        if ( addressedToCurrentUser( documentReceived, documentExisting, documentMapper ) ) {
          Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: Set processed = false");
          documentExisting.setProcessed( false );
          documentExisting.setFromProcessedFolder( false );
        }

        if ( forceProcessed ) {
          Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: Set processed = true");
          documentExisting.setProcessed( true );
        }

//        if ( documentType == DocumentType.PROCESSED ) {
//          documentExisting.setProcessed( true );
//          documentExisting.setFromProcessedFolder( true );
//        }
//
//        if ( documentType == DocumentType.FAVORITE ) {
//          documentExisting.setFavorites( true );
//          documentExisting.setFromFavoritesFolder( true );
//        }

        documentExisting.setFromLinks( false );
        documentExisting.setChanged( false );

        Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: writing update to data store");
        updateDocument( documentReceived, documentExisting, TAG );

      } else {
        Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: MD5 equal");
        Timber.tag(TAG).d("MD5 equal");
      }
    }
  }

  @Override
  public void doAfterUpdate(RDocumentEntity document) {
    deleteLinkedDataPartTwo();

    if (document != null) {
      Timber.tag("RecyclerViewRefresh").d("UpdateDocumentJob: doAfterUpdate");
      Timber.tag(TAG).e( "doAfterUpdate %s - %s / %s", uid, index, filter );
      store.process( document, index, filter );
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
    deleteDecisions( document );
    deleteExemplars( document );
    deleteImages( document );
    deleteControlLabels( document );
    deleteActions( document );
    deleteLinks( document );
  }

  private void deleteLinkedDataPartTwo() {
    deleteSigner();
    deleteRoute();
  }

  private void deleteDecisions(RDocumentEntity document) {
    if ( notEmpty( document.getDecisions() ) ) {
      for (RDecision _decision : document.getDecisions() ) {
        RDecisionEntity decision = (RDecisionEntity) _decision;
        deleteBlocks( decision );
      }

      int count = dataStore
        .delete( RDecisionEntity.class )
        .where( RDecisionEntity.DOCUMENT_ID.eq( document.getId() ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " decisions from document with ID " + document.getId());
    }
  }

  private void deleteBlocks(RDecisionEntity decision) {
    if ( notEmpty( decision.getBlocks() ) ) {
      for ( RBlock _block : decision.getBlocks() ) {
        RBlockEntity block = (RBlockEntity) _block;
        deletePerformers( block );
      }

      int count = dataStore
        .delete( RBlockEntity.class )
        .where( RBlockEntity.DECISION_ID.eq( decision.getId() ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " blocks from decision with ID " + decision.getId());
    }
  }

  private void deletePerformers(RBlockEntity block) {
    int count = dataStore
      .delete( RPerformerEntity.class )
      .where( RPerformerEntity.BLOCK_ID.eq( block.getId() ) )
      .get().value();

    Timber.tag(TAG).d("Deleted " + count + " performers from block with ID " + block.getId());
  }

  private void deleteExemplars(RDocumentEntity document) {
    if ( notEmpty( document.getExemplars() ) ) {
      int count = dataStore
        .delete( RExemplarEntity.class )
        .where( RExemplarEntity.DOCUMENT_ID.eq( document.getId() ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " exemplars from document with ID " + document.getId());
    }
  }

  private void deleteImages(RDocumentEntity document) {
    if ( notEmpty( document.getImages() ) ) {
      int count = dataStore
        .delete( RImageEntity.class )
        .where( RImageEntity.DOCUMENT_ID.eq( document.getId() ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " images from document with ID " + document.getId());
    }
  }

  private void deleteControlLabels(RDocumentEntity document) {
    if ( notEmpty( document.getControlLabels() ) ) {
      int count = dataStore
        .delete( RControlLabelsEntity.class )
        .where( RControlLabelsEntity.DOCUMENT_ID.eq( document.getId() ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " control labels from document with ID " + document.getId());
    }
  }

  private void deleteActions(RDocumentEntity document) {
    if ( notEmpty( document.getActions() ) ) {
      int count = dataStore
        .delete( RActionEntity.class )
        .where( RActionEntity.DOCUMENT_ID.eq( document.getId() ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " actions from document with ID " + document.getId());
    }
  }

  private void deleteLinks(RDocumentEntity document) {
    if ( notEmpty( document.getLinks() ) ) {
      int count = dataStore
        .delete( RLinksEntity.class )
        .where( RLinksEntity.DOCUMENT_ID.eq( document.getId() ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " links from document with ID " + document.getId());
    }
  }

  private void deleteSigner() {
    if ( oldSignerId > 0 ) {
      int count = dataStore
        .delete( RSignerEntity.class )
        .where( RSignerEntity.ID.eq( oldSignerId ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " signers with ID " + oldSignerId);
    }
  }

  private void deleteRoute() {
    if ( oldRouteId > 0 ) {
      deleteSteps( oldRouteId );

      int count = dataStore
        .delete( RRouteEntity.class )
        .where( RRouteEntity.ID.eq( oldRouteId ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " routes with ID " + oldRouteId);
    }
  }

  private void deleteSteps(int routeId) {
    int count = dataStore
      .delete(RStepEntity.class)
      .where(RStepEntity.ROUTE_ID.eq(routeId))
      .get().value();

    Timber.tag(TAG).d("Deleted " + count + " steps from route with ID " + routeId);
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

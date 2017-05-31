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
import timber.log.Timber;

// Updates ordinary documents, projects, documents from favorite folder and documents from processed folder
public class UpdateDocumentJob extends DocumentJob {

  public static final int PRIORITY = 1;

  private String TAG = this.getClass().getSimpleName();

  private String uid;

  private int oldSignerId;
  private int oldRouteId;

  public UpdateDocumentJob(String uid) {
    super( new Params(PRIORITY).requireNetwork().persist() );
    this.uid = uid;
  }

  @Override
  public void onAdded() {
  }

  @Override
  public void onRun() throws Throwable {
    loadDocument(uid, TAG);
  }

  @Override
  public void doAfterLoad(DocumentInfo documentReceived) {

    RDocumentEntity documentExisting = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(uid))
      .get().firstOrNull();

    if ( exist( documentExisting ) ) {
      if ( !Objects.equals( documentReceived.getMd5(), documentExisting.getMd5() ) ) {
        Timber.tag(TAG).d( "MD5 not equal %s - %s", documentReceived.getMd5(), documentExisting.getMd5() );

        saveIdsToDelete( documentExisting );

        DocumentMapper documentMapper = mappers.getDocumentMapper();
        documentMapper.setBaseFields( documentExisting, documentReceived );

        deleteLinkedDataPartOne( documentExisting );

        boolean isFromProcessedFolder = Boolean.TRUE.equals( documentExisting.isFromProcessedFolder() );
        documentMapper.setNestedFields( documentExisting, documentReceived, isFromProcessedFolder );
        if ( !isFromProcessedFolder ) {
          // если прилетело обновление и документ не из папки обработанных - уберем из обработанных
          documentExisting.setProcessed( false );
        }

        updateDocument( documentReceived, documentExisting, TAG );

      } else {
        Timber.tag(TAG).d("MD5 equal");
      }
    }
  }

  @Override
  public void doAfterUpdate() {
    deleteLinkedDataPartTwo();
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
            .delete( RStepEntity.class )
            .where( RStepEntity.ROUTE_ID.eq( routeId ) )
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
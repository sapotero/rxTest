package sapotero.rxtest.db.requery.utils;

import java.util.Collection;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.application.EsdApplication;
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
import timber.log.Timber;

public class Deleter {

  @Inject SingleEntityStore<Persistable> dataStore;

  public Deleter() {
    EsdApplication.getDataComponent().inject(this);
  }

  public <T> boolean notEmpty(Collection<T> collection) {
    return collection != null && collection.size() > 0;
  }

  public void deleteDocument(RDocumentEntity document, String TAG) {
    deleteDecisions( document, TAG );
    deleteExemplars( document, TAG );
    deleteImages( document, TAG );
    deleteControlLabels( document, TAG );
    deleteActions( document, TAG );
    deleteLinks( document, TAG );

    int count = dataStore
      .delete( RDocumentEntity.class )
      .where( RDocumentEntity.UID.eq( document.getUid() ) )
      .get().value();

    Timber.tag(TAG).d("Deleted " + count + " documents with UID " + document.getUid());

    int signerId = document.getSigner() != null ? ((RSignerEntity) document.getSigner()).getId() : 0;
    int routeId = document.getRoute() != null ? ((RRouteEntity) document.getRoute()).getId() : 0;

    deleteSigner( signerId, TAG );
    deleteRoute( routeId, TAG );
  }

  public void deleteDecisions(RDocumentEntity document, String TAG) {
    if ( notEmpty( document.getDecisions() ) ) {
      for (RDecision _decision : document.getDecisions() ) {
        RDecisionEntity decision = (RDecisionEntity) _decision;
        deleteBlocks( decision, TAG );
      }

      int count = dataStore
        .delete( RDecisionEntity.class )
        .where( RDecisionEntity.DOCUMENT_ID.eq( document.getId() ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " decisions from document with ID " + document.getId());
    }
  }

  private void deleteBlocks(RDecisionEntity decision, String TAG) {
    if ( notEmpty( decision.getBlocks() ) ) {
      for ( RBlock _block : decision.getBlocks() ) {
        RBlockEntity block = (RBlockEntity) _block;
        deletePerformers( block, TAG );
      }

      int count = dataStore
        .delete( RBlockEntity.class )
        .where( RBlockEntity.DECISION_ID.eq( decision.getId() ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " blocks from decision with ID " + decision.getId());
    }
  }

  private void deletePerformers(RBlockEntity block, String TAG) {
    int count = dataStore
      .delete( RPerformerEntity.class )
      .where( RPerformerEntity.BLOCK_ID.eq( block.getId() ) )
      .get().value();

    Timber.tag(TAG).d("Deleted " + count + " performers from block with ID " + block.getId());
  }

  public void deleteExemplars(RDocumentEntity document, String TAG) {
    if ( notEmpty( document.getExemplars() ) ) {
      int count = dataStore
        .delete( RExemplarEntity.class )
        .where( RExemplarEntity.DOCUMENT_ID.eq( document.getId() ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " exemplars from document with ID " + document.getId());
    }
  }

  public void deleteImages(RDocumentEntity document, String TAG) {
    if ( notEmpty( document.getImages() ) ) {
      int count = dataStore
        .delete( RImageEntity.class )
        .where( RImageEntity.DOCUMENT_ID.eq( document.getId() ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " images from document with ID " + document.getId());
    }
  }

  public void deleteControlLabels(RDocumentEntity document, String TAG) {
    if ( notEmpty( document.getControlLabels() ) ) {
      int count = dataStore
        .delete( RControlLabelsEntity.class )
        .where( RControlLabelsEntity.DOCUMENT_ID.eq( document.getId() ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " control labels from document with ID " + document.getId());
    }
  }

  public void deleteActions(RDocumentEntity document, String TAG) {
    if ( notEmpty( document.getActions() ) ) {
      int count = dataStore
        .delete( RActionEntity.class )
        .where( RActionEntity.DOCUMENT_ID.eq( document.getId() ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " actions from document with ID " + document.getId());
    }
  }

  public void deleteLinks(RDocumentEntity document, String TAG) {
    if ( notEmpty( document.getLinks() ) ) {
      int count = dataStore
        .delete( RLinksEntity.class )
        .where( RLinksEntity.DOCUMENT_ID.eq( document.getId() ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " links from document with ID " + document.getId());
    }
  }

  public void deleteSigner(int signerId, String TAG) {
    if ( signerId > 0 ) {
      int count = dataStore
        .delete( RSignerEntity.class )
        .where( RSignerEntity.ID.eq( signerId ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " signers with ID " + signerId);
    }
  }

  public void deleteRoute(int routeId, String TAG) {
    if ( routeId > 0 ) {
      deleteSteps( routeId, TAG );

      int count = dataStore
        .delete( RRouteEntity.class )
        .where( RRouteEntity.ID.eq( routeId ) )
        .get().value();

      Timber.tag(TAG).d("Deleted " + count + " routes with ID " + routeId);
    }
  }

  private void deleteSteps(int routeId, String TAG) {
    int count = dataStore
      .delete(RStepEntity.class)
      .where(RStepEntity.ROUTE_ID.eq(routeId))
      .get().value();

    Timber.tag(TAG).d("Deleted " + count + " steps from route with ID " + routeId);
  }
}

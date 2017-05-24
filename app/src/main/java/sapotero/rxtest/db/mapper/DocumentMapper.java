package sapotero.rxtest.db.mapper;

import java.util.List;
import java.util.Objects;

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
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.exemplars.RExemplarEntity;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.retrofit.models.document.ControlLabel;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.document.DocumentInfoAction;
import sapotero.rxtest.retrofit.models.document.Exemplar;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.retrofit.models.document.Route;
import sapotero.rxtest.retrofit.models.document.Signer;
import sapotero.rxtest.retrofit.models.document.Step;
import sapotero.rxtest.utils.Settings;

// Maps between DocumentInfo and RDocumentEntity
public class DocumentMapper extends AbstractMapper<DocumentInfo, RDocumentEntity> {

  @Inject Settings settings;

  public DocumentMapper() {
    EsdApplication.getDataComponent().inject(this);
  }

  @Override
  public RDocumentEntity toEntity(DocumentInfo model) {
    RDocumentEntity entity = new RDocumentEntity();

    convertSimpleFields(entity, model);
    convertNestedFields(entity, model);

    return entity;
  }

  @Override
  public DocumentInfo toModel(RDocumentEntity entity) {
    return null;
  }

  public void convertSimpleFields(RDocumentEntity entity, DocumentInfo model) {
    entity.setUid( model.getUid() );
    entity.setUser( settings.getLogin() );
    entity.setFavorites( false );
    entity.setProcessed( false );
    entity.setControl( false );
    entity.setFromLinks( false );
    entity.setFromProcessedFolder( false );
    entity.setFromFavoritesFolder( false );
    entity.setChanged( false );

    entity.setMd5( model.getMd5() );
    entity.setSortKey( model.getSortKey() );
    entity.setTitle( model.getTitle() );
    entity.setRegistrationNumber( model.getRegistrationNumber() );
    entity.setRegistrationDate( model.getRegistrationDate() );
    entity.setUrgency( model.getUrgency() );
    entity.setShortDescription( model.getShortDescription() );
    entity.setComment( model.getComment() );
    entity.setExternalDocumentNumber( model.getExternalDocumentNumber() );
    entity.setReceiptDate( model.getReceiptDate() );
    entity.setViewed( model.getViewed() );

    setSigner( entity, model.getSigner() );

    if ( exist( model.getSigner() ) ) {
      if ( stringNotEmpty( model.getSigner().getOrganisation() ) ) {
        entity.setOrganization( model.getSigner().getOrganisation() );
      } else {
        entity.setOrganization( "Без организации" );
      }
    }
  }

  public void convertNestedFields(RDocumentEntity entity, DocumentInfo model) {
    setDecisions(entity, model.getDecisions());
    setRoute(entity, model.getRoute());
    setExemplars(entity, model.getExemplars());
    setImages(entity, model.getImages());
    setControlLabels(entity, model.getControlLabels());
    setActions(entity, model.getActions());
    setLinks(entity, model.getLinks());
    setInfoCard(entity, model.getInfoCard());
  }

  public void setJournal(RDocumentEntity entity, String journal) {
    if ( exist( journal ) ) {
      entity.setDocumentType( journal );
    }
  }

  public void setFilter(RDocumentEntity entity, String filter) {
    if ( exist( filter ) ) {
      entity.setFilter( filter );
    }
  }

  public void setShared(RDocumentEntity entity, boolean shared) {
    if ( shared || Objects.equals(entity.getAddressedToType(), "group") ) {
      entity.setAddressedToType("group");
    } else {
      entity.setAddressedToType("");
    }
  }

  public void setSigner(RDocumentEntity entity, Signer signerModel) {
    if ( exist( signerModel ) ) {
      RSignerEntity signerEntity = new SignerMapper().toEntity( signerModel );
      entity.setSigner( signerEntity );
    }
  }

  public void setInfoCard(RDocumentEntity entity, String infoCard) {
    if ( exist( infoCard ) ) {
      entity.setInfoCard( infoCard );
    }
  }

  public void setLinks(RDocumentEntity entity, List<String> links) {
    if ( listNotEmpty( links ) ) {
      entity.getLinks().clear();
      LinkMapper linkMapper = new LinkMapper();

      for (String linkModel : links) {
        RLinksEntity linkEntity = linkMapper.toEntity( linkModel );
        linkEntity.setDocument( entity );
        entity.getLinks().add( linkEntity );
      }
    }
  }

  public void setControlLabels(RDocumentEntity entity, List<ControlLabel> controlLabels) {
    if ( listNotEmpty( controlLabels ) ) {
      entity.getControlLabels().clear();
      ControlLabelMapper controlLabelMapper = new ControlLabelMapper();

      for (ControlLabel labelModel : controlLabels ) {
        RControlLabelsEntity labelEntity = controlLabelMapper.toEntity( labelModel );
        labelEntity.setDocument( entity );
        entity.getControlLabels().add( labelEntity );
      }
    }
  }

  public void setImages(RDocumentEntity entity, List<Image> images) {
    if ( listNotEmpty( images ) ) {
      entity.getImages().clear();
      ImageMapper imageMapper = new ImageMapper();

      for (Image imageModel : images ) {
        RImageEntity imageEntity = imageMapper.toEntity( imageModel );
        imageEntity.setDocument( entity );
        entity.getImages().add( imageEntity );
      }
    }
  }

  public void setExemplars(RDocumentEntity entity, List<Exemplar> exemplars) {
    if ( listNotEmpty( exemplars ) ) {
      entity.getExemplars().clear();
      ExemplarMapper exemplarMapper = new ExemplarMapper();

      for (Exemplar exemplarModel : exemplars ) {
        RExemplarEntity exemplarEntity = exemplarMapper.toEntity( exemplarModel );
        exemplarEntity.setDocument( entity );
        entity.getExemplars().add( exemplarEntity );
      }
    }
  }

  public void setActions(RDocumentEntity entity, List<DocumentInfoAction> actions) {
    if ( listNotEmpty( actions ) ) {
      entity.getActions().clear();
      ActionMapper actionMapper = new ActionMapper();

      for (DocumentInfoAction actionModel: actions ) {
        RActionEntity actionEntity = actionMapper.toEntity( actionModel );
        actionEntity.setDocument( entity );
        entity.getActions().add( actionEntity );
      }
    }
  }

  public void setRoute(RDocumentEntity entity, Route route) {
    if ( exist( route ) ) {
      RRouteEntity routeEntity = (RRouteEntity) entity.getRoute();
      routeEntity.setText( route.getTitle() );
      routeEntity.getSteps().clear();
      StepMapper stepMapper = new StepMapper();

      for (Step stepModel : route.getSteps() ) {
        RStepEntity stepEntity = stepMapper.toEntity( stepModel );
        stepEntity.setRoute( routeEntity );
        routeEntity.getSteps().add( stepEntity );
      }
    }
  }

  public void setDecisions(RDocumentEntity entity, List<Decision> decisions) {
    Boolean red = false;
    Boolean with_decision = false;

    if ( listNotEmpty( decisions ) ) {
      with_decision = true;
      DecisionMapper decisionMapper = new DecisionMapper();

      for (Decision decisionModel : decisions ) {
        if ( decisionModel.getRed() ) {
          red = true;
        }

        RDecisionEntity decisionEntity = decisionMapper.toEntity( decisionModel );
        decisionEntity.setDocument( entity );
        entity.getDecisions().add( decisionEntity );
      }
    }

    entity.setWithDecision( with_decision );
    entity.setRed( red );
  }

  public void deleteDecisions(RDocumentEntity entity, DocumentInfo model, SingleEntityStore<Persistable> dataStore) {
    if ( listNotEmpty( model.getDecisions() ) ) {
      entity.getDecisions().clear();
      dataStore.delete(RDecisionEntity.class).where(RDecisionEntity.DOCUMENT_ID.eq(entity.getId())).get().value();
    }
  }

  public void updateProcessed(RDocumentEntity entity, String journal, String status, Fields.Status filter) {
    // если прилетоло обновление - уберем из обработанных
    if ( filter != null && filter.getValue() != null && status != null && entity.isProcessed() ) {
      entity.setProcessed( false );
    }

    // если подписание/согласование
    if ( journal == null && entity.getDocumentType() == null && status != null && entity.isProcessed() ) {
      entity.setProcessed( false );
    }
  }
}

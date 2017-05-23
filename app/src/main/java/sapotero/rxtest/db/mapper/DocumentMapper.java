package sapotero.rxtest.db.mapper;

import javax.inject.Inject;

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
import sapotero.rxtest.retrofit.models.document.ControlLabel;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.document.DocumentInfoAction;
import sapotero.rxtest.retrofit.models.document.Exemplar;
import sapotero.rxtest.retrofit.models.document.Image;
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

    if ( exist( model.getSigner() ) ) {
      RSignerEntity signer = new SignerMapper().toEntity(model.getSigner());
      entity.setSigner(signer);
    }

    if ( stringNotEmpty( model.getSigner().getOrganisation() ) ) {
      entity.setOrganization( model.getSigner().getOrganisation() );
    } else {
      entity.setOrganization( "Без организации" );
    }

    Boolean red = false;
    Boolean with_decision = false;

    if ( listNotEmpty( model.getDecisions() ) ) {
      with_decision = true;
      DecisionMapper decisionMapper = new DecisionMapper();

      for (Decision decisionModel : model.getDecisions() ) {
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

    if ( exist( model.getRoute() ) ) {
      RRouteEntity routeEntity = (RRouteEntity) entity.getRoute();
      routeEntity.setText( model.getRoute().getTitle() );
      StepMapper stepMapper = new StepMapper();

      for (Step stepModel : model.getRoute().getSteps() ) {
        RStepEntity stepEntity = stepMapper.toEntity( stepModel );
        stepEntity.setRoute( routeEntity );
        routeEntity.getSteps().add( stepEntity );
      }
    }

    if ( listNotEmpty( model.getExemplars() ) ) {
      ExemplarMapper exemplarMapper = new ExemplarMapper();

      for (Exemplar exemplarModel : model.getExemplars() ) {
        RExemplarEntity exemplarEntity = exemplarMapper.toEntity( exemplarModel );
        exemplarEntity.setDocument( entity );
        entity.getExemplars().add( exemplarEntity );
      }
    }

    if ( listNotEmpty( model.getImages() ) ) {
      ImageMapper imageMapper = new ImageMapper();

      for (Image imageModel : model.getImages() ) {
        RImageEntity imageEntity = imageMapper.toEntity( imageModel );
        imageEntity.setDocument( entity );
        entity.getImages().add( imageEntity );
      }
    }

    if ( listNotEmpty( model.getControlLabels() ) ) {
      ControlLabelMapper controlLabelMapper = new ControlLabelMapper();

      for (ControlLabel labelModel : model.getControlLabels() ) {
        RControlLabelsEntity labelEntity = controlLabelMapper.toEntity( labelModel );
        labelEntity.setDocument( entity );
        entity.getControlLabels().add( labelEntity );
      }
    }

    if ( listNotEmpty( model.getActions() ) ) {
      ActionMapper actionMapper = new ActionMapper();

      for (DocumentInfoAction actionModel: model.getActions() ) {
        RActionEntity actionEntity = actionMapper.toEntity( actionModel );
        actionEntity.setDocument( entity );
        entity.getActions().add( actionEntity );
      }
    }

    if ( listNotEmpty( model.getLinks() ) ) {
      LinkMapper linkMapper = new LinkMapper();

      for (String linkModel : model.getLinks()) {
        RLinksEntity linkEntity = linkMapper.toEntity( linkModel );
        linkEntity.setDocument( entity );
        entity.getLinks().add( linkEntity );
      }
    }

    if ( exist( model.getInfoCard() ) ) {
      entity.setInfoCard( model.getInfoCard() );
    }

    return entity;
  }

  @Override
  public DocumentInfo toModel(RDocumentEntity entity) {
    return null;
  }

  private boolean exist(Object obj) {
    return obj != null;
  }
}

package sapotero.rxtest.db.mapper;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RLinks;
import sapotero.rxtest.db.requery.models.RLinksEntity;
import sapotero.rxtest.db.requery.models.RRoute;
import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.models.RSigner;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.db.requery.models.RStep;
import sapotero.rxtest.db.requery.models.RStepEntity;
import sapotero.rxtest.db.requery.models.actions.RAction;
import sapotero.rxtest.db.requery.models.actions.RActionEntity;
import sapotero.rxtest.db.requery.models.control_labels.RControlLabels;
import sapotero.rxtest.db.requery.models.control_labels.RControlLabelsEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecision;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.exemplars.RExemplar;
import sapotero.rxtest.db.requery.models.exemplars.RExemplarEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
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
    convertNestedFields(entity, model, false);

    return entity;
  }

  @Override
  public DocumentInfo toModel(RDocumentEntity entity) {
    DocumentInfo model = new DocumentInfo();

    convertSimpleFields(model, entity);
    convertNestedFields(model, entity);

    return model;
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
      if ( notEmpty( model.getSigner().getOrganisation() ) ) {
        entity.setOrganization( model.getSigner().getOrganisation() );
      } else {
        entity.setOrganization( "Без организации" );
      }
    }
  }

  public void convertSimpleFields(DocumentInfo model, RDocumentEntity entity) {
    model.setUid( entity.getUid() );
    model.setMd5( entity.getMd5() );
    model.setSortKey( entity.getSortKey() );
    model.setTitle( entity.getTitle() );
    model.setRegistrationNumber( entity.getRegistrationNumber() );
    model.setRegistrationDate( entity.getRegistrationDate() );
    model.setUrgency( entity.getUrgency() );
    model.setShortDescription( entity.getShortDescription() );
    model.setComment( entity.getComment() );
    model.setExternalDocumentNumber( entity.getExternalDocumentNumber() );
    model.setReceiptDate( entity.getReceiptDate() );
    model.setViewed( entity.isViewed() );

    setSigner( model, entity.getSigner() );
  }

  public void convertNestedFields(RDocumentEntity entity, DocumentInfo model, boolean processed) {
    setDecisions(entity, model.getDecisions(), processed);
    setRoute(entity, model.getRoute());
    setExemplars(entity, model.getExemplars());
    setImages(entity, model.getImages());
    setControlLabels(entity, model.getControlLabels());
    setActions(entity, model.getActions());
    setLinks(entity, model.getLinks());
    setInfoCard(entity, model.getInfoCard());
  }

  public void convertNestedFields(DocumentInfo model, RDocumentEntity entity) {
    setDecisions(model, entity.getDecisions());
    setRoute(model, entity.getRoute());
    setExemplars(model, entity.getExemplars());
    setImages(model, entity.getImages());
    setControlLabels(model, entity.getControlLabels());
    setActions(model, entity.getActions());
    setLinks(model, entity.getLinks());
    setInfoCard(model, entity.getInfoCard());
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

  public void setSigner(DocumentInfo model, RSigner signer) {
    RSignerEntity signerEntity = (RSignerEntity) signer;

    if ( exist( signerEntity ) ) {
      Signer signerModel = new SignerMapper().toModel( signerEntity );
      model.setSigner( signerModel );
    }
  }

  public void setInfoCard(RDocumentEntity entity, String infoCard) {
    if ( exist( infoCard ) ) {
      entity.setInfoCard( infoCard );
    }
  }

  public void setInfoCard(DocumentInfo model, String infoCard) {
    if ( exist( infoCard ) ) {
      model.setInfoCard( infoCard );
    }
  }

  public void setLinks(RDocumentEntity entity, List<String> links) {
    if ( notEmpty( links ) ) {
      entity.getLinks().clear();
      LinkMapper linkMapper = new LinkMapper();

      for (String linkModel : links) {
        RLinksEntity linkEntity = linkMapper.toEntity( linkModel );
        linkEntity.setDocument( entity );
        entity.getLinks().add( linkEntity );
      }
    }
  }

  public void setLinks(DocumentInfo model, Set<RLinks> links) {
    if ( notEmpty( links ) ) {
      LinkMapper linkMapper = new LinkMapper();

      for (RLinks link : links) {
        RLinksEntity linksEntity = (RLinksEntity) link;
        String linkModel = linkMapper.toModel( linksEntity );
        model.getLinks().add( linkModel );
      }
    }
  }

  public void setControlLabels(RDocumentEntity entity, List<ControlLabel> controlLabels) {
    if ( notEmpty( controlLabels ) ) {
      entity.getControlLabels().clear();
      ControlLabelMapper controlLabelMapper = new ControlLabelMapper();

      for (ControlLabel labelModel : controlLabels ) {
        RControlLabelsEntity labelEntity = controlLabelMapper.toEntity( labelModel );
        labelEntity.setDocument( entity );
        entity.getControlLabels().add( labelEntity );
      }
    }
  }

  public void setControlLabels(DocumentInfo model, Set<RControlLabels> controlLabels) {
    if ( notEmpty( controlLabels ) ) {
      ControlLabelMapper controlLabelMapper = new ControlLabelMapper();

      for (RControlLabels label : controlLabels ) {
        RControlLabelsEntity labelEntity = (RControlLabelsEntity) label;
        ControlLabel labelModel = controlLabelMapper.toModel( labelEntity );
        model.getControlLabels().add( labelModel );
      }
    }
  }

  public void setImages(RDocumentEntity entity, List<Image> images) {
    if ( notEmpty( images ) ) {
      entity.getImages().clear();
      ImageMapper imageMapper = new ImageMapper();

      for (Image imageModel : images ) {
        RImageEntity imageEntity = imageMapper.toEntity( imageModel );
        imageEntity.setDocument( entity );
        entity.getImages().add( imageEntity );
      }
    }
  }

  public void setImages(DocumentInfo model, Set<RImage> images) {
    if ( notEmpty( images ) ) {
      ImageMapper imageMapper = new ImageMapper();

      for (RImage image : images ) {
        RImageEntity imageEntity = (RImageEntity) image;
        Image imageModel = imageMapper.toModel( imageEntity );
        model.getImages().add( imageModel );
      }
    }
  }

  public void setExemplars(RDocumentEntity entity, List<Exemplar> exemplars) {
    if ( notEmpty( exemplars ) ) {
      entity.getExemplars().clear();
      ExemplarMapper exemplarMapper = new ExemplarMapper();

      for (Exemplar exemplarModel : exemplars ) {
        RExemplarEntity exemplarEntity = exemplarMapper.toEntity( exemplarModel );
        exemplarEntity.setDocument( entity );
        entity.getExemplars().add( exemplarEntity );
      }
    }
  }

  public void setExemplars(DocumentInfo model, Set<RExemplar> exemplars) {
    if ( notEmpty( exemplars ) ) {
      ExemplarMapper exemplarMapper = new ExemplarMapper();

      for (RExemplar exemplar : exemplars ) {
        RExemplarEntity exemplarEntity = (RExemplarEntity) exemplar;
        Exemplar exemplarModel = exemplarMapper.toModel( exemplarEntity );
        model.getExemplars().add( exemplarModel );
      }
    }
  }

  public void setActions(RDocumentEntity entity, List<DocumentInfoAction> actions) {
    if ( notEmpty( actions ) ) {
      entity.getActions().clear();
      ActionMapper actionMapper = new ActionMapper();

      for (DocumentInfoAction actionModel : actions ) {
        RActionEntity actionEntity = actionMapper.toEntity( actionModel );
        actionEntity.setDocument( entity );
        entity.getActions().add( actionEntity );
      }
    }
  }

  public void setActions(DocumentInfo model, Set<RAction> actions) {
    if ( notEmpty( actions ) ) {
      ActionMapper actionMapper = new ActionMapper();

      for (RAction action : actions ) {
        RActionEntity actionEntity = (RActionEntity) action;
        DocumentInfoAction actionModel = actionMapper.toModel( actionEntity );
        model.getActions().add( actionModel );
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

  public void setRoute(DocumentInfo model, RRoute route) {
    if ( exist( route ) ) {
      RRouteEntity routeEntity = (RRouteEntity) route;
      Route routeModel = model.getRoute();
      routeModel.setTitle( routeEntity.getText() );
      StepMapper stepMapper = new StepMapper();

      for (RStep step : routeEntity.getSteps() ) {
        RStepEntity stepEntity = (RStepEntity) step;
        Step stepModel = stepMapper.toModel( stepEntity );
        routeModel.getSteps().add( stepModel );
      }
    }
  }

  public void setDecisions(RDocumentEntity entity, List<Decision> decisions, Boolean processed) {
    Boolean red = false;
    Boolean with_decision = false;

    if ( notEmpty( decisions ) ) {
      with_decision = true;
      entity.getDecisions().clear();
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

    if ( !processed ) {
      entity.setWithDecision( with_decision );
      entity.setRed( red );
    }
  }

  public void setDecisions(DocumentInfo model, Set<RDecision> decisions) {
    if ( notEmpty( decisions ) ) {
      DecisionMapper decisionMapper = new DecisionMapper();

      for (RDecision decision : decisions ) {
        RDecisionEntity decisionEntity = (RDecisionEntity) decision;
        Decision decisionModel = decisionMapper.toModel( decisionEntity );
        model.getDecisions().add( decisionModel );
      }
    }
  }

  public void updateProcessed(RDocumentEntity entity, String journal, String status, Fields.Status filter) {
    // если прилетело обновление - уберем из обработанных
    if ( filter != null && filter.getValue() != null && status != null && entity.isProcessed() ) {
      entity.setProcessed( false );
    }

    // если подписание/согласование
    if ( journal == null && entity.getDocumentType() == null && status != null && entity.isProcessed() ) {
      entity.setProcessed( false );
    }
  }
}

package sapotero.rxtest.db.mapper;

import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.retrofit.models.document.Performer;

// Maps between Performer and RPerformerEntity
public class PerformerMapper implements Mapper<Performer, RPerformerEntity> {

  @Override
  public RPerformerEntity toEntity(Performer model) {
    RPerformerEntity entity = new RPerformerEntity();

    entity.setNumber(model.getNumber());
    entity.setPerformerId(model.getPerformerId());
    entity.setPerformerType(model.getPerformerType());
    entity.setPerformerText(model.getPerformerText());
    entity.setPerformerGender(model.getPerformerGender());
    entity.setOrganizationText(model.getOrganizationText());
    entity.setIsOriginal(model.getIsOriginal());
    entity.setIsResponsible(model.getIsResponsible());
    entity.setIsOrganization(model.getOrganization());

    return entity;
  }

  @Override
  public Performer toModel(RPerformerEntity entity) {
    Performer model = new Performer();

    model.setNumber(entity.getNumber());
    model.setPerformerId(entity.getPerformerId());
    model.setPerformerType(entity.getPerformerType());
    model.setPerformerText(entity.getPerformerText());
    model.setPerformerGender(entity.getPerformerGender());
    model.setOrganizationText(entity.getOrganizationText());
    model.setIsOriginal(entity.isIsOriginal());
    model.setIsResponsible(entity.isIsResponsible());
    model.setOrganization(entity.isIsOrganization());

    return model;
  }

  public Performer toFormattedModel(RPerformerEntity entity) {
    Performer formattedModel = new Performer();

    formattedModel.setPerformerId( entity.getPerformerId() );
    formattedModel.setIsOriginal( entity.isIsOriginal() );
    formattedModel.setIsResponsible( entity.isIsResponsible() );
    formattedModel.setGroup( false );
    formattedModel.setOrganization( entity.isIsOrganization() );

    return formattedModel;
  }
}

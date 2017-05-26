package sapotero.rxtest.db.mapper;

import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.actions.RActionEntity;
import sapotero.rxtest.retrofit.models.document.DocumentInfoAction;
import sapotero.rxtest.utils.Settings;

// Maps between DocumentInfoAction and RActionEntity
public class ActionMapper extends AbstractMapper<DocumentInfoAction, RActionEntity> {

  public ActionMapper(Settings settings, Mappers mappers) {
    super(settings, mappers);
  }

  @Override
  public RActionEntity toEntity(DocumentInfoAction model) {
    RActionEntity entity = new RActionEntity();

    entity.setOfficialId(model.getOfficialId());
    entity.setAddressedToId(model.getAddressedToId());
    entity.setAction(model.getAction());
    entity.setActionDescription(model.getActionDescription());
    entity.setUpdatedAt(model.getUpdatedAt());
    entity.setToS(model.getToS());

    return entity;
  }

  @Override
  public DocumentInfoAction toModel(RActionEntity entity) {
    DocumentInfoAction model = new DocumentInfoAction();

    model.setOfficialId(entity.getOfficialId());
    model.setAddressedToId(entity.getAddressedToId());
    model.setAction(entity.getAction());
    model.setActionDescription(entity.getActionDescription());
    model.setUpdatedAt(entity.getUpdatedAt());
    model.setToS(entity.getToS());

    return model;
  }
}

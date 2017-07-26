package sapotero.rxtest.db.mapper;

import sapotero.rxtest.db.requery.models.RLinksEntity;

// Maps between String and RLinksEntity
// (model for RLinksEntity is String)
public class LinkMapper extends AbstractMapper<String, RLinksEntity> {

  @Override
  public RLinksEntity toEntity(String model) {
    RLinksEntity entity = new RLinksEntity();
    entity.setUid( model );
    return entity;
  }

  @Override
  public String toModel(RLinksEntity entity) {
    String model = entity.getUid();
    return model;
  }
}

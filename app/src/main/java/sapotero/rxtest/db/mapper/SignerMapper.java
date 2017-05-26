package sapotero.rxtest.db.mapper;

import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.retrofit.models.document.Signer;
import sapotero.rxtest.utils.Settings;

// Maps between Signer and RSignerEntity
public class SignerMapper extends AbstractMapper<Signer, RSignerEntity> {

  public SignerMapper(Settings settings, Mappers mappers) {
    super(settings, mappers);
  }

  @Override
  public RSignerEntity toEntity(Signer model) {
    RSignerEntity entity = new RSignerEntity();

    entity.setUid( model.getId() );
    entity.setName( model.getName() );
    entity.setOrganisation( model.getOrganisation() );
    entity.setType( model.getType() );

    return entity;
  }

  @Override
  public Signer toModel(RSignerEntity entity) {
    Signer model = new Signer();

    model.setId(entity.getUid());
    model.setName(entity.getName());
    model.setOrganisation(entity.getOrganisation());
    model.setType(entity.getType());

    return model;
  }
}

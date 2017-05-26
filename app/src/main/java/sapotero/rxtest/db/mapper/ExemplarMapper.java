package sapotero.rxtest.db.mapper;

import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.exemplars.RExemplarEntity;
import sapotero.rxtest.retrofit.models.document.Exemplar;
import sapotero.rxtest.utils.Settings;

// Maps between Exemplar and ExemplarEntity
public class ExemplarMapper extends AbstractMapper<Exemplar, RExemplarEntity> {

  public ExemplarMapper(Settings settings, Mappers mappers) {
    super(settings, mappers);
  }

  @Override
  public RExemplarEntity toEntity(Exemplar model) {
    RExemplarEntity entity = new RExemplarEntity();

    entity.setNumber(String.valueOf(model.getNumber()));
    entity.setIsOriginal(model.getIsOriginal());
    entity.setStatusCode(model.getStatusCode());
    entity.setAddressedToId(model.getAddressedToId());
    entity.setAddressedToName(model.getAddressedToName());
    entity.setDate(model.getDate());

    return entity;
  }

  @Override
  public Exemplar toModel(RExemplarEntity entity) {
    Exemplar model = new Exemplar();

    model.setNumber(Integer.valueOf(entity.getNumber()));
    model.setIsOriginal(entity.isIsOriginal());
    model.setStatusCode(entity.getStatusCode());
    model.setAddressedToId(entity.getAddressedToId());
    model.setAddressedToName(entity.getAddressedToName());
    model.setDate(entity.getDate());

    return model;
  }
}

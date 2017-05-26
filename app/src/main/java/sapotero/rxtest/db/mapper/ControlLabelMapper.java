package sapotero.rxtest.db.mapper;

import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.control_labels.RControlLabelsEntity;
import sapotero.rxtest.retrofit.models.document.ControlLabel;
import sapotero.rxtest.utils.Settings;

// Maps between ControlLabel and RControlLabelsEntity
public class ControlLabelMapper extends AbstractMapper<ControlLabel, RControlLabelsEntity> {

  public ControlLabelMapper(Settings settings, Mappers mappers) {
    super(settings, mappers);
  }

  @Override
  public RControlLabelsEntity toEntity(ControlLabel model) {
    RControlLabelsEntity entity = new RControlLabelsEntity();

    entity.setCreatedAt(model.getCreatedAt());
    entity.setOfficialId(model.getOfficialId());
    entity.setOfficialName(model.getOfficialName());
    entity.setSkippedOfficialId(model.getSkippedOfficialId());
    entity.setSkippedOfficialName(model.getSkippedOfficialName());
    entity.setState(model.getState());

    return entity;
  }

  @Override
  public ControlLabel toModel(RControlLabelsEntity entity) {
    ControlLabel model = new ControlLabel();

    model.setCreatedAt(entity.getCreatedAt());
    model.setOfficialId(entity.getOfficialId());
    model.setOfficialName(entity.getOfficialName());
    model.setSkippedOfficialId(entity.getSkippedOfficialId());
    model.setSkippedOfficialName(entity.getSkippedOfficialName());
    model.setState(entity.getState());

    return model;
  }
}

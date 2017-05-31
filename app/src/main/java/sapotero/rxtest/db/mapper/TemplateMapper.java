package sapotero.rxtest.db.mapper;

import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.retrofit.models.Template;
import sapotero.rxtest.utils.Settings;

// Maps between Template and RTemplateEntity
public class TemplateMapper extends AbstractMapper<Template, RTemplateEntity> {

  public TemplateMapper(Settings settings, Mappers mappers) {
    super(settings, mappers);
  }

  @Override
  public RTemplateEntity toEntity(Template model) {
    RTemplateEntity entity = new RTemplateEntity();

    entity.setUid(model.getId());
    entity.setTitle(model.getText());
    entity.setType(model.getType());
    entity.setUser(settings.getLogin());

    return entity;
  }

  @Override
  public Template toModel(RTemplateEntity entity) {
    Template model = new Template();

    model.setId(entity.getUid());
    model.setText(entity.getTitle());
    model.setType(entity.getType());

    return model;
  }
}

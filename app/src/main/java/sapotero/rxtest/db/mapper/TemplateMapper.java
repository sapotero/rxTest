package sapotero.rxtest.db.mapper;

import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.retrofit.models.Template;

// Maps between Template and RTemplateEntity
public class TemplateMapper extends AbstractMapper<Template, RTemplateEntity> {

  private String login = "";

  public TemplateMapper withLogin(String login) {
    this.login = login;
    return this;
  }

  @Override
  public RTemplateEntity toEntity(Template model) {
    RTemplateEntity entity = new RTemplateEntity();

    entity.setUid(model.getId());
    entity.setTitle(model.getText());
    entity.setType(model.getType());
    entity.setUser(login);

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

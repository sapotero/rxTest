package sapotero.rxtest.db.mapper;

import com.google.gson.Gson;

import sapotero.rxtest.db.requery.models.RManagerEntity;
import sapotero.rxtest.retrofit.models.Oshs;
import timber.log.Timber;

public class ManagerMapper extends AbstractMapper<Oshs, RManagerEntity> {

  private String login = "";

  @Override
  public RManagerEntity toEntity(Oshs model) {
    RManagerEntity entity = new RManagerEntity();

    Timber.e("RManagerEntity+++ %s", new Gson().toJson(model));

    model.setOrganization( entity.getOrganization() );
    model.setFirstName( entity.getFirstName() );
    model.setLastName( entity.getLastName() );
    model.setMiddleName( entity.getMiddleName() );
    model.setGender( entity.getGender() );
    model.setPosition( entity.getPosition() );
    model.setId( entity.getUid() );
    model.setName( entity.getName() );
    model.setIsGroup( entity.isIsGroup() );
    model.setIsOrganization( entity.isIsOrganization() );
    model.setImage( entity.getImage() );

    return entity;
  }

  @Override
  public Oshs toModel(RManagerEntity entity) {
    Oshs model = new Oshs();

    model.setOrganization( entity.getOrganization() );
    model.setFirstName( entity.getFirstName() );
    model.setLastName( entity.getLastName() );
    model.setMiddleName( entity.getMiddleName() );
    model.setGender( entity.getGender() );
    model.setPosition( entity.getPosition() );
    model.setId( entity.getUid() );
    model.setName( entity.getName() );
    model.setIsGroup( entity.isIsGroup() );
    model.setIsOrganization( entity.isIsOrganization() );
    model.setImage( entity.getImage() );

    return model;
  }

  public ManagerMapper withLogin(String login) {
    this.login = login;
    return this;
  }

}

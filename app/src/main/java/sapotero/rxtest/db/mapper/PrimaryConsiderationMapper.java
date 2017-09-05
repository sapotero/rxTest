package sapotero.rxtest.db.mapper;

import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.RPrimaryConsiderationEntity;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;

// Maps between RPrimaryConsideration, Oshs and PrimaryConsiderationPeople
// (model for RPrimaryConsideration is Oshs)
public class PrimaryConsiderationMapper extends AbstractMapper<Oshs, RPrimaryConsiderationEntity> {

  private Mappers mappers;
  private String login = "";

  public PrimaryConsiderationMapper(Mappers mappers) {
    this.mappers = mappers;
  }

  public PrimaryConsiderationMapper withLogin(String login) {
    this.login = login;
    return this;
  }

  @Override
  public RPrimaryConsiderationEntity toEntity(Oshs model) {
    RPrimaryConsiderationEntity entity = new RPrimaryConsiderationEntity();

    entity.setOrganization( model.getOrganization() );
    entity.setFirstName( model.getFirstName() );
    entity.setLastName( model.getLastName() );
    entity.setMiddleName( model.getMiddleName() );
    entity.setGender( model.getGender() );
    entity.setPosition( model.getPosition() );
    entity.setUid( model.getId() );
    entity.setName( model.getName() );
    entity.setIsGroup( model.getIsGroup() );
    entity.setIsOrganization( model.getIsOrganization() );
    entity.setUser( login );
    entity.setImage( model.getIImage() );

    return entity;
  }

  @Override
  public Oshs toModel(RPrimaryConsiderationEntity entity) {
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

  public PrimaryConsiderationPeople toPrimaryConsiderationPeople(RPrimaryConsiderationEntity entity) {
    Oshs model = toModel(entity);
    PrimaryConsiderationPeople people =
            (PrimaryConsiderationPeople) mappers.getPerformerMapper().convert(model, PerformerMapper.DestinationType.PRIMARYCONSIDERATIONPEOPLE);
    people.setSortIndex(entity.getSortIndex());
    return people;
  }
}

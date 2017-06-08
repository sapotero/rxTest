package sapotero.rxtest.db.mapper;

import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.RFavoriteUserEntity;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;

// Maps between RFavoriteUserEntity, Oshs and PrimaryConsiderationPeople
// (model for RFavoriteUserEntity is Oshs)
public class FavoriteUserMapper extends AbstractMapper<Oshs, RFavoriteUserEntity> {

  public FavoriteUserMapper(Settings settings, Mappers mappers) {
    super(settings, mappers);
  }

  @Override
  public RFavoriteUserEntity toEntity(Oshs model) {
    RFavoriteUserEntity entity = new RFavoriteUserEntity();

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
    entity.setUser( settings.getLogin() );

    return entity;
  }

  @Override
  public Oshs toModel(RFavoriteUserEntity entity) {
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

    return model;
  }

  public PrimaryConsiderationPeople toPrimaryConsiderationPeople(RFavoriteUserEntity entity) {
    Oshs model = toModel(entity);
    PrimaryConsiderationPeople people =
            (PrimaryConsiderationPeople) mappers.getPerformerMapper().convert(model, PerformerMapper.DestinationType.PRIMARYCONSIDERATIONPEOPLE);
    return people;
  }
}

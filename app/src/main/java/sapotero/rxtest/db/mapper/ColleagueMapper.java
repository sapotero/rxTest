package sapotero.rxtest.db.mapper;

import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.RColleagueEntity;
import sapotero.rxtest.retrofit.models.Colleague;
import sapotero.rxtest.utils.Settings;

// Maps between Colleague and RColleagueEntity
 public class ColleagueMapper extends AbstractMapper<Colleague, RColleagueEntity> {

  public ColleagueMapper(Settings settings, Mappers mappers) {
    super(settings, mappers);
  }

  @Override
  public RColleagueEntity toEntity(Colleague model) {
    RColleagueEntity entity = new RColleagueEntity();

    entity.setColleagueId( model.getColleagueId() );
    entity.setOfficialId( model.getOfficialId() );
    entity.setOfficialName( model.getOfficialName() );
    entity.setActived( model.getActived() );
    entity.setUser( settings.getLogin() );

    return entity;
  }

  @Override
  public Colleague toModel(RColleagueEntity entity) {
    Colleague model = new Colleague();

    model.setColleagueId( entity.getColleagueId() );
    model.setOfficialId( entity.getOfficialId() );
    model.setOfficialName( entity.getOfficialName() );
    model.setActived( entity.isActived() );

    return model;
  }
}

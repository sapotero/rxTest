package sapotero.rxtest.db.mapper;

import sapotero.rxtest.db.requery.models.RColleagueEntity;
import sapotero.rxtest.retrofit.models.Colleague;

// Maps between Colleague and RColleagueEntity
 public class ColleagueMapper extends AbstractMapper<Colleague, RColleagueEntity> {

  private String login = "";

  @Override
  public RColleagueEntity toEntity(Colleague model) {
    RColleagueEntity entity = new RColleagueEntity();

    entity.setColleagueId( model.getColleagueId() );
    entity.setOfficialId( model.getOfficialId() );
    entity.setOfficialName( model.getOfficialName() );
    entity.setActived( model.getActived() );
    entity.setUser( login );

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

  public ColleagueMapper withLogin(String login) {
    this.login = login;
    return this;
  }
}

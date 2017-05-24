package sapotero.rxtest.db.mapper;

import javax.inject.Inject;

import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RAssistantEntity;
import sapotero.rxtest.retrofit.models.Assistant;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;

// Maps between Assistant, RAssistantEntity and PrimaryConsiderationPeople
public class AssistantMapper extends AbstractMapper<Assistant, RAssistantEntity> {

  @Inject Settings settings;

  public AssistantMapper() {
    EsdApplication.getDataComponent().inject(this);
  }

  @Override
  public RAssistantEntity toEntity(Assistant model) {
    RAssistantEntity entity = new RAssistantEntity();

    entity.setTitle( model.getToS() );
    entity.setAssistantId( model.getAssistantId() );
    entity.setAssistantName( model.getAssistantName() );
    entity.setForDecision( model.getForDecision() );
    entity.setHeadId( model.getHeadId() );
    entity.setHeadName( model.getHeadName() );
    entity.setUser( settings.getLogin() );

    return entity;
  }

  @Override
  public Assistant toModel(RAssistantEntity entity) {
    Assistant model = new Assistant();

    model.setToS( entity.getTitle() );
    model.setAssistantId( entity.getAssistantId() );
    model.setAssistantName( entity.getAssistantName() );
    model.setForDecision( entity.isForDecision() );
    model.setHeadId( entity.getHeadId() );
    model.setHeadName( entity.getHeadName() );

    return model;
  }

  public PrimaryConsiderationPeople toPrimaryConsiderationPeople(RAssistantEntity entity) {
    PrimaryConsiderationPeople people = new PrimaryConsiderationPeople();

    people.setId( entity.getHeadId() );
    people.setName( entity.getTitle() );
    people.setPosition("");
    people.setOrganization("");
    people.setAssistantId( entity.getAssistantId() );
    people.setGender("");
    people.setIsOrganization( false );

    return people;
  }
}

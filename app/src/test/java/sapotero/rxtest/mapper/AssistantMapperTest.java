package sapotero.rxtest.mapper;

import org.junit.Before;
import org.junit.Test;

import sapotero.rxtest.db.mapper.AssistantMapper;
import sapotero.rxtest.db.requery.models.RAssistantEntity;
import sapotero.rxtest.retrofit.models.Assistant;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AssistantMapperTest {

  private AssistantMapper mapper;
  private Assistant dummyAssistant;
  private Integer dummySortIndex;
  private RAssistantEntity entity;
  private Assistant model;
  private PrimaryConsiderationPeople people;
  private String dummyLogin;

  @Before
  public void init() {
    generateAssistant();
    dummySortIndex = PrimaryConsiderationMapperTest.generateDummySortIndex();
  }

  private void generateAssistant() {
    dummyAssistant = new Assistant();
    dummyAssistant.setToS( "Сотрудник О. (ОДиР ГУ МВД России по Самарской области, Врио  Руководителя О.)" );
    dummyAssistant.setAssistantId( "57347f4673f700005b000001" );
    dummyAssistant.setAssistantName( "Сотрудник О." );
    dummyAssistant.setForDecision( false );
    dummyAssistant.setHeadId( "56eaaddb1372000002000001" );
    dummyAssistant.setHeadName( "Иванов И.И." );
    dummyLogin = "dummyLogin";
  }

  @Test
  public void toEntity() {
    mapper = new AssistantMapper();
    entity = mapper.withLogin(dummyLogin).toEntity(dummyAssistant);
    entity.setSortIndex( dummySortIndex );

    assertNotNull( entity );
    assertEquals( 0, entity.getId() );
    assertEquals( dummyAssistant.getToS(), entity.getTitle() );
    assertEquals( dummyAssistant.getAssistantId(), entity.getAssistantId() );
    assertEquals( dummyAssistant.getAssistantName(), entity.getAssistantName() );
    assertEquals( dummyAssistant.getForDecision(), entity.isForDecision() );
    assertEquals( dummyAssistant.getHeadId(), entity.getHeadId() );
    assertEquals( dummyAssistant.getHeadName(), entity.getHeadName() );
    assertEquals( dummyLogin, entity.getUser() );
    assertEquals( dummySortIndex, entity.getSortIndex() );
  }

  @Test
  public void toModel() {
    mapper = new AssistantMapper();
    entity = mapper.toEntity(dummyAssistant);
    model = mapper.toModel(entity);

    assertNotNull( model );
    assertEquals( dummyAssistant.getToS(), model.getToS() );
    assertEquals( dummyAssistant.getAssistantId(), model.getAssistantId() );
    assertEquals( dummyAssistant.getAssistantName(), model.getAssistantName() );
    assertEquals( dummyAssistant.getForDecision(), model.getForDecision() );
    assertEquals( dummyAssistant.getHeadId(), model.getHeadId() );
    assertEquals( dummyAssistant.getHeadName(), model.getHeadName() );
  }

  @Test
  public void toPrimaryConsiderationPeople() {
    mapper = new AssistantMapper();
    entity = mapper.toEntity(dummyAssistant);
    entity.setSortIndex(dummySortIndex);
    people = mapper.toPrimaryConsiderationPeople(entity);

    assertNotNull( people );
    assertEquals( dummyAssistant.getHeadId(), people.getId() );
    assertEquals( dummyAssistant.getToS(), people.getName() );
    assertEquals( "", people.getPosition() );
    assertEquals( "", people.getOrganization() );
    assertEquals( dummyAssistant.getAssistantId(), people.getAssistantId() );
    assertEquals( "", people.getGender() );
    assertEquals( false, people.isOrganization() );
    assertEquals( dummySortIndex, people.getSortIndex() );
  }

  @Test
  public void hasDiff() {
    mapper = new AssistantMapper();

    RAssistantEntity entity1 = mapper.toEntity(dummyAssistant);
    RAssistantEntity entity2 = mapper.toEntity(dummyAssistant);

    boolean hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setAssistantId("");
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }
}

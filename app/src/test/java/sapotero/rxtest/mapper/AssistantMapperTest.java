package sapotero.rxtest.mapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.inject.Inject;

import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.dagger.components.DaggerTestDataComponent;
import sapotero.rxtest.dagger.components.TestDataComponent;
import sapotero.rxtest.db.mapper.AssistantMapper;
import sapotero.rxtest.db.requery.models.RAssistantEntity;
import sapotero.rxtest.retrofit.models.Assistant;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.TestSettings;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ EsdApplication.class })
public class AssistantMapperTest {

  private TestDataComponent testDataComponent;
  private AssistantMapper mapper;
  private Assistant dummyAssistant;
  private Integer dummySortIndex;
  private RAssistantEntity entity;
  private Assistant model;
  private PrimaryConsiderationPeople people;

  @Inject ISettings settings;

  @Before
  public void init() {
    testDataComponent = DaggerTestDataComponent.builder().build();
    testDataComponent.inject(this);

    generateAssistant();
    dummySortIndex = PrimaryConsiderationMapperTest.generateDummySortIndex();

    PowerMockito.mockStatic(EsdApplication.class);
    PowerMockito.when(EsdApplication.getDataComponent()).thenReturn(testDataComponent);
  }

  private void generateAssistant() {
    dummyAssistant = new Assistant();
    dummyAssistant.setToS( "Сотрудник О. (ОДиР ГУ МВД России по Самарской области, Врио  Руководителя О.)" );
    dummyAssistant.setAssistantId( "57347f4673f700005b000001" );
    dummyAssistant.setAssistantName( "Сотрудник О." );
    dummyAssistant.setForDecision( false );
    dummyAssistant.setHeadId( "56eaaddb1372000002000001" );
    dummyAssistant.setHeadName( "Иванов И.И." );
  }

  @Test
  public void toEntity() {
    mapper = new AssistantMapper();
    entity = mapper.toEntity(dummyAssistant);

    PowerMockito.verifyStatic(times(1));
    EsdApplication.getDataComponent();

    Mockito.verify(settings, times(1)).getLogin();

    assertNotNull( entity );
    assertEquals( 0, entity.getId() );
    assertEquals( dummyAssistant.getToS(), entity.getTitle() );
    assertEquals( dummyAssistant.getAssistantId(), entity.getAssistantId() );
    assertEquals( dummyAssistant.getAssistantName(), entity.getAssistantName() );
    assertEquals( dummyAssistant.getForDecision(), entity.isForDecision() );
    assertEquals( dummyAssistant.getHeadId(), entity.getHeadId() );
    assertEquals( dummyAssistant.getHeadName(), entity.getHeadName() );
    assertEquals( ((TestSettings) settings).login, entity.getUser() );
  }

  @Test
  public void toModel() {
    mapper = new AssistantMapper();
    entity = mapper.toEntity(dummyAssistant);
    model = mapper.toModel(entity);

    PowerMockito.verifyStatic(times(1));
    EsdApplication.getDataComponent();

    Mockito.verify(settings, times(1)).getLogin();

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

    PowerMockito.verifyStatic(times(1));
    EsdApplication.getDataComponent();

    Mockito.verify(settings, times(1)).getLogin();

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

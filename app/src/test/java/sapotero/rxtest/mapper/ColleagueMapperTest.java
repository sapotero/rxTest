package sapotero.rxtest.mapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.dagger.components.DaggerTestDataComponent;
import sapotero.rxtest.dagger.components.TestDataComponent;
import sapotero.rxtest.db.mapper.ColleagueMapper;
import sapotero.rxtest.db.requery.models.RColleagueEntity;
import sapotero.rxtest.retrofit.models.Colleague;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ EsdApplication.class })
public class ColleagueMapperTest {

  private TestDataComponent testDataComponent;
  private ColleagueMapper mapper;
  private Colleague dummyColleague;
  private Integer dummySortIndex;
  private RColleagueEntity entity;
  private Colleague model;
  private String dummyLogin;

  @Before
  public void init() {
    testDataComponent = DaggerTestDataComponent.builder().build();
    testDataComponent.inject(this);

    generateColleague();
    dummySortIndex = PrimaryConsiderationMapperTest.generateDummySortIndex();

    PowerMockito.mockStatic(EsdApplication.class);
    PowerMockito.when(EsdApplication.getDataComponent()).thenReturn(testDataComponent);
  }

  private void generateColleague() {
    dummyColleague = new Colleague();
    dummyColleague.setColleagueId( "58b845ed24b40001b3000001" );
    dummyColleague.setOfficialId( "56eaaddb1372000002000001" );
    dummyColleague.setOfficialName( "Руководитель О. (ОДиР ГУ МВД России по Самарской области, Начальник)" );
    dummyColleague.setActived( true );
    dummyLogin = "dummyLogin";
  }

  @Test
  public void toEntity() {
    mapper = new ColleagueMapper();
    entity = mapper.withLogin(dummyLogin).toEntity(dummyColleague);

    assertNotNull( entity );
    assertEquals( 0, entity.getId() );
    assertEquals( dummyColleague.getColleagueId(), entity.getColleagueId() );
    assertEquals( dummyColleague.getOfficialId(), entity.getOfficialId() );
    assertEquals( dummyColleague.getOfficialName(), entity.getOfficialName() );
    assertEquals( dummyColleague.getActived(), entity.isActived() );
    assertEquals( dummyLogin, entity.getUser() );
  }

  @Test
  public void toModel() {
    mapper = new ColleagueMapper();
    entity = mapper.toEntity(dummyColleague);
    model = mapper.toModel(entity);

    assertNotNull( model );
    assertEquals( dummyColleague.getColleagueId(), model.getColleagueId() );
    assertEquals( dummyColleague.getOfficialId(), model.getOfficialId() );
    assertEquals( dummyColleague.getOfficialName(), model.getOfficialName() );
    assertEquals( dummyColleague.getActived(), model.getActived() );
  }

  @Test
  public void hasDiff() {
    mapper = new ColleagueMapper();

    RColleagueEntity entity1 = mapper.toEntity(dummyColleague);
    RColleagueEntity entity2 = mapper.toEntity(dummyColleague);

    boolean hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setColleagueId("");
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }
}

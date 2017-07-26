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
import sapotero.rxtest.db.mapper.TemplateMapper;
import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.retrofit.models.Template;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.TestSettings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ EsdApplication.class })
public class TemplateMapperTest {

  private TestDataComponent testDataComponent;
  private TemplateMapper mapper;
  private Template dummyTemplate;
  private RTemplateEntity entity;
  private Template model;

  @Inject ISettings settings;

  @Before
  public void init() {
    testDataComponent = DaggerTestDataComponent.builder().build();
    testDataComponent.inject(this);

    generateTemplate();

    PowerMockito.mockStatic(EsdApplication.class);
    PowerMockito.when(EsdApplication.getDataComponent()).thenReturn(testDataComponent);
  }

  private void generateTemplate() {
    dummyTemplate = new Template();
    dummyTemplate.setId( "56ebb7b45d2d000091000001" );
    dummyTemplate.setText( "Документ на рассмотрение для всех сотрудников" );
    dummyTemplate.setType( "dummyType" );
  }

  @Test
  public void toEntity() {
    mapper = new TemplateMapper();
    entity = mapper.toEntity(dummyTemplate);

    PowerMockito.verifyStatic(times(1));
    EsdApplication.getDataComponent();

    Mockito.verify(settings, times(1)).getLogin();

    assertNotNull( entity );
    assertEquals( 0, entity.getId() );
    assertEquals( dummyTemplate.getId(), entity.getUid() );
    assertEquals( dummyTemplate.getText(), entity.getTitle() );
    assertEquals( dummyTemplate.getType(), entity.getType() );
    assertEquals( ((TestSettings) settings).login, entity.getUser() );
  }

  @Test
  public void toModel() {
    mapper = new TemplateMapper();
    entity = mapper.toEntity(dummyTemplate);
    model = mapper.toModel(entity);

    PowerMockito.verifyStatic(times(1));
    EsdApplication.getDataComponent();

    Mockito.verify(settings, times(1)).getLogin();

    assertNotNull( model );
    assertEquals( dummyTemplate.getId(), model.getId() );
    assertEquals( dummyTemplate.getText(), model.getText() );
    assertEquals( dummyTemplate.getType(), model.getType() );
  }

  @Test
  public void hasDiff() {
    mapper = new TemplateMapper();

    RTemplateEntity entity1 = mapper.toEntity(dummyTemplate);
    RTemplateEntity entity2 = mapper.toEntity(dummyTemplate);

    boolean hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setUid("");
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }
}

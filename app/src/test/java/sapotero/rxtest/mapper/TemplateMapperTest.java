package sapotero.rxtest.mapper;

import org.junit.Before;
import org.junit.Test;

import sapotero.rxtest.db.mapper.TemplateMapper;
import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.retrofit.models.Template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TemplateMapperTest {

  private TemplateMapper mapper;
  private Template dummyTemplate;
  private RTemplateEntity entity;
  private Template model;
  private String dummyLogin;

  @Before
  public void init() {
    generateTemplate();
  }

  private void generateTemplate() {
    dummyTemplate = new Template();
    dummyTemplate.setId( "56ebb7b45d2d000091000001" );
    dummyTemplate.setText( "Документ на рассмотрение для всех сотрудников" );
    dummyTemplate.setType( "dummyType" );
    dummyLogin = "dummyLogin";
  }

  @Test
  public void toEntity() {
    mapper = new TemplateMapper();
    entity = mapper.withLogin(dummyLogin).toEntity(dummyTemplate);

    assertNotNull( entity );
    assertEquals( 0, entity.getId() );
    assertEquals( dummyTemplate.getId(), entity.getUid() );
    assertEquals( dummyTemplate.getText(), entity.getTitle() );
    assertEquals( dummyTemplate.getType(), entity.getType() );
  }

  @Test
  public void toModel() {
    mapper = new TemplateMapper();
    entity = mapper.toEntity(dummyTemplate);
    model = mapper.toModel(entity);

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

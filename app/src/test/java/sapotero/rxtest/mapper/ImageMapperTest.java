package sapotero.rxtest.mapper;

import org.junit.Before;
import org.junit.Test;

import sapotero.rxtest.db.mapper.ImageMapper;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.retrofit.models.document.Image;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ImageMapperTest {

  private ImageMapper mapper;
  private Image dummyImage;
  private RImageEntity entity;
  private Image model;

  @Before
  public void init() {
    generateImage();
  }

  private void generateImage(){
    String title       = "1538 от 21.04.2017";
    String md5         = "0288415775b0295cf5e475221a04989c6088ece4eb1b13010f27fff31ca0c56a64";
    String path        = "/documents/0288415775b0295cf5e475221a04989c6088ece4eb1b13010f27fff31ca0c56a64?show_operations=true";
    String contentType = "pdf";
    String createdAt   = "23 марта 2016 г.";
    Integer number     = 1;
    Integer size       = 654658;
    Boolean signed     = Math.random() > 0.5;

    dummyImage = new Image();
    dummyImage.setTitle(title);
    dummyImage.setNumber(number);
    dummyImage.setMd5(md5);
    dummyImage.setSize(size);
    dummyImage.setPath(path);
    dummyImage.setContentType(contentType);
    dummyImage.setSigned(signed);
    dummyImage.setCreatedAt(createdAt);
  }

  @Test
  public void toEntity() {
    mapper = new ImageMapper();
    entity = mapper.toEntity(dummyImage);

    assertNotNull( entity );
    assertEquals( 0                       , entity.getId() );
    assertEquals( entity.getNumber()      , dummyImage.getNumber() );
    assertEquals( entity.getTitle()       , dummyImage.getTitle() );
    assertEquals( entity.getMd5()         , dummyImage.getMd5() );
    assertEquals( entity.getSize()        , dummyImage.getSize() );
    assertEquals( entity.getPath()        , dummyImage.getPath() );
    assertEquals( entity.getContentType() , dummyImage.getContentType() );
    assertEquals( entity.isSigned()       , dummyImage.getSigned() );
    assertEquals( entity.getCreatedAt()   , dummyImage.getCreatedAt() );
    assertEquals( entity.isLoading()      , false );
    assertEquals( entity.isComplete()     , false );
    assertEquals( entity.isError()        , false );
    assertEquals( entity.isDeleted()      , false );
  }

  @Test
  public void toModel(){
    mapper = new ImageMapper();
    entity = mapper.toEntity(dummyImage);
    model = mapper.toModel(entity);

    assertNotNull( model );
    assertEquals( entity.getNumber()      , model.getNumber() );
    assertEquals( entity.getTitle()       , model.getTitle() );
    assertEquals( entity.getMd5()         , model.getMd5() );
    assertEquals( entity.getSize()        , model.getSize() );
    assertEquals( entity.getPath()        , model.getPath() );
    assertEquals( entity.getContentType() , model.getContentType() );
    assertEquals( entity.isSigned()       , model.getSigned() );
    assertEquals( entity.getCreatedAt()   , model.getCreatedAt() );
  }

  @Test
  public void hasDiff() {
    mapper = new ImageMapper();

    RImageEntity entity1 = mapper.toEntity(dummyImage);
    RImageEntity entity2 = mapper.toEntity(dummyImage);

    boolean hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setNumber( 7 );
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }
}

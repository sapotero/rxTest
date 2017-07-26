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
    dummyImage = generateImage();
  }

  public static Image generateImage() {
    String title       = "1538 от 21.04.2017";
    String md5         = "0288415775b0295cf5e475221a04989c6088ece4eb1b13010f27fff31ca0c56a64";
    String path        = "/documents/0288415775b0295cf5e475221a04989c6088ece4eb1b13010f27fff31ca0c56a64?show_operations=true";
    String contentType = "pdf";
    String createdAt   = "23 марта 2016 г.";
    Integer number     = 1;
    Integer size       = 654658;
    Boolean signed     = Math.random() > 0.5;

    Image dummyImage = new Image();
    dummyImage.setTitle(title);
    dummyImage.setNumber(number);
    dummyImage.setMd5(md5);
    dummyImage.setSize(size);
    dummyImage.setPath(path);
    dummyImage.setContentType(contentType);
    dummyImage.setSigned(signed);
    dummyImage.setCreatedAt(createdAt);

    return dummyImage;
  }

  @Test
  public void toEntity() {
    mapper = new ImageMapper();
    entity = mapper.toEntity(dummyImage);

    verifyImage( dummyImage, entity );
  }

  public static void verifyImage(Image expected, RImageEntity actual) {
    assertNotNull( actual );
    assertEquals( 0                          , actual.getId() );
    assertEquals( expected.getNumber()       , actual.getNumber() );
    assertEquals( expected.getTitle()        , actual.getTitle() );
    assertEquals( expected.getMd5()          , actual.getMd5() );
    assertEquals( expected.getSize()         , actual.getSize() );
    assertEquals( expected.getPath()         , actual.getPath() );
    assertEquals( expected.getContentType()  , actual.getContentType() );
    assertEquals( expected.getSigned()       , actual.isSigned() );
    assertEquals( expected.getCreatedAt()    , actual.getCreatedAt() );
    assertEquals( false                      , actual.isLoading() );
    assertEquals( false                      , actual.isComplete() );
    assertEquals( false                      , actual.isError() );
    assertEquals( false                      , actual.isDeleted() );
  }

  @Test
  public void toModel(){
    mapper = new ImageMapper();
    entity = mapper.toEntity(dummyImage);
    model = mapper.toModel(entity);

    verifyImage( dummyImage, model );
  }

  public static void verifyImage(Image expected, Image actual) {
    assertNotNull( actual );
    assertEquals( expected.getNumber()      , actual.getNumber() );
    assertEquals( expected.getTitle()       , actual.getTitle() );
    assertEquals( expected.getMd5()         , actual.getMd5() );
    assertEquals( expected.getSize()        , actual.getSize() );
    assertEquals( expected.getPath()        , actual.getPath() );
    assertEquals( expected.getContentType() , actual.getContentType() );
    assertEquals( expected.getSigned()      , actual.getSigned() );
    assertEquals( expected.getCreatedAt()   , actual.getCreatedAt() );
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

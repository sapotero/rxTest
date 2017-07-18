package sapotero.rxtest.mapper;

import android.content.Context;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import sapotero.rxtest.db.mapper.ImageMapper;
import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.utils.Settings;

public class ImageMapperTest {

  @Mock private Context context;
  @Mock private ImageMapper mapper;
  @Mock private Mappers mappers;
  @Mock private Settings settings;
  @Mock private Image image;
  @Mock private RImageEntity imageModel;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    mapper = new ImageMapper(settings, mappers);
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

    image = new Image();
    image.setTitle(title);
    image.setNumber(number);
    image.setMd5(md5);
    image.setSize(size);
    image.setPath(path);
    image.setContentType(contentType);
    image.setSigned(signed);
    image.setCreatedAt(createdAt);
    image.setPath(path);
  }

  @Test
  public void ToEntity() {
    imageModel = mapper.toEntity(image);

    Assert.assertEquals( imageModel.getNumber()      , image.getNumber() );
    Assert.assertEquals( imageModel.getTitle()       , image.getTitle() );
    Assert.assertEquals( imageModel.getMd5()         , image.getMd5() );
    Assert.assertEquals( imageModel.getSize()        , image.getSize() );
    Assert.assertEquals( imageModel.getPath()        , image.getPath() );
    Assert.assertEquals( imageModel.getContentType() , image.getContentType() );
    Assert.assertEquals( imageModel.isSigned()       , image.getSigned() );
    Assert.assertEquals( imageModel.getCreatedAt()   , image.getCreatedAt() );
    Assert.assertEquals( imageModel.isLoading()      , false );
    Assert.assertEquals( imageModel.isComplete()     , false );
    Assert.assertEquals( imageModel.isError()        , false );
    Assert.assertEquals( imageModel.isDeleted()      , false );
  }

  @Test
  public void ToModel(){
    Image temp_model = mapper.toModel(imageModel);

    Assert.assertEquals( imageModel.getNumber()      , temp_model.getNumber() );
    Assert.assertEquals( imageModel.getTitle()       , temp_model.getTitle() );
    Assert.assertEquals( imageModel.getMd5()         , temp_model.getMd5() );
    Assert.assertEquals( imageModel.getSize()        , temp_model.getSize() );
    Assert.assertEquals( imageModel.getPath()        , temp_model.getPath() );
    Assert.assertEquals( imageModel.getContentType() , temp_model.getContentType() );
    Assert.assertEquals( imageModel.isSigned()       , temp_model.getSigned() );
    Assert.assertEquals( imageModel.getCreatedAt()   , temp_model.getCreatedAt() );
  }

}

package sapotero.rxtest.mapper;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import sapotero.rxtest.db.mapper.BlockMapper;
import sapotero.rxtest.db.mapper.PerformerMapper;
import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformer;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Performer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;

public class BlockMapperTest {

  private BlockMapper mapper;
  private PerformerMapper performerMapper;
  private Block dummyBlock;
  private Performer dummyPerformer;
  private RBlockEntity entity;
  private Block model;

  @Mock Mappers mappers;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    performerMapper = new PerformerMapper();

    generateBlock();

    Mockito.when(mappers.getPerformerMapper()).thenReturn(performerMapper);
  }

  private void generateBlock() {
    dummyBlock = new Block();
    dummyBlock.setId( "Ij3hf834y7f4iEhfweihfeRfhjw9823ryu98" );
    dummyBlock.setNumber( 5 );
    dummyBlock.setText( "Уылдаовы влдыоа выд олдвыао ва" );
    dummyBlock.setAppealText( "Прошу доложить" );
    dummyBlock.setTextBefore( false );
    dummyBlock.setHidePerformers( false );
    dummyBlock.setFontSize( "10" );
    dummyBlock.setToCopy( false );
    dummyBlock.setToFamiliarization( false );

    dummyPerformer = PerformerMapperTest.generatePerformer();
    this.dummyBlock.getPerformers().add( dummyPerformer );
  }

  @Test
  public void toEntity() {
    mapper = new BlockMapper(mappers);
    entity = mapper.toEntity(dummyBlock);

    Mockito.verify(mappers, times(1)).getPerformerMapper();

    assertNotNull( entity );
    assertEquals( 0, entity.getId() );
    assertEquals( dummyBlock.getId(), entity.getUid() );
    assertEquals( dummyBlock.getNumber(), entity.getNumber() );
    assertEquals( dummyBlock.getText(), entity.getText() );
    assertEquals( dummyBlock.getAppealText(), entity.getAppealText() );
    assertEquals( dummyBlock.getTextBefore(), entity.isTextBefore() );
    assertEquals( dummyBlock.getHidePerformers(), entity.isHidePerformers() );
    assertEquals( dummyBlock.getFontSize(), entity.getFontSize() );
    assertEquals( dummyBlock.getToCopy(), entity.isToCopy() );
    assertEquals( dummyBlock.getToFamiliarization(), entity.isToFamiliarization() );

    for (RPerformer _performer : entity.getPerformers() ) {
      RPerformerEntity performerEntity = (RPerformerEntity) _performer;
      PerformerMapperTest.verifyPerformer( dummyPerformer, performerEntity );
    }
  }

  @Test
  public void toModel() {
    mapper = new BlockMapper(mappers);
    entity = mapper.toEntity(dummyBlock);
    model = mapper.toModel(entity);

    Mockito.verify(mappers, times(2)).getPerformerMapper();

    assertNotNull( model );
    assertEquals( dummyBlock.getId(), model.getId() );
    assertEquals( dummyBlock.getNumber(), model.getNumber() );
    assertEquals( dummyBlock.getText(), model.getText() );
    assertEquals( dummyBlock.getAppealText(), model.getAppealText() );
    assertEquals( dummyBlock.getTextBefore(), model.getTextBefore() );
    assertEquals( dummyBlock.getHidePerformers(), model.getHidePerformers() );
    assertEquals( dummyBlock.getFontSize(), model.getFontSize() );
    assertEquals( dummyBlock.getToCopy(), model.getToCopy() );
    assertEquals( dummyBlock.getToFamiliarization(), model.getToFamiliarization() );

    for (Performer performer : this.model.getPerformers() ) {
      PerformerMapperTest.verifyPerformer( dummyPerformer, performer );
    }
  }

  @Test
  public void hasDiff() {
    mapper = new BlockMapper(mappers);

    RBlockEntity entity1 = mapper.toEntity(dummyBlock);
    RBlockEntity entity2 = mapper.toEntity(dummyBlock);

    boolean hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setUid("");
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }
}

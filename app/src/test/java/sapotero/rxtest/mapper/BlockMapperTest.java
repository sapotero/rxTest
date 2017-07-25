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
  private RBlockEntity entity;
  private Block model;

  @Mock Mappers mappers;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    performerMapper = new PerformerMapper();

    dummyBlock = generateBlock();

    Mockito.when(mappers.getPerformerMapper()).thenReturn(performerMapper);
  }

  public static Block generateBlock() {
    Block dummyBlock = new Block();
    dummyBlock.setId( "Ij3hf834y7f4iEhfweihfeRfhjw9823ryu98" );
    dummyBlock.setNumber( 5 );
    dummyBlock.setText( "Уылдаовы влдыоа выд олдвыао ва" );
    dummyBlock.setAppealText( "Прошу доложить" );
    dummyBlock.setTextBefore( false );
    dummyBlock.setHidePerformers( false );
    dummyBlock.setFontSize( "10" );
    dummyBlock.setToCopy( false );
    dummyBlock.setToFamiliarization( false );

    Performer dummyPerformer = PerformerMapperTest.generatePerformer();
    dummyBlock.getPerformers().add( dummyPerformer );
    return dummyBlock;
  }

  @Test
  public void toEntity() {
    mapper = new BlockMapper(mappers);
    entity = mapper.toEntity(dummyBlock);

    Mockito.verify(mappers, times(1)).getPerformerMapper();

    verifyBlock( dummyBlock, entity );
  }

  public static void verifyBlock(Block expected, RBlockEntity actual) {
    assertNotNull( actual );
    assertEquals( 0, actual.getId() );
    assertEquals( expected.getId(), actual.getUid() );
    assertEquals( expected.getNumber(), actual.getNumber() );
    assertEquals( expected.getText(), actual.getText() );
    assertEquals( expected.getAppealText(), actual.getAppealText() );
    assertEquals( expected.getTextBefore(), actual.isTextBefore() );
    assertEquals( expected.getHidePerformers(), actual.isHidePerformers() );
    assertEquals( expected.getFontSize(), actual.getFontSize() );
    assertEquals( expected.getToCopy(), actual.isToCopy() );
    assertEquals( expected.getToFamiliarization(), actual.isToFamiliarization() );

    int index = 0;
    for (RPerformer _performer : actual.getPerformers() ) {
      RPerformerEntity performerEntity = (RPerformerEntity) _performer;
      PerformerMapperTest.verifyPerformer( expected.getPerformers().get(index), performerEntity );
      index++;
    }
  }

  @Test
  public void toModel() {
    mapper = new BlockMapper(mappers);
    entity = mapper.toEntity(dummyBlock);
    model = mapper.toModel(entity);

    Mockito.verify(mappers, times(2)).getPerformerMapper();

    verifyBlock( dummyBlock, model );
  }

  public static void verifyBlock(Block expected, Block actual) {
    assertNotNull( actual );
    assertEquals( expected.getId(), actual.getId() );
    assertEquals( expected.getNumber(), actual.getNumber() );
    assertEquals( expected.getText(), actual.getText() );
    assertEquals( expected.getAppealText(), actual.getAppealText() );
    assertEquals( expected.getTextBefore(), actual.getTextBefore() );
    assertEquals( expected.getHidePerformers(), actual.getHidePerformers() );
    assertEquals( expected.getFontSize(), actual.getFontSize() );
    assertEquals( expected.getToCopy(), actual.getToCopy() );
    assertEquals( expected.getToFamiliarization(), actual.getToFamiliarization() );

    int index = 0;
    for (Performer performer : actual.getPerformers() ) {
      PerformerMapperTest.verifyPerformer( expected.getPerformers().get(index), performer );
      index++;
    }
  }

  public static void verifyFormattedBlock(Block expected, Block actual) {
    assertNotNull( actual );
    assertEquals( expected.getId(), actual.getId() );
    assertEquals( expected.getNumber(), actual.getNumber() );
    assertEquals( expected.getText(), actual.getText() );
    assertEquals( expected.getAppealText(), actual.getAppealText() );
    assertEquals( expected.getTextBefore(), actual.getTextBefore() );
    assertEquals( expected.getHidePerformers(), actual.getHidePerformers() );
    assertEquals( "14", actual.getFontSize() );
    assertEquals( "0", actual.getIndentation() );
    assertEquals( null, actual.getToCopy() );
    assertEquals( null, actual.getToFamiliarization() );

    int index = 0;
    for (Performer performer : actual.getPerformers() ) {
      PerformerMapperTest.verifyFormattedPerformer( expected.getPerformers().get(index), performer );
      index++;
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

package sapotero.rxtest.mapper;


import org.junit.Before;
import org.junit.Test;

import sapotero.rxtest.db.mapper.BlockMapper;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformer;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Performer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BlockMapperTest {

  private BlockMapper mapper;
  private Block dummyBlock;
  private RBlockEntity entity;
  private Block model;

  @Before
  public void init() {
    dummyBlock = generateBlock();
  }

  public static Block generateBlock() {
    Block dummyBlock = new Block();
    dummyBlock.setId( "5968dce312dd000028000001" );
    dummyBlock.setNumber( 5 );
    dummyBlock.setText( "Уылдаовы влдыоа выд олдвыао ва" );
    dummyBlock.setAppealText( "Прошу доложить" );
    dummyBlock.setTextBefore( false );
    dummyBlock.setHidePerformers( false );
    dummyBlock.setFontSize( "8" );
    dummyBlock.setToCopy( false );
    dummyBlock.setToFamiliarization( false );

    Performer dummyPerformer = PerformerMapperTest.generatePerformer();
    dummyBlock.getPerformers().add( dummyPerformer );
    return dummyBlock;
  }

  @Test
  public void toEntity() {
    mapper = new BlockMapper();
    entity = mapper.toEntity(dummyBlock);

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
    mapper = new BlockMapper();
    entity = mapper.toEntity(dummyBlock);
    model = mapper.toModel(entity);

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
    assertEquals( "5", actual.getIndentation() );
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
    mapper = new BlockMapper();

    RBlockEntity entity1 = mapper.toEntity(dummyBlock);
    RBlockEntity entity2 = mapper.toEntity(dummyBlock);

    boolean hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setUid("");
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }
}

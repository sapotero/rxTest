package sapotero.rxtest.mapper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import sapotero.rxtest.db.mapper.BlockMapper;
import sapotero.rxtest.db.mapper.DecisionMapper;
import sapotero.rxtest.db.mapper.PerformerMapper;
import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.decisions.RBlock;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;

public class DecisionMapperTest {

  private DecisionMapper mapper;
  private BlockMapper blockMapper;
  private PerformerMapper performerMapper;
  private Decision dummyDecision;
  private RDecisionEntity entity;
  private Decision model;
  private Decision formattedModel;

  @Mock Mappers mappers;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    performerMapper = new PerformerMapper();
    blockMapper = new BlockMapper(mappers);

    dummyDecision = generateDecision();

    Mockito.when(mappers.getPerformerMapper()).thenReturn(performerMapper);
    Mockito.when(mappers.getBlockMapper()).thenReturn(blockMapper);
  }

  public static Decision generateDecision() {
    Decision dummyDecision = new Decision();
    dummyDecision.setId( "G546olierufih8EDE4erD34fdg" );
    dummyDecision.setLetterhead( "Бланк резолюции" );
    dummyDecision.setApproved( false );
    dummyDecision.setSigner( "Сотрудник_а1 A.T. (ОДиР ГУ МВД России по Самарской области)" );
    dummyDecision.setSignerId( "58f88dfc776b000026000402" );
    dummyDecision.setSignerBlankText( "Сотрудник_а1 A.T." );
    dummyDecision.setSignerIsManager( false );
    dummyDecision.setSignerPositionS( "Сотрудник ОДИР" );
    dummyDecision.setAssistantId( "kjhwf78&HJJ3eg43f" );
    dummyDecision.setComment( "kjh89 23sdjf23n 2389dskjf slsdkjfsdj" );
    dummyDecision.setDate( "2017-07-24" );
    dummyDecision.setUrgencyText( "Весьма срочно" );
    dummyDecision.setShowPosition( true );
    dummyDecision.setSignBase64( null );
    dummyDecision.setRed( false );
    dummyDecision.setLetterheadFontSize( "14" );
    dummyDecision.setPerformersFontSize( "10" );

    Block dummyBlock = BlockMapperTest.generateBlock();
    dummyDecision.getBlocks().add(dummyBlock);

    return dummyDecision;
  }

  @Test
  public void toEntity() {
    mapper = new DecisionMapper(mappers);
    entity = mapper.toEntity(dummyDecision);

    Mockito.verify(mappers, times(1)).getPerformerMapper();
    Mockito.verify(mappers, times(1)).getBlockMapper();

    verifyDecision( dummyDecision, entity );
  }

  public static void verifyDecision(Decision expected, RDecisionEntity actual) {
    assertNotNull( actual );
    assertEquals( 0, actual.getId() );
    assertEquals( false, actual.isTemporary() );
    assertEquals( expected.getId(), actual.getUid() );
    assertEquals( expected.getLetterhead(), actual.getLetterhead() );
    assertEquals( expected.getApproved(), actual.isApproved() );
    assertEquals( expected.getSigner(), actual.getSigner() );
    assertEquals( expected.getSignerId(), actual.getSignerId() );
    assertEquals( expected.getSignerBlankText(), actual.getSignerBlankText() );
    assertEquals( expected.getSignerIsManager(), actual.isSignerIsManager() );
    assertEquals( expected.getSignerPositionS(), actual.getSignerPositionS() );
    assertEquals( expected.getAssistantId(), actual.getAssistantId() );
    assertEquals( expected.getComment(), actual.getComment() );
    assertEquals( expected.getDate(), actual.getDate() );
    assertEquals( expected.getUrgencyText(), actual.getUrgencyText() );
    assertEquals( expected.getShowPosition(), actual.isShowPosition() );
    assertEquals( expected.getSignBase64(), actual.getSignBase64() );
    assertEquals( expected.getRed(), actual.isRed() );
    assertEquals( expected.getLetterheadFontSize(), actual.getLetterheadFontSize() );
    assertEquals( expected.getPerformersFontSize(), actual.getPerformerFontSize() );

    int index = 0;
    for (RBlock _block : actual.getBlocks() ) {
      RBlockEntity blockEntity = (RBlockEntity) _block;
      BlockMapperTest.verifyBlock( expected.getBlocks().get(index), blockEntity );
      index++;
    }
  }

  @Test
  public void toModel() {
    mapper = new DecisionMapper(mappers);
    entity = mapper.toEntity(dummyDecision);
    model = mapper.toModel(entity);

    Mockito.verify(mappers, times(2)).getPerformerMapper();
    Mockito.verify(mappers, times(2)).getBlockMapper();

    verifyDecision( dummyDecision, model );
  }

  public static void verifyDecision(Decision expected, Decision actual) {
    assertNotNull( actual );
    assertEquals( expected.getId(), actual.getId() );
    assertEquals( expected.getLetterhead(), actual.getLetterhead() );
    assertEquals( expected.getApproved(), actual.getApproved() );
    assertEquals( expected.getSigner(), actual.getSigner() );
    assertEquals( expected.getSignerId(), actual.getSignerId() );
    assertEquals( expected.getSignerBlankText(), actual.getSignerBlankText() );
    assertEquals( expected.getSignerIsManager(), actual.getSignerIsManager() );
    assertEquals( expected.getSignerPositionS(), actual.getSignerPositionS() );
    assertEquals( expected.getAssistantId(), actual.getAssistantId() );
    assertEquals( expected.getComment(), actual.getComment() );
    assertEquals( expected.getDate(), actual.getDate() );
    assertEquals( expected.getUrgencyText(), actual.getUrgencyText() );
    assertEquals( expected.getShowPosition(), actual.getShowPosition() );
    assertEquals( expected.getSignBase64(), actual.getSignBase64() );
    assertEquals( expected.getRed(), actual.getRed() );
    assertEquals( expected.getLetterheadFontSize(), actual.getLetterheadFontSize() );
    assertEquals( expected.getPerformersFontSize(), actual.getPerformersFontSize() );

    int index = 0;
    for (Block block : actual.getBlocks() ) {
      BlockMapperTest.verifyBlock( expected.getBlocks().get(index), block );
      index++;
    }
  }

  @Test
  public void hasDiff() {
    mapper = new DecisionMapper(mappers);

    RDecisionEntity entity1 = mapper.toEntity(dummyDecision);
    RDecisionEntity entity2 = mapper.toEntity(dummyDecision);

    boolean hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setUid("");
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }

  @Test
  public void toFormattedModel() {
    mapper = new DecisionMapper(mappers);
    entity = mapper.toEntity(dummyDecision);
    formattedModel = mapper.toFormattedModel(entity);

    Mockito.verify(mappers, times(2)).getPerformerMapper();
    Mockito.verify(mappers, times(2)).getBlockMapper();

    assertNotNull( formattedModel );
    assertEquals( dummyDecision.getId(), formattedModel.getId() );
    assertEquals( dummyDecision.getLetterhead(), formattedModel.getLetterhead() );
    assertEquals( dummyDecision.getApproved(), formattedModel.getApproved() );
    assertEquals( dummyDecision.getSigner(), formattedModel.getSigner() );
    assertEquals( dummyDecision.getSignerId(), formattedModel.getSignerId() );
    assertEquals( dummyDecision.getSignerBlankText(), formattedModel.getSignerBlankText() );
    assertEquals( dummyDecision.getSignerIsManager(), formattedModel.getSignerIsManager() );
    assertEquals( dummyDecision.getSignerPositionS(), formattedModel.getSignerPositionS() );
    assertEquals( dummyDecision.getAssistantId(), formattedModel.getAssistantId() );
    assertEquals( dummyDecision.getComment(), formattedModel.getComment() );
    assertEquals( dummyDecision.getDate(), formattedModel.getDate() );
    assertEquals( dummyDecision.getUrgencyText(), formattedModel.getUrgencyText() );
    assertEquals( dummyDecision.getShowPosition(), formattedModel.getShowPosition() );
    assertEquals( dummyDecision.getSignBase64(), formattedModel.getSignBase64() );
    assertEquals( false, formattedModel.getRed() );
    assertEquals( "12", formattedModel.getLetterheadFontSize() );
    assertEquals( null, formattedModel.getPerformersFontSize() );

    int index = 0;
    for (Block block : formattedModel.getBlocks() ) {
      BlockMapperTest.verifyFormattedBlock( dummyDecision.getBlocks().get(index), block );
      index++;
    }
  }
}

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

    assertNotNull( entity );
    assertEquals( 0, entity.getId() );
    assertEquals( false, entity.isTemporary() );
    assertEquals( dummyDecision.getId(), entity.getUid() );
    assertEquals( dummyDecision.getLetterhead(), entity.getLetterhead() );
    assertEquals( dummyDecision.getApproved(), entity.isApproved() );
    assertEquals( dummyDecision.getSigner(), entity.getSigner() );
    assertEquals( dummyDecision.getSignerId(), entity.getSignerId() );
    assertEquals( dummyDecision.getSignerBlankText(), entity.getSignerBlankText() );
    assertEquals( dummyDecision.getSignerIsManager(), entity.isSignerIsManager() );
    assertEquals( dummyDecision.getSignerPositionS(), entity.getSignerPositionS() );
    assertEquals( dummyDecision.getAssistantId(), entity.getAssistantId() );
    assertEquals( dummyDecision.getComment(), entity.getComment() );
    assertEquals( dummyDecision.getDate(), entity.getDate() );
    assertEquals( dummyDecision.getUrgencyText(), entity.getUrgencyText() );
    assertEquals( dummyDecision.getShowPosition(), entity.isShowPosition() );
    assertEquals( dummyDecision.getSignBase64(), entity.getSignBase64() );
    assertEquals( dummyDecision.getRed(), entity.isRed() );
    assertEquals( dummyDecision.getLetterheadFontSize(), entity.getLetterheadFontSize() );
    assertEquals( dummyDecision.getPerformersFontSize(), entity.getPerformerFontSize() );

    int index = 0;
    for (RBlock _block : entity.getBlocks() ) {
      RBlockEntity blockEntity = (RBlockEntity) _block;
      BlockMapperTest.verifyBlock( dummyDecision.getBlocks().get(index), blockEntity );
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

    assertNotNull( model );
    assertEquals( dummyDecision.getId(), model.getId() );
    assertEquals( dummyDecision.getLetterhead(), model.getLetterhead() );
    assertEquals( dummyDecision.getApproved(), model.getApproved() );
    assertEquals( dummyDecision.getSigner(), model.getSigner() );
    assertEquals( dummyDecision.getSignerId(), model.getSignerId() );
    assertEquals( dummyDecision.getSignerBlankText(), model.getSignerBlankText() );
    assertEquals( dummyDecision.getSignerIsManager(), model.getSignerIsManager() );
    assertEquals( dummyDecision.getSignerPositionS(), model.getSignerPositionS() );
    assertEquals( dummyDecision.getAssistantId(), model.getAssistantId() );
    assertEquals( dummyDecision.getComment(), model.getComment() );
    assertEquals( dummyDecision.getDate(), model.getDate() );
    assertEquals( dummyDecision.getUrgencyText(), model.getUrgencyText() );
    assertEquals( dummyDecision.getShowPosition(), model.getShowPosition() );
    assertEquals( dummyDecision.getSignBase64(), model.getSignBase64() );
    assertEquals( dummyDecision.getRed(), model.getRed() );
    assertEquals( dummyDecision.getLetterheadFontSize(), model.getLetterheadFontSize() );
    assertEquals( dummyDecision.getPerformersFontSize(), model.getPerformersFontSize() );

    int index = 0;
    for (Block block : model.getBlocks() ) {
      BlockMapperTest.verifyBlock( dummyDecision.getBlocks().get(index), block );
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

package sapotero.rxtest.db.mapper;

import java.util.Collections;

import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.decisions.RBlock;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.utils.Settings;

// Maps between Decision and RDecisionEntity
public class DecisionMapper extends AbstractMapper<Decision, RDecisionEntity> {

  public DecisionMapper(Settings settings, Mappers mappers) {
    super(settings, mappers);
  }

  @Override
  public RDecisionEntity toEntity(Decision model) {
    RDecisionEntity entity = new RDecisionEntity();

    entity.setUid( model.getId() );
    entity.setLetterhead(model.getLetterhead());
    entity.setApproved(model.getApproved());
    entity.setTemporary(false);
    entity.setSigner(model.getSigner());
    entity.setSignerId(model.getSignerId());
    entity.setSignerBlankText(model.getSignerBlankText());
    entity.setSignerIsManager(model.getSignerIsManager());
    entity.setSignerPositionS(model.getSignerPositionS());
    entity.setAssistantId(model.getAssistantId());
    entity.setComment(model.getComment());
    entity.setDate(model.getDate());
    entity.setUrgencyText(model.getUrgencyText());
    entity.setShowPosition(model.getShowPosition());
    entity.setSignBase64(model.getSignBase64());
    entity.setRed(model.getRed());
    entity.setLetterheadFontSize(model.getLetterheadFontSize());
    entity.setPerformerFontSize(model.getPerformersFontSize());

    if ( notEmpty( model.getBlocks() ) ) {
      BlockMapper blockMapper = mappers.getBlockMapper();

      for (Block blockModel: model.getBlocks() ) {
        RBlockEntity blockEntity = blockMapper.toEntity(blockModel);
        blockEntity.setDecision(entity);
        entity.getBlocks().add(blockEntity);
      }
    }

    return entity;
  }

  @Override
  public Decision toModel(RDecisionEntity entity) {
    Decision model = new Decision();

    setBaseFields( model, entity );
    model.setRed( entity.isRed() );
    model.setLetterheadFontSize( entity.getLetterheadFontSize() );
    model.setPerformersFontSize( entity.getPerformerFontSize() );
    setBlocks( model, entity, false );

    return model;
  }

  public Decision toFormattedModel(RDecisionEntity entity) {
    Decision formattedModel = new Decision();

    setBaseFields( formattedModel, entity );
    setBlocks( formattedModel, entity, true );

    return formattedModel;
  }

  private void setBaseFields(Decision model, RDecisionEntity entity) {
    model.setId( entity.getUid() );
    model.setLetterhead( entity.getLetterhead() );
    model.setApproved( entity.isApproved() );
    model.setSigner( entity.getSigner() );
    model.setSignerId( entity.getSignerId() );
    model.setSignerBlankText( entity.getSignerBlankText() );
    model.setSignerIsManager( entity.isSignerIsManager() );
    model.setSignerPositionS( entity.getSignerPositionS() );
    model.setAssistantId( entity.getAssistantId() );
    model.setComment( entity.getComment() );
    model.setDate( entity.getDate() );
    model.setUrgencyText( entity.getUrgencyText() );
    model.setShowPosition( entity.isShowPosition() );
    model.setSignBase64( entity.getSignBase64() );
  }

  private void setBlocks(Decision model, RDecisionEntity entity, boolean formatted) {
    if ( notEmpty( entity.getBlocks() ) ) {
      BlockMapper blockMapper = mappers.getBlockMapper();

      for (RBlock _block : entity.getBlocks()) {
        RBlockEntity blockEntity = (RBlockEntity) _block;
        Block blockModel = formatted ? blockMapper.toFormattedModel(blockEntity) : blockMapper.toModel(blockEntity);
        model.getBlocks().add(blockModel);
      }

      if ( !formatted ) {
        Collections.sort(model.getBlocks(), (o1, o2) -> o1.getNumber().compareTo(o2.getNumber()));
      }
    }
  }
}

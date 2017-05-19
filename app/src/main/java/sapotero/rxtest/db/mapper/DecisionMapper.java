package sapotero.rxtest.db.mapper;

import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;

// Maps between Decision and RDecisionEntity
public class DecisionMapper extends AbstractMapper<Decision, RDecisionEntity> {

  @Override
  public RDecisionEntity toEntity(Decision model) {
    RDecisionEntity entity = new RDecisionEntity();

    entity.setUid( model.getId() );
    entity.setLetterhead(model.getLetterhead());
    entity.setApproved(model.getApproved());
    entity.setTemporary(false);
    entity.setSigner(model.getSigner());
    entity.setSignerId(model.getSignerId());
    entity.setAssistantId(model.getAssistantId());
    entity.setSignerBlankText(model.getSignerBlankText());
    entity.setSignerIsManager(model.getSignerIsManager());
    entity.setSignerPositionS(model.getSignerPositionS());
    entity.setComment(model.getComment());
    entity.setDate(model.getDate());
    entity.setUrgencyText(model.getUrgencyText());
    entity.setShowPosition(model.getShowPosition());
    entity.setSignBase64(model.getSignBase64());
    entity.setRed(model.getRed());
    entity.setLetterheadFontSize(model.getLetterhead());
    entity.setPerformerFontSize(model.getPerformersFontSize());

    if ( model.getBlocks() != null && model.getBlocks().size() > 0 ){
      BlockMapper blockMapper = new BlockMapper();

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
    return null;
  }
}

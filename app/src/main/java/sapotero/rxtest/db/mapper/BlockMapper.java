package sapotero.rxtest.db.mapper;

import java.util.Collections;

import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformer;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Performer;

// Maps between Block and RBlockEntity
public class BlockMapper extends AbstractMapper<Block, RBlockEntity> {

  @Override
  public RBlockEntity toEntity(Block model) {
    RBlockEntity entity = new RBlockEntity();

    entity.setNumber(model.getNumber());
    entity.setText(model.getText());
    entity.setFontSize(model.getFontSize());
    entity.setAppealText(model.getAppealText());
    entity.setTextBefore(model.getTextBefore());
    entity.setHidePerformers(model.getHidePerformers());
    entity.setToCopy(model.getToCopy());
    entity.setToFamiliarization(model.getToFamiliarization());

    if ( notEmpty(model.getPerformers() ) ) {
      PerformerMapper performerMapper = new PerformerMapper();

      for (Performer performerModel : model.getPerformers()) {
        RPerformerEntity performerEntity = performerMapper.toEntity(performerModel);
        performerEntity.setBlock(entity);
        entity.getPerformers().add(performerEntity);
      }
    }

    return entity;
  }

  @Override
  public Block toModel(RBlockEntity entity) {
    Block model = new Block();

    model.setNumber(entity.getNumber());
    model.setText(entity.getText());
    model.setFontSize(entity.getFontSize());
    model.setAppealText(entity.getAppealText());
    model.setTextBefore(entity.isTextBefore());
    model.setHidePerformers(entity.isHidePerformers());
    model.setToCopy(entity.isToCopy());
    model.setToFamiliarization(entity.isToFamiliarization());

    if ( notEmpty( entity.getPerformers() ) ) {
      PerformerMapper performerMapper = new PerformerMapper();

      for (RPerformer _performer : entity.getPerformers()) {
        RPerformerEntity performerEntity = (RPerformerEntity) _performer;
        Performer performerModel = performerMapper.toModel(performerEntity);
        model.getPerformers().add(performerModel);
      }
    }

    Collections.sort(model.getPerformers(), (o1, o2) -> o1.getNumber().compareTo(o2.getNumber()));

    return model;
  }

  public Block toFormattedModel(RBlockEntity entity) {
    Block formattedModel = new Block();

    formattedModel.setNumber( entity.getNumber() );
    formattedModel.setText( entity.getText() );
    formattedModel.setFontSize( "14" );
    formattedModel.setAppealText( entity.getAppealText() );
    formattedModel.setTextBefore( entity.isTextBefore() );
    formattedModel.setHidePerformers( entity.isHidePerformers() );
    formattedModel.setIndentation( "1" );

    if ( notEmpty( entity.getPerformers() ) ) {
      PerformerMapper performerMapper = new PerformerMapper();

      for (RPerformer _p : entity.getPerformers()) {
        RPerformerEntity performerEntity = (RPerformerEntity) _p;
        Performer formattedPerformerModel = performerMapper.toFormattedModel(performerEntity);
        formattedModel.getPerformers().add(formattedPerformerModel);
      }
    }

    return formattedModel;
  }
}

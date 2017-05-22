package sapotero.rxtest.db.mapper;

import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.Callable;

import sapotero.rxtest.db.requery.models.RStepEntity;
import sapotero.rxtest.retrofit.models.document.Step;

// Maps between Step and RStepEntity
public class StepMapper extends AbstractMapper<Step, RStepEntity> {

  private Gson gson;

  public StepMapper() {
    super();
    gson = new Gson();
  }

  @Override
  public RStepEntity toEntity(Step model) {
    RStepEntity entity = new RStepEntity();

    entity.setTitle( model.getTitle() );
    entity.setNumber( model.getNumber() );

    if ( listNotEmpty( model.getPeople() ) ) {
      entity.setPeople( listToJson( model.getPeople() ) );
    }

    if ( listNotEmpty( model.getCards() ) ) {
      entity.setCards( listToJson( model.getCards() ) );
    }

    if ( listNotEmpty( model.getAnotherApprovals() ) ) {
      entity.setAnother_approvals( listToJson( model.getAnotherApprovals() ) );
    }

    return entity;
  }

  @Override
  public Step toModel(RStepEntity entity) {
    // Method is not used
    return null;
  }

  private <T> boolean listNotEmpty(List<T> list) {
    return list != null && list.size() > 0;
  }

  private <T> String listToJson(List<T> list) {
    return gson.toJson( list );
  }
}

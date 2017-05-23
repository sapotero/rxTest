package sapotero.rxtest.db.mapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Type;

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
    set( entity::setPeople, model.getPeople() );
    set (entity::setCards, model.getCards() );
    set (entity::setAnother_approvals, model.getAnotherApprovals() );

    return entity;
  }

  @Override
  public Step toModel(RStepEntity entity) {
    Step model = new Step();

    model.setTitle( entity.getTitle() );
    model.setNumber( entity.getNumber() );

    if ( stringNotEmpty( entity.getPeople() ) ) {
      model.setPeople( jsonToList( entity.getPeople() ) );
    }

    if ( stringNotEmpty( entity.getCards() ) ) {
      model.setCards( jsonToList( entity.getCards() ) );
    }

    if ( stringNotEmpty( entity.getAnother_approvals() ) ) {
      model.setAnotherApprovals( jsonToList( entity.getAnother_approvals() ) );
    }

    return model;
  }

  private <T> String listToJson(List<T> list) {
    return gson.toJson( list );
  }

  private <T> ArrayList<T> jsonToList(String jsonString) {
    Type listType = new TypeToken<ArrayList<T>>(){}.getType();
    return gson.fromJson(jsonString, listType);
  }

  private boolean stringNotEmpty(String s) {
    return s != null && !s.equals("");
  }

  private interface FieldSetter {
    void setField(String s);
  }

  private <T> void set(FieldSetter fieldSetter, List<T> list) {
    if ( listNotEmpty( list ) ) {
      fieldSetter.setField( listToJson( list ) );
    }
  }
}

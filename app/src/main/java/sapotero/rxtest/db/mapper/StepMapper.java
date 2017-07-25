package sapotero.rxtest.db.mapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Type;

import sapotero.rxtest.db.requery.models.RStepEntity;
import sapotero.rxtest.retrofit.models.document.AnotherApproval;
import sapotero.rxtest.retrofit.models.document.Card;
import sapotero.rxtest.retrofit.models.document.Person;
import sapotero.rxtest.retrofit.models.document.Step;

// Maps between Step and RStepEntity
public class StepMapper extends AbstractMapper<Step, RStepEntity> {

  private Gson gson;

  public StepMapper() {
    gson = new Gson();
  }

  @Override
  public RStepEntity toEntity(Step model) {
    RStepEntity entity = new RStepEntity();

    entity.setTitle( model.getTitle() );
    entity.setNumber( model.getNumber() );
    set( entity::setPeople, model.getPeople() );
    set( entity::setCards, model.getCards() );
    set( entity::setAnother_approvals, model.getAnotherApprovals() );

    return entity;
  }

  @Override
  public Step toModel(RStepEntity entity) {
    Step model = new Step();

    model.setTitle( entity.getTitle() );
    model.setNumber( entity.getNumber() );
    set( model::setPeople, entity.getPeople(), FieldType.PEOPLE );
    set( model::setCards, entity.getCards(), FieldType.CARDS );
    set( model::setAnotherApprovals, entity.getAnother_approvals(), FieldType.ANOTHER_APPROVALS );

    return model;
  }

  private <T> String listToJson(List<T> list) {
    return gson.toJson( list );
  }

  public <T> ArrayList<T> jsonToList(String jsonString, FieldType fieldType) {
    Type listType;
    switch ( fieldType ) {
      case PEOPLE:
        listType = new TypeToken<ArrayList<Person>>(){}.getType();
        break;
      case CARDS:
        listType = new TypeToken<ArrayList<Card>>(){}.getType();
        break;
      case ANOTHER_APPROVALS:
        listType = new TypeToken<ArrayList<AnotherApproval>>(){}.getType();
        break;
      default:
        listType = new TypeToken<ArrayList<Person>>(){}.getType();
        break;
    }
    return gson.fromJson(jsonString, listType);
  }

  private <T> void set(StringFieldSetter stringFieldSetter, List<T> list) {
    if ( notEmpty( list ) ) {
      stringFieldSetter.setField( listToJson( list ) );
    }
  }

  private <T> void set(ListFieldSetter<T> listFieldSetter, String jsonString, FieldType fieldType) {
    if ( notEmpty( jsonString ) ) {
      listFieldSetter.setField( jsonToList( jsonString, fieldType ) );
    }
  }

  public enum FieldType {
    PEOPLE,
    CARDS,
    ANOTHER_APPROVALS
  }
}

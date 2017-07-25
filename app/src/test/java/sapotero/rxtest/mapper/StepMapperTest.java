package sapotero.rxtest.mapper;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import sapotero.rxtest.db.mapper.StepMapper;
import sapotero.rxtest.db.requery.models.RStepEntity;
import sapotero.rxtest.retrofit.models.document.Action;
import sapotero.rxtest.retrofit.models.document.AnotherApproval;
import sapotero.rxtest.retrofit.models.document.Card;
import sapotero.rxtest.retrofit.models.document.Person;
import sapotero.rxtest.retrofit.models.document.Step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StepMapperTest {

  private StepMapper mapper;
  private Step dummyStep;
  private RStepEntity entity;
  private Step model;

  @Before
  public void init() {
    generateStep();
  }

  private void generateStep() {
    dummyStep = new Step();

    dummyStep.setTitle( "kjsdhfkds823kjsd" );
    dummyStep.setNumber( "56" );

    List<Person> people = new ArrayList<>();
    Person person = new Person();
    person.setOfficialId( "58f88dfc776b000026000001" );
    person.setOfficialName( "Сотрудник_а2 A.T." );
    person.setSignPng( "dskjfhdskifh23rkjb3qrGJTds34ds" );

    List<Action> actions = new ArrayList<>();
    Action action = new Action();
    action.setDate( "2017-07-24" );
    action.setStatus( "primary_consideration" );
    action.setComment( "Отправлен на первичное рассмотрение" );

    actions.add(action);
    person.setActions(actions);
    people.add(person);
    dummyStep.setPeople(people);

    List<Card> cards = new ArrayList<>();
    Card card = new Card();
    card.setUid( "lkfjg8934fkjrhf893ynrd934iydr789y3nr823y4nx874ny32x8" );
    card.setOriginalApproval( "Eskjhi sfkjhs f sdfihijaushfkj" );
    card.setFullTextApproval( "Dkjsdhfiu  dsfkjdhs f hsdkfh dsfdhaus fawhkjsa hfkjsdh" );
    cards.add(card);
    dummyStep.setCards(cards);

    List<AnotherApproval> anotherApprovals = new ArrayList<>();
    AnotherApproval anotherApproval = new AnotherApproval();
    anotherApproval.setOfficialName( "Сотрудник_а3 A.T." );
    anotherApproval.setComment( "SD23iuyi hfjew8y23rkjhf ewfkjhewf78hfdks fdfgfd" );
    anotherApprovals.add(anotherApproval);
    dummyStep.setAnotherApprovals(anotherApprovals);
  }

  @Test
  public void toEntity() {
    mapper = new StepMapper();
    entity = mapper.toEntity(dummyStep);

    assertNotNull( entity );
    assertEquals( 0, entity.getId() );
    assertEquals( dummyStep.getTitle(), entity.getTitle() );
    assertEquals( dummyStep.getNumber(), entity.getNumber() );

    List<Person> expectedPeople = dummyStep.getPeople();
    List<Person> actualPeople = mapper.jsonToList( entity.getPeople(), StepMapper.FieldType.PEOPLE );
    if ( actualPeople != null ) {
      for ( int i = 0; i < actualPeople.size(); i++ ) {
        assertEquals( expectedPeople.get(i).getOfficialId(), actualPeople.get(i).getOfficialId() );
        assertEquals( expectedPeople.get(i).getOfficialName(), actualPeople.get(i).getOfficialName() );
        assertEquals( expectedPeople.get(i).getSignPng(), actualPeople.get(i).getSignPng() );

        List<Action> expectedActions = expectedPeople.get(i).getActions();
        List<Action> actualActions = actualPeople.get(i).getActions();
        if ( actualActions != null) {
          for ( int j = 0; j < actualActions.size(); j++ ) {
            assertEquals( expectedActions.get(j).getDate(), actualActions.get(j).getDate() );
            assertEquals( expectedActions.get(j).getStatus(), actualActions.get(j).getStatus() );
            assertEquals( expectedActions.get(j).getComment(), actualActions.get(j).getComment() );
          }
        }
      }
    }

    // TODO: verify cards

    // TODO: verify another approvals
  }
}

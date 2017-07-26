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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class StepMapperTest {

  private StepMapper mapper;
  private Step dummyStep;
  private RStepEntity entity;
  private Step model;

  @Before
  public void init() {
    dummyStep = generateStep();
  }

  public static Step generateStep() {
    Step dummyStep = new Step();

    dummyStep.setTitle( "Согласующие" );
    dummyStep.setNumber( "56" );

    List<Person> people = new ArrayList<>();
    Person person = new Person();
    person.setOfficialId( "58f88dfc776b000026000001" );
    person.setOfficialName( "Сотрудник_а2 A.T." );
    person.setSignPng( "dskjfhdskifh23rkjb3qrGJTds34ds" );

    List<Action> actions = new ArrayList<>();
    Action action = new Action();
    action.setDate( "30.06.2017 10:01" );
    action.setStatus( "Согласовано" );
    action.setComment( "SDFrkjf f ds78 fjhkdskjfh" );

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

    return dummyStep;
  }

  @Test
  public void toEntity() {
    mapper = new StepMapper();
    entity = mapper.toEntity(dummyStep);

    verifyStep( dummyStep, entity, mapper );
  }

  public static void verifyStep( Step expected, RStepEntity actual, StepMapper mapper ) {
    assertNotNull( actual );
    assertEquals( 0, actual.getId() );
    assertEquals( expected.getTitle(), actual.getTitle() );
    assertEquals( expected.getNumber(), actual.getNumber() );

    List<Person> expectedPeople = expected.getPeople();
    List<Person> actualPeople = mapper.jsonToList( actual.getPeople(), StepMapper.FieldType.PEOPLE );
    verifyPeople( expectedPeople, actualPeople );

    List<Card> expectedCards = expected.getCards();
    List<Card> actualCards = mapper.jsonToList( actual.getCards(), StepMapper.FieldType.CARDS );
    verifyCards( expectedCards, actualCards );

    List<AnotherApproval> expectedAnotherApprovals = expected.getAnotherApprovals();
    List<AnotherApproval> actualAnotherApprovals = mapper.jsonToList( actual.getAnother_approvals(), StepMapper.FieldType.ANOTHER_APPROVALS );
    verifyAnotherApprovals( expectedAnotherApprovals, actualAnotherApprovals );
  }

  private static void verifyPeople(List<Person> expectedPeople, List<Person> actualPeople) {
    assertNotNull( actualPeople );

    for ( int i = 0; i < actualPeople.size(); i++ ) {
      assertEquals( expectedPeople.get(i).getOfficialId(), actualPeople.get(i).getOfficialId() );
      assertEquals( expectedPeople.get(i).getOfficialName(), actualPeople.get(i).getOfficialName() );
      assertEquals( expectedPeople.get(i).getSignPng(), actualPeople.get(i).getSignPng() );

      List<Action> expectedActions = expectedPeople.get(i).getActions();
      List<Action> actualActions = actualPeople.get(i).getActions();

      assertNotNull( actualActions );

      for ( int j = 0; j < actualActions.size(); j++ ) {
        assertEquals( expectedActions.get(j).getDate(), actualActions.get(j).getDate() );
        assertEquals( expectedActions.get(j).getStatus(), actualActions.get(j).getStatus() );
        assertEquals( expectedActions.get(j).getComment(), actualActions.get(j).getComment() );
      }
    }
  }

  private static void verifyCards(List<Card> expectedCards, List<Card> actualCards) {
    assertNotNull( actualCards );

    for ( int i = 0; i < actualCards.size(); i++ ) {
      assertEquals( expectedCards.get(i).getUid(), actualCards.get(i).getUid() );
      assertEquals( expectedCards.get(i).getOriginalApproval(), actualCards.get(i).getOriginalApproval() );
      assertEquals( expectedCards.get(i).getFullTextApproval(), actualCards.get(i).getFullTextApproval() );
    }
  }

  private static void verifyAnotherApprovals(List<AnotherApproval> expectedAnotherApprovals, List<AnotherApproval> actualAnotherApprovals) {
    assertNotNull( actualAnotherApprovals );

    for ( int i = 0; i < actualAnotherApprovals.size(); i++ ) {
      assertEquals( expectedAnotherApprovals.get(i).getOfficialName(), actualAnotherApprovals.get(i).getOfficialName() );
      assertEquals( expectedAnotherApprovals.get(i).getComment(), actualAnotherApprovals.get(i).getComment() );
    }
  }

  @Test
  public void toModel() {
    mapper = new StepMapper();
    entity = mapper.toEntity(dummyStep);
    model = mapper.toModel(entity);

    verifyStep( dummyStep, model );
  }

  public static void verifyStep(Step expected, Step actual) {
    assertNotNull( actual );
    assertEquals( expected.getTitle(), actual.getTitle() );
    assertEquals( expected.getNumber(), actual.getNumber() );

    List<Person> expectedPeople = expected.getPeople();
    List<Person> actualPeople = actual.getPeople();
    verifyPeople( expectedPeople, actualPeople );

    List<Card> expectedCards = expected.getCards();
    List<Card> actualCards = actual.getCards();
    verifyCards( expectedCards, actualCards );

    List<AnotherApproval> expectedAnotherApprovals = expected.getAnotherApprovals();
    List<AnotherApproval> actualAnotherApprovals = actual.getAnotherApprovals();
    verifyAnotherApprovals( expectedAnotherApprovals, actualAnotherApprovals );
  }

  @Test
  public void hasDiff() {
    mapper = new StepMapper();

    RStepEntity entity1 = mapper.toEntity(dummyStep);
    RStepEntity entity2 = mapper.toEntity(dummyStep);

    boolean hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setNumber( "10" );
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }
}

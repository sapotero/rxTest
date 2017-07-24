package sapotero.rxtest.mapper;

import org.junit.Before;
import org.junit.Test;

import sapotero.rxtest.db.mapper.PerformerMapper;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.retrofit.models.document.IPerformer;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PerformerMapperTest {

  private PerformerMapper mapper;
  private Performer dummyPerformer;
  private PrimaryConsiderationPeople dummyPrimaryConsiderationPeople;
  private Oshs dummyOshs;
  private RPerformerEntity entity;
  private Performer model;

  @Before
  public void init() {
    mapper = new PerformerMapper();
    dummyPerformer = generatePerformer();
    generatePrimaryConsiderationPeople();
    dummyOshs = PrimaryConsiderationMapperTest.generateOshs();
  }

  public static Performer generatePerformer() {
    Performer dummyPerformer = new Performer();
    dummyPerformer.setId( "5936884c029a000222000001" );
    dummyPerformer.setPerformerId( "58f88dfc776b000026000001" );
    dummyPerformer.setIsOriginal( true );
    dummyPerformer.setIsResponsible( false );
    dummyPerformer.setOrganization( false );
    dummyPerformer.setNumber( 1 );
    dummyPerformer.setPerformerType( "mvd_person" );
    dummyPerformer.setPerformerText( "Сотрудник_а2 A.T. (Сотрудник ОДИР)" );
    dummyPerformer.setPerformerGender( "Мужской" );
    dummyPerformer.setOrganizationText( "ОДиР ГУ МВД России по Самарской области" );
    return dummyPerformer;
  }

  private void generatePrimaryConsiderationPeople() {
    dummyPrimaryConsiderationPeople = new PrimaryConsiderationPeople();
    dummyPrimaryConsiderationPeople.setPosition( "Сотрудник ОДИР" );
    dummyPrimaryConsiderationPeople.setName( "Сотрудник_а2 A.T." );
    dummyPrimaryConsiderationPeople.setId( "58f88dfc776b000026000001" );
    dummyPrimaryConsiderationPeople.setOrganization( "ОДиР ГУ МВД России по Самарской области" );
    dummyPrimaryConsiderationPeople.setGender( "Мужской" );
    dummyPrimaryConsiderationPeople.setIsOrganization( false );
  }

  @Test
  public void toEntity() {
    entity = mapper.toEntity(dummyPerformer);

    verifyPerformer( dummyPerformer, entity );
  }

  public static void verifyPerformer(Performer expected, RPerformerEntity actual) {
    assertNotNull( actual );
    assertEquals( 0, actual.getId() );
    assertEquals( expected.getId(), actual.getUid() );
    assertEquals( expected.getPerformerId(), actual.getPerformerId() );
    assertEquals( expected.getIsOriginal(), actual.isIsOriginal() );
    assertEquals( expected.getIsResponsible(), actual.isIsResponsible() );
    assertEquals( expected.getOrganization(), actual.isIsOrganization() );
    assertEquals( expected.getNumber(), actual.getNumber() );
    assertEquals( expected.getPerformerType(), actual.getPerformerType() );
    assertEquals( expected.getPerformerText(), actual.getPerformerText() );
    assertEquals( expected.getPerformerGender(), actual.getPerformerGender() );
    assertEquals( expected.getOrganizationText(), actual.getOrganizationText() );
  }

  @Test
  public void toModel() {
    entity = mapper.toEntity(dummyPerformer);
    model = mapper.toModel(entity);

    verifyPerformer( dummyPerformer, model );
  }

  public static void verifyPerformer(Performer expected, Performer actual) {
    assertNotNull( actual );
    assertEquals( expected.getId(), actual.getId() );
    assertEquals( expected.getPerformerId(), actual.getPerformerId() );
    assertEquals( expected.getIsOriginal(), actual.getIsOriginal() );
    assertEquals( expected.getIsResponsible(), actual.getIsResponsible() );
    assertEquals( expected.getOrganization(), actual.getOrganization() );
    assertEquals( expected.getNumber(), actual.getNumber() );
    assertEquals( expected.getPerformerType(), actual.getPerformerType() );
    assertEquals( expected.getPerformerText(), actual.getPerformerText() );
    assertEquals( expected.getPerformerGender(), actual.getPerformerGender() );
    assertEquals( expected.getOrganizationText(), actual.getOrganizationText() );
  }

  public static void verifyFormattedPerformer(Performer expected, Performer actual) {
    assertNotNull( actual );
    assertEquals( expected.getId(), actual.getId() );
    assertEquals( expected.getPerformerId(), actual.getPerformerId() );
    assertEquals( expected.getIsOriginal(), actual.getIsOriginal() );
    assertEquals( expected.getIsResponsible(), actual.getIsResponsible() );
    assertEquals( expected.getOrganization(), actual.getOrganization() );
    assertEquals( null, actual.getNumber() );
    assertEquals( null, actual.getPerformerType() );
    assertEquals( null, actual.getPerformerText() );
    assertEquals( null, actual.getPerformerGender() );
    assertEquals( null, actual.getOrganizationText() );
    assertEquals( false, actual.getGroup() );
  }

  @Test
  public void isOrganization() {
    Performer performer = new Performer();

    performer.setPerformerType( "mvd_person" );
    assertFalse( performer.getOrganization() );

    performer.setPerformerType( "mvd_organisation" );
    assertTrue( performer.getOrganization() );
  }

  @Test
  public void hasDiff() {
    RPerformerEntity entity1 = mapper.toEntity(dummyPerformer);
    RPerformerEntity entity2 = mapper.toEntity(dummyPerformer);
    boolean hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setUid("");
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }

  @Test
  public void convertFromPerformerToPerformer() {
    IPerformer destination = mapper.convert(dummyPerformer, PerformerMapper.DestinationType.PERFORMER);

    assertNotNull( destination );
    assertTrue( destination instanceof Performer );
    assertEquals( dummyPerformer.getId(), destination.getIPerformerUid() );
    assertEquals( dummyPerformer.getNumber(), destination.getIPerformerNumber() );
    assertEquals( dummyPerformer.getPerformerId(), destination.getIPerformerId() );
    assertEquals( dummyPerformer.getPerformerType(), destination.getIPerformerType() );
    assertEquals( dummyPerformer.getPerformerText(), destination.getIPerformerName() );
    assertEquals( dummyPerformer.getPerformerGender(), destination.getIPerformerGender() );
    assertEquals( dummyPerformer.getOrganizationText(), destination.getIPerformerOrganizationName() );
    assertEquals( null, destination.getIPerformerAssistantId() );
    assertEquals( null, destination.getIPerformerPosition() );
    assertEquals( null, destination.getIPerformerLastName() );
    assertEquals( null, destination.getIPerformerFirstName() );
    assertEquals( null, destination.getIPerformerMiddleName() );
    assertEquals( null, destination.getIPerformerImage() );
    assertEquals( dummyPerformer.getIsOriginal(), destination.isIPerformerOriginal() );
    assertEquals( dummyPerformer.getIsResponsible(), destination.isIPerformerResponsible() );
    assertEquals( null, destination.isIPerformerGroup() );
    assertEquals( dummyPerformer.getOrganization(), destination.isIPerformerOrganization() );
  }

  @Test
  public void convertFromPerformerToPrimaryConsiderationPeople() {
    IPerformer destination = mapper.convert(dummyPerformer, PerformerMapper.DestinationType.PRIMARYCONSIDERATIONPEOPLE);

    assertNotNull( destination );
    assertTrue( destination instanceof PrimaryConsiderationPeople);
    assertEquals( dummyPerformer.getId(), destination.getIPerformerUid() );
    assertEquals( null, destination.getIPerformerNumber() );
    assertEquals( dummyPerformer.getPerformerId(), destination.getIPerformerId() );
    assertEquals( null, destination.getIPerformerType() );
    assertEquals( dummyPerformer.getPerformerText(), destination.getIPerformerName() );
    assertEquals( dummyPerformer.getPerformerGender(), destination.getIPerformerGender() );
    assertEquals( dummyPerformer.getOrganizationText(), destination.getIPerformerOrganizationName() );
    assertEquals( null, destination.getIPerformerAssistantId() );
    assertEquals( null, destination.getIPerformerPosition() );
    assertEquals( null, destination.getIPerformerLastName() );
    assertEquals( null, destination.getIPerformerFirstName() );
    assertEquals( null, destination.getIPerformerMiddleName() );
    assertEquals( null, destination.getIPerformerImage() );
    assertEquals( dummyPerformer.getIsOriginal(), destination.isIPerformerOriginal() );
    assertEquals( dummyPerformer.getIsResponsible(), destination.isIPerformerResponsible() );
    assertEquals( null, destination.isIPerformerGroup() );
    assertEquals( dummyPerformer.getOrganization(), destination.isIPerformerOrganization() );
  }

  @Test
  public void convertFromPerformerToOshs() {
    IPerformer destination = mapper.convert(dummyPerformer, PerformerMapper.DestinationType.OSHS);

    assertNotNull( destination );
    assertTrue( destination instanceof Oshs );
    assertEquals( null, destination.getIPerformerUid() );
    assertEquals( null, destination.getIPerformerNumber() );
    assertEquals( dummyPerformer.getPerformerId(), destination.getIPerformerId() );
    assertEquals( null, destination.getIPerformerType() );
    assertEquals( dummyPerformer.getPerformerText(), destination.getIPerformerName() );
    assertEquals( dummyPerformer.getPerformerGender(), destination.getIPerformerGender() );
    assertEquals( dummyPerformer.getOrganizationText(), destination.getIPerformerOrganizationName() );
    assertEquals( null, destination.getIPerformerAssistantId() );
    assertEquals( null, destination.getIPerformerPosition() );
    assertEquals( null, destination.getIPerformerLastName() );
    assertEquals( null, destination.getIPerformerFirstName() );
    assertEquals( null, destination.getIPerformerMiddleName() );
    assertEquals( null, destination.getIPerformerImage() );
    assertEquals( null, destination.isIPerformerOriginal() );
    assertEquals( null, destination.isIPerformerResponsible() );
    assertEquals( null, destination.isIPerformerGroup() );
    assertEquals( dummyPerformer.getOrganization(), destination.isIPerformerOrganization() );
  }

  @Test
  public void convertFromPrimaryConsiderationPeopleToPerformer() {
    IPerformer destination = mapper.convert(dummyPrimaryConsiderationPeople, PerformerMapper.DestinationType.PERFORMER);

    assertNotNull( destination );
    assertTrue( destination instanceof Performer );
    assertEquals( null, destination.getIPerformerUid() );
    assertEquals( null, destination.getIPerformerNumber() );
    assertEquals( dummyPrimaryConsiderationPeople.getId(), destination.getIPerformerId() );
    assertEquals( null, destination.getIPerformerType() );
    assertEquals( dummyPrimaryConsiderationPeople.getName(), destination.getIPerformerName() );
    assertEquals( dummyPrimaryConsiderationPeople.getGender(), destination.getIPerformerGender() );
    assertEquals( dummyPrimaryConsiderationPeople.getOrganization(), destination.getIPerformerOrganizationName() );
    assertEquals( null, destination.getIPerformerAssistantId() );
    assertEquals( null, destination.getIPerformerPosition() );
    assertEquals( null, destination.getIPerformerLastName() );
    assertEquals( null, destination.getIPerformerFirstName() );
    assertEquals( null, destination.getIPerformerMiddleName() );
    assertEquals( null, destination.getIPerformerImage() );
    assertEquals( dummyPrimaryConsiderationPeople.isOriginal(), destination.isIPerformerOriginal() );
    assertEquals( dummyPrimaryConsiderationPeople.isResponsible(), destination.isIPerformerResponsible() );
    assertEquals( null, destination.isIPerformerGroup() );
    assertEquals( dummyPrimaryConsiderationPeople.isOrganization(), destination.isIPerformerOrganization() );
  }

  @Test
  public void convertFromPrimaryConsiderationPeopleToPrimaryConsiderationPeople() {
    IPerformer destination = mapper.convert(dummyPrimaryConsiderationPeople, PerformerMapper.DestinationType.PRIMARYCONSIDERATIONPEOPLE);

    assertNotNull( destination );
    assertTrue( destination instanceof PrimaryConsiderationPeople );
    assertEquals( null, destination.getIPerformerUid() );
    assertEquals( null, destination.getIPerformerNumber() );
    assertEquals( dummyPrimaryConsiderationPeople.getId(), destination.getIPerformerId() );
    assertEquals( null, destination.getIPerformerType() );
    assertEquals( dummyPrimaryConsiderationPeople.getName(), destination.getIPerformerName() );
    assertEquals( dummyPrimaryConsiderationPeople.getGender(), destination.getIPerformerGender() );
    assertEquals( dummyPrimaryConsiderationPeople.getOrganization(), destination.getIPerformerOrganizationName() );
    assertEquals( null, destination.getIPerformerAssistantId() );
    assertEquals( dummyPrimaryConsiderationPeople.getPosition(), destination.getIPerformerPosition() );
    assertEquals( null, destination.getIPerformerLastName() );
    assertEquals( null, destination.getIPerformerFirstName() );
    assertEquals( null, destination.getIPerformerMiddleName() );
    assertEquals( null, destination.getIPerformerImage() );
    assertEquals( dummyPrimaryConsiderationPeople.isOriginal(), destination.isIPerformerOriginal() );
    assertEquals( dummyPrimaryConsiderationPeople.isResponsible(), destination.isIPerformerResponsible() );
    assertEquals( null, destination.isIPerformerGroup() );
    assertEquals( dummyPrimaryConsiderationPeople.isOrganization(), destination.isIPerformerOrganization() );
  }

  @Test
  public void convertFromPrimaryConsiderationPeopleToOshs() {
    IPerformer destination = mapper.convert(dummyPrimaryConsiderationPeople, PerformerMapper.DestinationType.OSHS);

    assertNotNull( destination );
    assertTrue( destination instanceof Oshs);
    assertEquals( null, destination.getIPerformerUid() );
    assertEquals( null, destination.getIPerformerNumber() );
    assertEquals( dummyPrimaryConsiderationPeople.getId(), destination.getIPerformerId() );
    assertEquals( null, destination.getIPerformerType() );
    assertEquals( dummyPrimaryConsiderationPeople.getName(), destination.getIPerformerName() );
    assertEquals( dummyPrimaryConsiderationPeople.getGender(), destination.getIPerformerGender() );
    assertEquals( dummyPrimaryConsiderationPeople.getOrganization(), destination.getIPerformerOrganizationName() );
    assertEquals( null, destination.getIPerformerAssistantId() );
    assertEquals( dummyPrimaryConsiderationPeople.getPosition(), destination.getIPerformerPosition() );
    assertEquals( null, destination.getIPerformerLastName() );
    assertEquals( null, destination.getIPerformerFirstName() );
    assertEquals( null, destination.getIPerformerMiddleName() );
    assertEquals( null, destination.getIPerformerImage() );
    assertEquals( null, destination.isIPerformerOriginal() );
    assertEquals( null, destination.isIPerformerResponsible() );
    assertEquals( null, destination.isIPerformerGroup() );
    assertEquals( dummyPrimaryConsiderationPeople.isOrganization(), destination.isIPerformerOrganization() );
  }

  @Test
  public void convertFromOshsToPerformer() {
    IPerformer destination = mapper.convert(dummyOshs, PerformerMapper.DestinationType.PERFORMER);

    assertNotNull( destination );
    assertTrue( destination instanceof Performer );
    assertEquals( null, destination.getIPerformerUid() );
    assertEquals( null, destination.getIPerformerNumber() );
    assertEquals( dummyOshs.getId(), destination.getIPerformerId() );
    assertEquals( null, destination.getIPerformerType() );
    assertEquals( dummyOshs.getName(), destination.getIPerformerName() );
    assertEquals( dummyOshs.getGender(), destination.getIPerformerGender() );
    assertEquals( dummyOshs.getOrganization(), destination.getIPerformerOrganizationName() );
    assertEquals( null, destination.getIPerformerAssistantId() );
    assertEquals( null, destination.getIPerformerPosition() );
    assertEquals( null, destination.getIPerformerLastName() );
    assertEquals( null, destination.getIPerformerFirstName() );
    assertEquals( null, destination.getIPerformerMiddleName() );
    assertEquals( null, destination.getIPerformerImage() );
    assertEquals( null, destination.isIPerformerOriginal() );
    assertEquals( null, destination.isIPerformerResponsible() );
    assertEquals( null, destination.isIPerformerGroup() );
    assertEquals( dummyOshs.getIsOrganization(), destination.isIPerformerOrganization() );
  }

  @Test
  public void convertFromOshsToPrimaryConsiderationPeople() {
    IPerformer destination = mapper.convert(dummyOshs, PerformerMapper.DestinationType.PRIMARYCONSIDERATIONPEOPLE);

    assertNotNull( destination );
    assertTrue( destination instanceof PrimaryConsiderationPeople );
    assertEquals( null, destination.getIPerformerUid() );
    assertEquals( null, destination.getIPerformerNumber() );
    assertEquals( dummyOshs.getId(), destination.getIPerformerId() );
    assertEquals( null, destination.getIPerformerType() );
    assertEquals( dummyOshs.getName(), destination.getIPerformerName() );
    assertEquals( dummyOshs.getGender(), destination.getIPerformerGender() );
    assertEquals( dummyOshs.getOrganization(), destination.getIPerformerOrganizationName() );
    assertEquals( dummyOshs.getAssistantId(), destination.getIPerformerAssistantId() );
    assertEquals( dummyOshs.getPosition(), destination.getIPerformerPosition() );
    assertEquals( null, destination.getIPerformerLastName() );
    assertEquals( null, destination.getIPerformerFirstName() );
    assertEquals( null, destination.getIPerformerMiddleName() );
    assertEquals( null, destination.getIPerformerImage() );
    assertEquals( false, destination.isIPerformerOriginal() );
    assertEquals( false, destination.isIPerformerResponsible() );
    assertEquals( null, destination.isIPerformerGroup() );
    assertEquals( dummyOshs.getIsOrganization(), destination.isIPerformerOrganization() );
  }

  @Test
  public void convertFromOshsToOshs() {
    IPerformer destination = mapper.convert(dummyOshs, PerformerMapper.DestinationType.OSHS);

    assertNotNull( destination );
    assertTrue( destination instanceof Oshs );
    assertEquals( null, destination.getIPerformerUid() );
    assertEquals( null, destination.getIPerformerNumber() );
    assertEquals( dummyOshs.getId(), destination.getIPerformerId() );
    assertEquals( null, destination.getIPerformerType() );
    assertEquals( dummyOshs.getName(), destination.getIPerformerName() );
    assertEquals( dummyOshs.getGender(), destination.getIPerformerGender() );
    assertEquals( dummyOshs.getOrganization(), destination.getIPerformerOrganizationName() );
    assertEquals( dummyOshs.getAssistantId(), destination.getIPerformerAssistantId() );
    assertEquals( dummyOshs.getPosition(), destination.getIPerformerPosition() );
    assertEquals( dummyOshs.getLastName(), destination.getIPerformerLastName() );
    assertEquals( dummyOshs.getFirstName(), destination.getIPerformerFirstName() );
    assertEquals( dummyOshs.getMiddleName(), destination.getIPerformerMiddleName() );
    assertEquals( dummyOshs.getImage(), destination.getIPerformerImage() );
    assertEquals( null, destination.isIPerformerOriginal() );
    assertEquals( null, destination.isIPerformerResponsible() );
    assertEquals( dummyOshs.getIsGroup(), destination.isIPerformerGroup() );
    assertEquals( dummyOshs.getIsOrganization(), destination.isIPerformerOrganization() );
  }
}

package sapotero.rxtest.mapper;


import org.junit.Before;
import org.junit.Test;

import sapotero.rxtest.db.mapper.ExemplarMapper;
import sapotero.rxtest.db.requery.models.exemplars.RExemplarEntity;
import sapotero.rxtest.db.requery.utils.JournalStatus;
import sapotero.rxtest.retrofit.models.document.Exemplar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ExemplarMapperTest {

  private ExemplarMapper mapper;
  private Exemplar dummyExemplar;
  private RExemplarEntity entity;
  private Exemplar model;

  @Before
  public void init() {
    dummyExemplar = generateExemplar();
  }

  public static Exemplar generateExemplar() {
    Exemplar dummyExemplar = new Exemplar();
    dummyExemplar.setNumber( 3 );
    dummyExemplar.setIsOriginal( true );
    dummyExemplar.setStatusCode( JournalStatus.FOR_REPORT.getName() );
    dummyExemplar.setAddressedToId( "58f88dfc776b000026000001" );
    dummyExemplar.setAddressedToName( "Сотрудник_а2 A.T. (ОДиР ГУ МВД России по Самарской области)" );
    dummyExemplar.setDate( "05.06.2017" );
    return dummyExemplar;
  }

  @Test
  public void toEntity() {
    mapper = new ExemplarMapper();
    entity = mapper.toEntity(dummyExemplar);

    verifyExemplar( dummyExemplar, entity );
  }

  public static void verifyExemplar(Exemplar expected, RExemplarEntity actual) {
    assertNotNull( actual );
    assertEquals( 0, actual.getId() );
    assertEquals( String.valueOf(expected.getNumber()), actual.getNumber() );
    assertEquals( expected.getIsOriginal(), actual.isIsOriginal() );
    assertEquals( expected.getStatusCode(), actual.getStatusCode() );
    assertEquals( expected.getAddressedToId(), actual.getAddressedToId() );
    assertEquals( expected.getAddressedToName(), actual.getAddressedToName() );
    assertEquals( expected.getDate(), actual.getDate() );
  }

  @Test
  public void toModel() {
    mapper = new ExemplarMapper();
    entity = mapper.toEntity(dummyExemplar);
    model = mapper.toModel(entity);

    verifyExemplar( dummyExemplar, model );
  }

  public static void verifyExemplar(Exemplar expected, Exemplar actual) {
    assertNotNull( actual );
    assertEquals( expected.getNumber(), actual.getNumber() );
    assertEquals( expected.getIsOriginal(), actual.getIsOriginal() );
    assertEquals( expected.getStatusCode(), actual.getStatusCode() );
    assertEquals( expected.getAddressedToId(), actual.getAddressedToId() );
    assertEquals( expected.getAddressedToName(), actual.getAddressedToName() );
    assertEquals( expected.getDate(), actual.getDate() );
  }

  @Test
  public void hasDiff() {
    mapper = new ExemplarMapper();

    RExemplarEntity entity1 = mapper.toEntity(dummyExemplar);
    RExemplarEntity entity2 = mapper.toEntity(dummyExemplar);

    boolean hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setNumber( "5" );
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }
}

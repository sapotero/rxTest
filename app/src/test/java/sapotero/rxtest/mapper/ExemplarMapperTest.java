package sapotero.rxtest.mapper;


import org.junit.Before;
import org.junit.Test;

import sapotero.rxtest.db.mapper.ExemplarMapper;
import sapotero.rxtest.db.requery.models.exemplars.RExemplarEntity;
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
    generateExemplar();
  }

  private void generateExemplar() {
    dummyExemplar = new Exemplar();
    dummyExemplar.setNumber( 3 );
    dummyExemplar.setIsOriginal( true );
    dummyExemplar.setStatusCode( "sent_to_the_report" );
    dummyExemplar.setAddressedToId( "58f88dfc776b000026000001" );
    dummyExemplar.setAddressedToName( "Сотрудник_а2 A.T." );
    dummyExemplar.setDate( "2017-07-24" );
  }

  @Test
  public void toEntity() {
    mapper = new ExemplarMapper();
    entity = mapper.toEntity(dummyExemplar);

    assertNotNull( entity );
    assertEquals( 0, entity.getId() );
    assertEquals( String.valueOf(dummyExemplar.getNumber()), entity.getNumber() );
    assertEquals( dummyExemplar.getIsOriginal(), entity.isIsOriginal() );
    assertEquals( dummyExemplar.getStatusCode(), entity.getStatusCode() );
    assertEquals( dummyExemplar.getAddressedToId(), entity.getAddressedToId() );
    assertEquals( dummyExemplar.getAddressedToName(), entity.getAddressedToName() );
    assertEquals( dummyExemplar.getDate(), entity.getDate() );
  }

  @Test
  public void toModel() {
    mapper = new ExemplarMapper();
    entity = mapper.toEntity(dummyExemplar);
    model = mapper.toModel(entity);

    assertNotNull( model );
    assertEquals( dummyExemplar.getNumber(), model.getNumber() );
    assertEquals( dummyExemplar.getIsOriginal(), model.getIsOriginal() );
    assertEquals( dummyExemplar.getStatusCode(), model.getStatusCode() );
    assertEquals( dummyExemplar.getAddressedToId(), model.getAddressedToId() );
    assertEquals( dummyExemplar.getAddressedToName(), model.getAddressedToName() );
    assertEquals( dummyExemplar.getDate(), model.getDate() );
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

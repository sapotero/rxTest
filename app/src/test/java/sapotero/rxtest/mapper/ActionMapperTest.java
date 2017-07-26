package sapotero.rxtest.mapper;


import org.junit.Before;
import org.junit.Test;

import sapotero.rxtest.db.mapper.ActionMapper;
import sapotero.rxtest.db.requery.models.actions.RActionEntity;
import sapotero.rxtest.retrofit.models.document.DocumentInfoAction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ActionMapperTest {

  private ActionMapper mapper;
  private DocumentInfoAction dummyAction;
  private RActionEntity entity;
  private DocumentInfoAction model;

  @Before
  public void init() {
    dummyAction = generateAction();
  }

  public static DocumentInfoAction generateAction() {
    DocumentInfoAction dummyAction = new DocumentInfoAction();
    dummyAction.setOfficialId( "J78hsdkjfh8WE23ewfs" );
    dummyAction.setAddressedToId( "kjds7jhWEKLdf34DFSGFDGdf" );
    dummyAction.setAction( "primary_consideration" );
    dummyAction.setActionDescription( "Отправлен на первичное рассмотрение" );
    dummyAction.setUpdatedAt( "2017-07-24" );
    dummyAction.setToS( "kjshfdkjshf" );
    return dummyAction;
  }

  @Test
  public void toEntity() {
    mapper = new ActionMapper();
    entity = mapper.toEntity(dummyAction);

    verifyAction( dummyAction, entity );
  }

  public static void verifyAction(DocumentInfoAction expected, RActionEntity actual) {
    assertNotNull( actual );
    assertEquals( 0, actual.getId() );
    assertEquals( expected.getOfficialId(), actual.getOfficialId() );
    assertEquals( expected.getAddressedToId(), actual.getAddressedToId() );
    assertEquals( expected.getAction(), actual.getAction() );
    assertEquals( expected.getActionDescription(), actual.getActionDescription() );
    assertEquals( expected.getUpdatedAt(), actual.getUpdatedAt() );
    assertEquals( expected.getToS(), actual.getToS() );
  }

  @Test
  public void toModel() {
    mapper = new ActionMapper();
    entity = mapper.toEntity(dummyAction);
    model = mapper.toModel(entity);

    verifyAction( dummyAction, model );
  }

  public static void verifyAction(DocumentInfoAction expected, DocumentInfoAction actual) {
    assertNotNull( actual );
    assertEquals( expected.getOfficialId(), actual.getOfficialId() );
    assertEquals( expected.getAddressedToId(), actual.getAddressedToId() );
    assertEquals( expected.getAction(), actual.getAction() );
    assertEquals( expected.getActionDescription(), actual.getActionDescription() );
    assertEquals( expected.getUpdatedAt(), actual.getUpdatedAt() );
    assertEquals( expected.getToS(), actual.getToS() );
  }

  @Test
  public void hasDiff() {
    mapper = new ActionMapper();

    RActionEntity entity1 = mapper.toEntity(dummyAction);
    RActionEntity entity2 = mapper.toEntity(dummyAction);

    boolean hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setOfficialId( "" );
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }
}

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

    assertNotNull( entity );
    assertEquals( 0, entity.getId() );
    assertEquals( dummyAction.getOfficialId(), entity.getOfficialId() );
    assertEquals( dummyAction.getAddressedToId(), entity.getAddressedToId() );
    assertEquals( dummyAction.getAction(), entity.getAction() );
    assertEquals( dummyAction.getActionDescription(), entity.getActionDescription() );
    assertEquals( dummyAction.getUpdatedAt(), entity.getUpdatedAt() );
    assertEquals( dummyAction.getToS(), entity.getToS() );
  }

  @Test
  public void toModel() {
    mapper = new ActionMapper();
    entity = mapper.toEntity(dummyAction);
    model = mapper.toModel(entity);

    assertNotNull( model );
    assertEquals( dummyAction.getOfficialId(), model.getOfficialId() );
    assertEquals( dummyAction.getAddressedToId(), model.getAddressedToId() );
    assertEquals( dummyAction.getAction(), model.getAction() );
    assertEquals( dummyAction.getActionDescription(), model.getActionDescription() );
    assertEquals( dummyAction.getUpdatedAt(), model.getUpdatedAt() );
    assertEquals( dummyAction.getToS(), model.getToS() );
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

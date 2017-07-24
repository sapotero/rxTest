package sapotero.rxtest.mapper;


import org.junit.Before;
import org.junit.Test;

import sapotero.rxtest.db.mapper.ActionMapper;
import sapotero.rxtest.db.mapper.ControlLabelMapper;
import sapotero.rxtest.db.requery.models.actions.RActionEntity;
import sapotero.rxtest.db.requery.models.control_labels.RControlLabelsEntity;
import sapotero.rxtest.retrofit.models.document.ControlLabel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ControlLabelMapperTest {

  private ControlLabelMapper mapper;
  private ControlLabel dummyControlLabel;
  private RControlLabelsEntity entity;
  private ControlLabel model;

  @Before
  public void init() {
    generateControlLabel();
  }

  private void generateControlLabel() {
    dummyControlLabel = new ControlLabel();
    dummyControlLabel.setCreatedAt( "2017-07-24" );
    dummyControlLabel.setOfficialId( "58f88dfc776b000026000001" );
    dummyControlLabel.setOfficialName( "Сотрудник_а2 A.T." );
    dummyControlLabel.setSkippedOfficialId( "dkjsbfdskjhfdkj" );
    dummyControlLabel.setSkippedOfficialName( "Сотрудник_а3 A.T." );
    dummyControlLabel.setState( "Отмечен для постановки на контроль" );
  }

  @Test
  public void toEntity() {
    mapper = new ControlLabelMapper();
    entity = mapper.toEntity(dummyControlLabel);

    assertNotNull( entity );
    assertEquals( 0, entity.getId() );
    assertEquals( dummyControlLabel.getCreatedAt(), entity.getCreatedAt() );
    assertEquals( dummyControlLabel.getOfficialId(), entity.getOfficialId() );
    assertEquals( dummyControlLabel.getOfficialName(), entity.getOfficialName() );
    assertEquals( dummyControlLabel.getSkippedOfficialId(), entity.getSkippedOfficialId() );
    assertEquals( dummyControlLabel.getSkippedOfficialName(), entity.getSkippedOfficialName() );
    assertEquals( dummyControlLabel.getState(), entity.getState() );
  }

  @Test
  public void toModel() {
    mapper = new ControlLabelMapper();
    entity = mapper.toEntity(dummyControlLabel);
    model = mapper.toModel(entity);

    assertNotNull( model );
    assertEquals( dummyControlLabel.getCreatedAt(), model.getCreatedAt() );
    assertEquals( dummyControlLabel.getOfficialId(), model.getOfficialId() );
    assertEquals( dummyControlLabel.getOfficialName(), model.getOfficialName() );
    assertEquals( dummyControlLabel.getSkippedOfficialId(), model.getSkippedOfficialId() );
    assertEquals( dummyControlLabel.getSkippedOfficialName(), model.getSkippedOfficialName() );
    assertEquals( dummyControlLabel.getState(), model.getState() );
  }

  @Test
  public void hasDiff() {
    mapper = new ControlLabelMapper();

    RControlLabelsEntity entity1 = mapper.toEntity(dummyControlLabel);
    RControlLabelsEntity entity2 = mapper.toEntity(dummyControlLabel);

    boolean hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setOfficialId( "" );
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }
}

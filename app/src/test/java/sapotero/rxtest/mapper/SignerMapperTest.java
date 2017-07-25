package sapotero.rxtest.mapper;

import org.junit.Before;
import org.junit.Test;

import sapotero.rxtest.db.mapper.ActionMapper;
import sapotero.rxtest.db.mapper.SignerMapper;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.db.requery.models.actions.RActionEntity;
import sapotero.rxtest.retrofit.models.document.Signer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SignerMapperTest {

  private SignerMapper mapper;
  private Signer dummySigner;
  private RSignerEntity entity;
  private Signer model;

  @Before
  public void init() {
    generateSigner();
  }

  private void generateSigner() {
    dummySigner = new Signer();
    dummySigner.setId( "RF34frkfjV9sjhd34rgfd" );
    dummySigner.setName( "Иванов И.И." );
    dummySigner.setOrganisation( "ОДиР ГУ МВД России по Самарской области" );
    dummySigner.setType( "mvd_person" );
  }

  @Test
  public void toEntity() {
    mapper = new SignerMapper();
    entity = mapper.toEntity(dummySigner);

    assertNotNull( entity );
    assertEquals( 0, entity.getId() );
    assertEquals( dummySigner.getId(), entity.getUid() );
    assertEquals( dummySigner.getName(), entity.getName() );
    assertEquals( dummySigner.getOrganisation(), entity.getOrganisation() );
    assertEquals( dummySigner.getType(), entity.getType() );
  }

  @Test
  public void toModel() {
    mapper = new SignerMapper();
    entity = mapper.toEntity(dummySigner);
    model = mapper.toModel(entity);

    assertNotNull( model );
    assertEquals( dummySigner.getId(), model.getId() );
    assertEquals( dummySigner.getName(), model.getName() );
    assertEquals( dummySigner.getOrganisation(), model.getOrganisation() );
    assertEquals( dummySigner.getType(), model.getType() );
  }

  @Test
  public void hasDiff() {
    mapper = new SignerMapper();

    RSignerEntity entity1 = mapper.toEntity(dummySigner);
    RSignerEntity entity2 = mapper.toEntity(dummySigner);

    boolean hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setName( "" );
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }
}

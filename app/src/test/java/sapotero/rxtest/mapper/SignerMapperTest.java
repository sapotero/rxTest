package sapotero.rxtest.mapper;

import org.junit.Before;
import org.junit.Test;

import sapotero.rxtest.db.mapper.SignerMapper;
import sapotero.rxtest.db.requery.models.RSignerEntity;
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
    dummySigner = generateSigner();
  }

  public static Signer generateSigner() {
    Signer dummySigner = new Signer();
    dummySigner.setId( "RF34frkfjV9sjhd34rgfd" );
    dummySigner.setName( "Иванов И.И." );
    dummySigner.setOrganisation( "ОДиР ГУ МВД России по Самарской области" );
    dummySigner.setType( "mvd_person" );
    return dummySigner;
  }

  @Test
  public void toEntity() {
    mapper = new SignerMapper();
    entity = mapper.toEntity(dummySigner);

    verifySigner( dummySigner, entity );
  }

  public static void verifySigner(Signer expected, RSignerEntity actual) {
    assertNotNull( actual );
    assertEquals( 0, actual.getId() );
    assertEquals( expected.getId(), actual.getUid() );
    assertEquals( expected.getName(), actual.getName() );
    assertEquals( expected.getOrganisation(), actual.getOrganisation() );
    assertEquals( expected.getType(), actual.getType() );
  }

  @Test
  public void toModel() {
    mapper = new SignerMapper();
    entity = mapper.toEntity(dummySigner);
    model = mapper.toModel(entity);

    verifySigner( dummySigner, model );
  }

  public static void verifySigner(Signer expected, Signer actual) {
    assertNotNull( actual );
    assertEquals( expected.getId(), actual.getId() );
    assertEquals( expected.getName(), actual.getName() );
    assertEquals( expected.getOrganisation(), actual.getOrganisation() );
    assertEquals( expected.getType(), actual.getType() );
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

package sapotero.rxtest.mapper;

import org.junit.Before;
import org.junit.Test;

import sapotero.rxtest.db.mapper.LinkMapper;
import sapotero.rxtest.db.requery.models.RLinksEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


public class LinkMapperTest {

  private LinkMapper mapper;
  private String dummyLink;
  private String model;
  private RLinksEntity entity;

  @Before
  public void init() {
    mapper = new LinkMapper();
    dummyLink = generateLink();
  }

  public static String generateLink() {
    return "025e937f2dffce50fee6fcd69cfe7daf48b333ddf29fe634d89cae907d7c42c409";
  }

  @Test
  public void toEntity() {
    entity = mapper.toEntity(dummyLink);

    verifyLink( dummyLink, entity );
  }

  public static void verifyLink(String expected, RLinksEntity actual) {
    assertNotNull( actual );
    assertEquals( 0, actual.getId() );
    assertEquals( expected, actual.getUid() );
  }

  @Test
  public void toModel() {
    entity = mock(RLinksEntity.class);
    when(entity.getUid()).thenReturn(dummyLink);

    model = null;
    model = mapper.toModel(entity);

    verify(entity, atLeastOnce()).getUid();
    verify(entity, never()).setUid(anyString());
    verifyNoMoreInteractions(entity);

    verifyLink( dummyLink, model );
  }

  public static void verifyLink(String dummyLink, String model) {
    assertNotNull( model );
    assertEquals( dummyLink, model );
  }

  @Test
  public void hasDiff() {
    RLinksEntity entity1 = new RLinksEntity();
    RLinksEntity entity2 = new RLinksEntity();
    boolean hasDiff;

    entity1.setUid(dummyLink);
    entity2.setUid(dummyLink);
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setUid("");
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }
}

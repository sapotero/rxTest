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
  private String link;
  private String model;
  private RLinksEntity entity;

  @Before
  public void init() {
    mapper = new LinkMapper();
    generateLink();
  }

  private void generateLink() {
    link = "025e937f2dffce50fee6fcd69cfe7daf48b333ddf29fe634d89cae907d7c42c409";
  }

  @Test
  public void toEntity() {
    model = link;

    entity = mapper.toEntity(model);

    assertNotNull( entity );
    assertEquals( 0, entity.getId() );
    assertEquals( model, entity.getUid() );
  }

  @Test
  public void toModel() {
    entity = mock(RLinksEntity.class);
    when(entity.getUid()).thenReturn(link);

    model = null;
    model = mapper.toModel(entity);

    assertNotNull( model );
    assertEquals( link, model );

    verify(entity, atLeastOnce()).getUid();
    verify(entity, never()).setUid(anyString());
    verifyNoMoreInteractions(entity);
  }

  @Test
  public void hasDiff() {
    RLinksEntity entity1 = new RLinksEntity();
    RLinksEntity entity2 = new RLinksEntity();
    boolean hasDiff;

    entity1.setUid(link);
    entity2.setUid(link);
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertFalse( hasDiff );

    entity2.setUid("");
    hasDiff = mapper.hasDiff(entity1, entity2);

    assertTrue( hasDiff );
  }
}

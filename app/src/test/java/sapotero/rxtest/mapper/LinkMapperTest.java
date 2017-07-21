package sapotero.rxtest.mapper;

import org.junit.Before;
import org.junit.Test;

import sapotero.rxtest.db.mapper.LinkMapper;
import sapotero.rxtest.db.requery.models.RLinksEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    assertEquals( entity.getId(), 0 );
    assertEquals( entity.getUid(), model );
  }

  @Test
  public void toModel() {
    entity = mock(RLinksEntity.class);
    when(entity.getUid()).thenReturn(link);

    model = null;
    model = mapper.toModel(entity);

    assertNotNull( model );
    assertEquals( model, link );

    verify(entity, atLeastOnce()).getUid();
    verify(entity, never()).setUid(anyString());
    verifyNoMoreInteractions(entity);
  }
}

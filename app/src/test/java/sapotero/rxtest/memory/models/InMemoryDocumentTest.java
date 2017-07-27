package sapotero.rxtest.memory.models;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import rx.android.plugins.RxAndroidPlugins;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.fields.InMemoryState;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;

public class InMemoryDocumentTest {

  //Field year of type Integer - was not mocked since Mockito doesn't mock a Final class
  //Field hasDecision of type Boolean - was not mocked since Mockito doesn't mock a Final class
  //Field processed of type Boolean - was not mocked since Mockito doesn't mock a Final class
  //Field allowUpdate of type Boolean - was not mocked since Mockito doesn't mock a Final class

  @Mock Document document;
  @InjectMocks InMemoryDocument inMemoryDocument;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void tearDown() throws Exception {
    RxAndroidPlugins.getInstance().reset();
  }
  @Test
  public void testIsProcessed() throws Exception {
    Boolean result = inMemoryDocument.isProcessed();
    Assert.assertEquals(Boolean.FALSE, result);
  }

  @Test
  public void testHasDecision() throws Exception {
    Boolean result = inMemoryDocument.hasDecision();
    Assert.assertEquals(Boolean.FALSE, result);
  }

  @Test
  public void testSetAsLoading() throws Exception {
    inMemoryDocument.setAsLoading();
    Assert.assertEquals(inMemoryDocument.getState(), InMemoryState.LOADING);
  }

  @Test
  public void testSetAsReady() throws Exception {
    inMemoryDocument.setAsReady();
    Assert.assertEquals(inMemoryDocument.getState(), InMemoryState.READY);
  }

  @Test
  public void testIsAllowUpdate() throws Exception {
    Boolean result = inMemoryDocument.isAllowUpdate();
    Assert.assertEquals(Boolean.TRUE, result);
  }
}
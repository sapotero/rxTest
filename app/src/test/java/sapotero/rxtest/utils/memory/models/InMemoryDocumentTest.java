package sapotero.rxtest.utils.memory.models;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import sapotero.rxtest.retrofit.models.documents.Document;

/**
 * Created by sapotero on 25.07.17.
 */
public class InMemoryDocumentTest {
  //Field year of type Integer - was not mocked since Mockito doesn't mock a Final class
  //Field hasDecision of type Boolean - was not mocked since Mockito doesn't mock a Final class
  //Field processed of type Boolean - was not mocked since Mockito doesn't mock a Final class
  //Field allowUpdate of type Boolean - was not mocked since Mockito doesn't mock a Final class
  @Mock
  Document document;
  //Field state of type InMemoryState - was not mocked since Mockito doesn't mock enums
  @InjectMocks
  InMemoryDocument inMemoryDocument;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testIsProcessed() throws Exception {
    Boolean result = inMemoryDocument.isProcessed();
    Assert.assertEquals(Boolean.TRUE, result);
  }

  @Test
  public void testHasDecision() throws Exception {
    Boolean result = inMemoryDocument.hasDecision();
    Assert.assertEquals(Boolean.TRUE, result);
  }

  @Test
  public void testSetAsLoading() throws Exception {
    inMemoryDocument.setAsLoading();
  }

  @Test
  public void testSetAsReady() throws Exception {
    inMemoryDocument.setAsReady();
  }

  @Test
  public void testToString() throws Exception {
    String result = inMemoryDocument.toString();
    Assert.assertEquals("replaceMeWithExpectedResult", result);
  }

  @Test
  public void testIsAllowUpdate() throws Exception {
    Boolean result = inMemoryDocument.isAllowUpdate();
    Assert.assertEquals(Boolean.TRUE, result);
  }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme
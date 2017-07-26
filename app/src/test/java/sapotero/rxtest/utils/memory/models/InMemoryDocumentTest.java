package sapotero.rxtest.utils.memory.models;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import sapotero.rxtest.retrofit.models.documents.Document;

public class InMemoryDocumentTest {

  @Mock Document document;
  @InjectMocks InMemoryDocument inMemoryDocument;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
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
  }

  @Test
  public void testSetAsReady() throws Exception {
    inMemoryDocument.setAsReady();
  }

  @Test
  public void testIsAllowUpdate() throws Exception {
    Boolean result = inMemoryDocument.isAllowUpdate();
    Assert.assertEquals(Boolean.TRUE, result);
  }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme
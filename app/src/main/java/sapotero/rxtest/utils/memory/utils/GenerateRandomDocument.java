package sapotero.rxtest.utils.memory.utils;

import sapotero.rxtest.utils.memory.InMemoryDocumentStorage;

public class GenerateRandomDocument implements Runnable {

  private final InMemoryDocumentStorage publish;

  public GenerateRandomDocument(InMemoryDocumentStorage publish) {
    this.publish = publish;
  }

  @Override
  public void run() {
    publish.generateFakeDocumentEntity();
  }
}
package sapotero.rxtest.events.document;

import sapotero.rxtest.utils.memory.models.InMemoryDocument;


public class UpdateIMDEvent {
  public final InMemoryDocument doc;

  public UpdateIMDEvent(InMemoryDocument doc) {
    this.doc = doc;
  }
}

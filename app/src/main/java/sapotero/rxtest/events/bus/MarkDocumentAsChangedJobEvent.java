package sapotero.rxtest.events.bus;

public class MarkDocumentAsChangedJobEvent {
  public final String uid;

  public MarkDocumentAsChangedJobEvent(String uid) {
    this.uid = uid;
  }
}

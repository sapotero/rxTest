package sapotero.rxtest.events.adapter;

public class UpdateDocumentAdapterEvent {
  public final String uid;
  public final String type;
  public final String status;

  public UpdateDocumentAdapterEvent(String uid, String type, String status) {
    this.uid    = uid;
    this.type   = type;
    this.status = status;
  }
}

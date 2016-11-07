package sapotero.rxtest.events.bus;

public class UpdateDocumentJobEvent {
  public final String uid;
  public final String field;
  public final Boolean value;

  public UpdateDocumentJobEvent(String uid, String field, Boolean value) {
    this.uid = uid;
    this.field = field;
    this.value = value;
  }


}

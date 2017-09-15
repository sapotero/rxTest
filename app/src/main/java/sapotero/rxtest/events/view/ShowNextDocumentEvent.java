package sapotero.rxtest.events.view;

/**
 * Created by sapotero on 02.03.17.
 */

public class ShowNextDocumentEvent {
  private String uid;

  public ShowNextDocumentEvent(String uid) {
    this.uid = uid;
  }

  public String getUid() {
    return uid;
  }
}

package sapotero.rxtest.events.view;

/**
 * Created by sapotero on 02.03.17.
 */

public class ShowNextDocumentEvent {
  private boolean removeUid;
  private String uid;

  public ShowNextDocumentEvent(boolean removeUid, String uid) {
    this.removeUid = removeUid;
    this.uid = uid;
  }

  public boolean isRemoveUid() {
    return removeUid;
  }

  public String getUid() {
    return uid;
  }
}

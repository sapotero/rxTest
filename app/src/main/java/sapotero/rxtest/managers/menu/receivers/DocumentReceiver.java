package sapotero.rxtest.managers.menu.receivers;


public class DocumentReceiver {
  private final String uid;

  public DocumentReceiver(String active_uid) {
    uid = active_uid;
  }

  public String getUid() {
    return uid;
  }
}

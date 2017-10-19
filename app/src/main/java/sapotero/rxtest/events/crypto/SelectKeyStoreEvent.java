package sapotero.rxtest.events.crypto;
public class SelectKeyStoreEvent {
  public String data;
  public int index;

  public SelectKeyStoreEvent(String data, int index) {
    this.data = data;
    this.index = index;
  }
}

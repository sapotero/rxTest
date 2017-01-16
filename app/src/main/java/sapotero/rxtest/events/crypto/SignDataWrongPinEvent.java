package sapotero.rxtest.events.crypto;
public class SignDataWrongPinEvent {
  public String data;

  public SignDataWrongPinEvent(String data) {
    this.data = data;
  }
}

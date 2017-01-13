package sapotero.rxtest.events.auth;

public class AuthDcCheckFailEvent {
  public String error;
  public AuthDcCheckFailEvent(String error) {
    this.error = error;
  }
}

package sapotero.rxtest.events.auth;

public class AuthLoginCheckFailEvent {
  public String error;
  public AuthLoginCheckFailEvent(String error) {
    this.error = error;
  }
}

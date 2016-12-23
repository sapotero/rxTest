package sapotero.rxtest.events.service;

public class AuthServiceAuthEvent {
  public Boolean success;
  public String  success_string;

  public AuthServiceAuthEvent(Boolean path) {
    this.success = path;
  }

  public AuthServiceAuthEvent(String data) {
    this.success_string = data;
  }
}

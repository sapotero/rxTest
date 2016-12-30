package sapotero.rxtest.events.service;

public class AuthServiceAuthSignInEvent {
  public String password;

  public AuthServiceAuthSignInEvent(String password) {
    this.password = password;
  }
}

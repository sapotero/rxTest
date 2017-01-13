package sapotero.rxtest.events.stepper.auth;

public class StepperLoginCheckEvent {
  public String login;
  public String password;
  public String host;

  public StepperLoginCheckEvent(String login, String password, String host) {
    this.login    = login;
    this.password = password;
    this.host = host;
  }
}

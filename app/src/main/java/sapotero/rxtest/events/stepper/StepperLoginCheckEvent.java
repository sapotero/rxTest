package sapotero.rxtest.events.stepper;

public class StepperLoginCheckEvent {
  public String login;
  public String password;

  public StepperLoginCheckEvent(String login, String password) {
    this.login    = login;
    this.password = password;
  }
}

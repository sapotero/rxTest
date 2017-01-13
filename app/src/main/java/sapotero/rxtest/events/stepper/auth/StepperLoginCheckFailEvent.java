package sapotero.rxtest.events.stepper.auth;

public class StepperLoginCheckFailEvent {
  public String error;
  public StepperLoginCheckFailEvent(String error){
    this.error = error;
  }
}

package sapotero.rxtest.events.stepper.auth;

public class StepperDcCheckFailEvent {
  public String error;

  public StepperDcCheckFailEvent( String error){
    this.error = error;
  }
}

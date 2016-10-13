package sapotero.rxtest.events.bus;

public class UpdateAuthTokenEvent {
  public final String message;

  public UpdateAuthTokenEvent(String message){
    this.message = message;
  }
}

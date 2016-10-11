package sapotero.rxtest.events.bus;


public class MassInsertDoneEvent {
  public final String message;

  public MassInsertDoneEvent(String message) {
    this.message = message;
  }
}

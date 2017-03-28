package sapotero.rxtest.events.decision;


public class SignAfterCreateEvent {
  public final String uid;

  public SignAfterCreateEvent(String uid) {
    this.uid = uid;
  }
}

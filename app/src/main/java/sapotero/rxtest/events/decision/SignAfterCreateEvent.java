package sapotero.rxtest.events.decision;


public class SignAfterCreateEvent {
  public final String uid;
  public final Boolean assignment;

  public SignAfterCreateEvent(String uid, Boolean assignment) {
    this.uid = uid;
    this.assignment = assignment;
  }
}

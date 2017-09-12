package sapotero.rxtest.events.decision;

public class DecisionVisibilityEvent {
  public Boolean approved;
  public String uid;

  public DecisionVisibilityEvent(Boolean approved, String uid) {
    this.approved = approved;
    this.uid = uid;
  }

  @Override
  public String toString() {
    return "DecisionVisibilityEvent{" +
      "approved=" + approved +
      ", uid='" + uid + '\'' +
      '}';
  }
}

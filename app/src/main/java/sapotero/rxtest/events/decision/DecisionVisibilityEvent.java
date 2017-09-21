package sapotero.rxtest.events.decision;

public class DecisionVisibilityEvent {
  public Boolean approved;
  public String uid;
  public Boolean hideEditDecision;

  public DecisionVisibilityEvent(Boolean approved, String uid, Boolean hideEditDecision) {
    this.approved = approved;
    this.uid = uid;
    this.hideEditDecision = hideEditDecision;
  }

  @Override
  public String toString() {
    return "DecisionVisibilityEvent{" +
      "approved=" + approved +
      ", uid='" + uid + '\'' +
      '}';
  }
}

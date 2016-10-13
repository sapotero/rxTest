package sapotero.rxtest.events.bus;

public class SetActiveDecisonEvent {

  public final Integer decision;

  public SetActiveDecisonEvent(Integer DECISION) {
    this.decision = DECISION;
  }
}

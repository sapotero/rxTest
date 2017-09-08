package sapotero.rxtest.events.bus;

public class StartRegularRefreshEvent {

  // true - start network checking,
  // false - stop network checking
  boolean start;

  public StartRegularRefreshEvent(boolean start) {
    this.start = start;
  }

  public boolean isStart() {
    return start;
  }
}

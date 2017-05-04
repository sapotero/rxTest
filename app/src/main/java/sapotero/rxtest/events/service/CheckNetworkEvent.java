package sapotero.rxtest.events.service;

// Starts / stops network checking
public class CheckNetworkEvent {

  // true - start network checking,
  // false - stop network checking
  boolean start;

  public CheckNetworkEvent(boolean start) {
    this.start = start;
  }

  public boolean isStart() {
    return start;
  }
}

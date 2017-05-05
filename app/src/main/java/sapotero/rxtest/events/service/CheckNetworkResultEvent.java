package sapotero.rxtest.events.service;

// Delivers network checking result
public class CheckNetworkResultEvent {

  boolean connected;

  public CheckNetworkResultEvent(boolean connected) {
    this.connected = connected;
  }

  public boolean isConnected() {
    return connected;
  }
}

package sapotero.rxtest.events.bus;

public class FolderCreatedEvent {
  String type;

  public FolderCreatedEvent(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}

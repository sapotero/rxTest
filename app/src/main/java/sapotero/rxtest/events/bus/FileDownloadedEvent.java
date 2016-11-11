package sapotero.rxtest.events.bus;

public class FileDownloadedEvent {
  public String path;

  public FileDownloadedEvent(String absolutePath) {
    this.path = absolutePath;
  }
}

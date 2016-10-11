package sapotero.rxtest.events.bus;

/**
 * Created by sapotero on 04.10.16.
 */

public class GetDocumentInfoEvent {

  public final String message;

  public GetDocumentInfoEvent(String message) {
    this.message = message;
  }
}

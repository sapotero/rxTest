package sapotero.rxtest.events.view;

/**
 * Created by sapotero on 30.01.17.
 */

public class ShowSnackEvent {
  public final String message;

  public ShowSnackEvent(String message) {
    this.message = message;
  }
}

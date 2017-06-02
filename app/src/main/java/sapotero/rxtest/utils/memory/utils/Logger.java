package sapotero.rxtest.utils.memory.utils;

import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import timber.log.Timber;

public class Logger {
  private String TAG = this.getClass().getSimpleName();

  public Logger() {
  }

  public void log(InMemoryDocument inMemoryDocument) {
    if (inMemoryDocument != null) {
      Timber.tag(TAG).d( inMemoryDocument.toString() );
    }
  }
}

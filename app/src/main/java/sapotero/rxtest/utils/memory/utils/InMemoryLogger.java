package sapotero.rxtest.utils.memory.utils;

import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import timber.log.Timber;

public class InMemoryLogger {
  private String TAG = this.getClass().getSimpleName();

  public InMemoryLogger() {
  }

  public void log(InMemoryDocument inMemoryDocument) {
    Timber.tag(TAG).d( inMemoryDocument.toString() );
  }
}

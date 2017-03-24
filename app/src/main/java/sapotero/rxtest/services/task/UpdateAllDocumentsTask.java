package sapotero.rxtest.services.task;

import android.content.Context;

import sapotero.rxtest.managers.DataLoaderManager;
import sapotero.rxtest.views.menu.fields.MainMenuItem;
import timber.log.Timber;

public class UpdateAllDocumentsTask implements Runnable {

  private DataLoaderManager dataLoaderInterface;

  public UpdateAllDocumentsTask(Context context) {
    Timber.tag("UpdateAllDocumentsTask").i("start");
    dataLoaderInterface = new DataLoaderManager(context);
  }

  @Override
  public void run() {
    dataLoaderInterface.updateByStatus(MainMenuItem.ALL);
  }
}
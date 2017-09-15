package sapotero.rxtest.services.task;

import javax.inject.Inject;

import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.managers.DataLoaderManager;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.views.menu.fields.MainMenuItem;
import timber.log.Timber;

public class UpdateAllDocumentsTask implements Runnable {

  @Inject ISettings settings;

  private DataLoaderManager dataLoaderInterface;

  public UpdateAllDocumentsTask() {
    Timber.tag("UpdateAllDocumentsTask").i("start");
    EsdApplication.getDataComponent().inject(this);
    dataLoaderInterface = new DataLoaderManager();
  }

  @Override
  public void run() {
    dataLoaderInterface.updateByCurrentStatus(MainMenuItem.ALL, null, settings.getLogin(), settings.getCurrentUserId());
    dataLoaderInterface.updateFavorites(true);
    dataLoaderInterface.updateProcessed(true);
  }
}
package sapotero.rxtest.services.task;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.events.view.UpdateMainActivityEvent;
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
    dataLoaderInterface.updateByCurrentStatus(MainMenuItem.ALL, null);
    dataLoaderInterface.updateFavorites(true);
    dataLoaderInterface.updateProcessed(true);
    EventBus.getDefault().post( new UpdateMainActivityEvent() );
  }
}
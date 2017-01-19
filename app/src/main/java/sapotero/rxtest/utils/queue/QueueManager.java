package sapotero.rxtest.utils.queue;

import android.content.Context;

import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.queue.db.QueueDBManager;
import sapotero.rxtest.utils.queue.threads.QueueSupervisor;
import sapotero.rxtest.views.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.views.managers.menu.interfaces.Command;
import timber.log.Timber;

public class QueueManager {



  private final Context context;
  private final QueueDBManager DBManager;
  private QueueSupervisor supervisor;

  private Boolean isConnectedToInternet = false;

  public QueueManager(Context context) {
    this.context = context;

    EsdApplication.getComponent(context).inject(this);
    isConnectedToInternet();

    supervisor = new QueueSupervisor(context);
    DBManager  = new QueueDBManager(context);


  }


  public void add(Command command){
    DBManager.add( command );
    supervisor.add(command);
  }

  public void clean(){
    DBManager.clear();
  }

  public void remove(AbstractCommand command) {
    Timber.e("remove %s", command);
  }

  private void isConnectedToInternet() {
    ReactiveNetwork.observeInternetConnectivity()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(isConnectedToInternet -> {
        this.isConnectedToInternet = isConnectedToInternet;
      });
  }

  public Boolean getConnected() {
    return isConnectedToInternet;
  }

}

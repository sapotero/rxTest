package sapotero.rxtest.utils.queue;

import android.content.Context;

import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.queue.QueueEntity;
import sapotero.rxtest.utils.queue.db.QueueDBManager;
import sapotero.rxtest.utils.queue.threads.QueueSupervisor;
import sapotero.rxtest.views.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.views.managers.menu.interfaces.Command;
import timber.log.Timber;

public class QueueManager {

  private final Context context;
  private final QueueDBManager dBManager;
  private QueueSupervisor supervisor;

  private Boolean isConnectedToInternet = false;

  public QueueManager(Context context) {
    this.context = context;

    EsdApplication.getComponent(context).inject(this);
    isConnectedToInternet();

    supervisor = new QueueSupervisor(context);
    dBManager = new QueueDBManager(context);
  }

  public void add(Command command){
    dBManager.add( command );
//    supervisor.add(command);
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

  public void getUncompleteTasks(){
    Timber.e("main > getUncompleteTasks");

    List<QueueEntity> uncompleteLocalTasks  = dBManager.getUncompleteLocalTasks();
    List<QueueEntity> uncompleteRemoteTasks = dBManager.getUncompleteRemoteTasks();

    if ( uncompleteLocalTasks.size() > 0 ){
      for ( QueueEntity command : uncompleteLocalTasks ) {
        supervisor.add(command, true);
      }
    }

    if ( uncompleteRemoteTasks.size() > 0 ){
      for ( QueueEntity command : uncompleteRemoteTasks ) {
        supervisor.add(command, false);
      }
    }

  }

  public void setExecutedLocal(Command command) {
    dBManager.setExecutedLocal(command);
  }

  public void setExecutedRemote(Command command) {
    dBManager.setExecutedRemote(command);
  }

  public void removeAll() {
    dBManager.removeAll();
  }
}

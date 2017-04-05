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
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.interfaces.Command;
import timber.log.Timber;

import static sapotero.rxtest.utils.queue.threads.QueueSupervisor.THREAD_POOL_SIZE;

public class QueueManager {

  private final Context context;
  private final QueueDBManager dBManager;
  private QueueSupervisor supervisor;
  private final String TAG = this.getClass().getSimpleName();


  private Boolean isConnectedToInternet = false;

  public QueueManager(Context context) {
    this.context = context;

    EsdApplication.getComponent(context).inject(this);
    isConnectedToInternet();

    supervisor = new QueueSupervisor(context);
    dBManager  = new QueueDBManager(context);
  }

  public void add(Command command){
    dBManager.add( command );
  }


  public void remove(AbstractCommand command) {
    Timber.tag(TAG).e("remove %s", command);
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

    if (supervisor.getRunningJobsCount() < THREAD_POOL_SIZE){


      List<QueueEntity> uncompleteSignTasks  = dBManager.getUncompleteSignTasks(2);
      if ( uncompleteSignTasks.size() > 0 ){
        for ( QueueEntity command : uncompleteSignTasks ) {
          dBManager.setAsRunning(command.getUuid());
          supervisor.addLocal(command);
        }
      }

      List<QueueEntity> uncompleteLocalTasks  = dBManager.getUncompleteLocalTasks(THREAD_POOL_SIZE - supervisor.getRunningJobsCount());
      if ( uncompleteLocalTasks.size() > 0 ){
        for ( QueueEntity command : uncompleteLocalTasks ) {
          dBManager.setAsRunning(command.getUuid());
          supervisor.addLocal(command);
        }
      }

      List<QueueEntity> uncompleteRemoteTasks = dBManager.getUncompleteRemoteTasks(THREAD_POOL_SIZE - uncompleteLocalTasks.size());
      if ( uncompleteRemoteTasks.size() > 0 ){
        for ( QueueEntity command : uncompleteRemoteTasks ) {
          dBManager.setAsRunning(command.getUuid());
          supervisor.addRemote(command);
        }
      }

//      Timber.tag(TAG).e("getUncompleteTasks\nlocal: %s\nremote: %s\n", uncompleteLocalTasks.size(), uncompleteRemoteTasks.size() );
    }

    if ( dBManager.getRunningJobsCount() > supervisor.getRunningJobsCount() ){
      dBManager.dropRunningJobs();
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

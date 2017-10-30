package sapotero.rxtest.utils.queue;

import java.util.List;

import sapotero.rxtest.db.requery.models.queue.QueueEntity;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.utils.queue.db.QueueDBManager;
import sapotero.rxtest.utils.queue.db.QueueMemoryManager;
import sapotero.rxtest.utils.queue.interfaces.QueueRepository;
import sapotero.rxtest.utils.queue.threads.QueueSupervisor;
import timber.log.Timber;

import static sapotero.rxtest.utils.queue.threads.QueueSupervisor.THREAD_POOL_SIZE;

public class QueueManager implements QueueRepository {

  private final QueueDBManager     dBManager;
  private final QueueMemoryManager memoryManager;
  private final QueueSupervisor supervisor;
  private final String TAG = this.getClass().getSimpleName();

  public QueueManager() {

    supervisor    = new QueueSupervisor();
    dBManager     = new QueueDBManager();
    memoryManager = new QueueMemoryManager();
  }

  @Override
  public void add(Command command){
    dBManager.add( command );
    memoryManager.add( command );
  }

  @Override
  public void remove(AbstractCommand command) {
    Timber.tag(TAG).e("remove %s", command);
  }

  @Override
  public void setExecutedLocal(Command command) {
    dBManager.setExecutedLocal(command);
    memoryManager.setExecutedLocal(command);
  }

  @Override
  public void setExecutedRemote(Command command) {
    dBManager.setExecutedRemote(command);
    memoryManager.setExecutedRemote(command);
  }

  @Override
  public void setExecutedWithError(Command command, List<String> errors) {
    dBManager.setExecutedWithError(command, errors);
    memoryManager.setExecutedWithError(command, errors);
  }

  @Override
  public void setAsRunning(Command command) {
    dBManager.setAsRunning(command);
    memoryManager.setAsRunning(command);
  }

  public void getUncompleteTasks(){

    if (supervisor.getRunningJobsCount() < THREAD_POOL_SIZE){


      List<QueueEntity> uncompleteSignTasks  = dBManager.getUncompleteSignTasks(8);
      if ( uncompleteSignTasks.size() > 0 ){
        for ( QueueEntity command : uncompleteSignTasks ) {
          push(command, false);
        }
      }

      List<QueueEntity> uncompleteLocalTasks  = dBManager.getUncompleteLocalTasks(THREAD_POOL_SIZE - supervisor.getRunningJobsCount());
      if ( uncompleteLocalTasks.size() > 0 ){
        for ( QueueEntity command : uncompleteLocalTasks ) {
          push(command, false);
        }
      }

      List<QueueEntity> uncompleteRemoteTasks = dBManager.getUncompleteRemoteTasks(THREAD_POOL_SIZE - uncompleteLocalTasks.size());
      if ( uncompleteRemoteTasks.size() > 0 ){
        for ( QueueEntity command : uncompleteRemoteTasks ) {
          push(command, true);
        }
      }

    }

    if ( dBManager.getRunningJobsCount() > supervisor.getRunningJobsCount() ){
      dBManager.dropRunningJobs();
    }

  }

  private void push(QueueEntity command, Boolean remote) {
    Command cmd = supervisor.create(command);
    if (cmd != null) {
      dBManager.setAsRunning( cmd );

      if (remote){
        supervisor.addRemote(command);
      } else {
        supervisor.addLocal(command);
      }
    }
  }

  public void removeAll() {
    dBManager.removeAll();
  }

  public boolean isAllTasksComplete() {
    return dBManager.isAllTasksComplete();
  }
}

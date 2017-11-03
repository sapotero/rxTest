package sapotero.rxtest.utils.queue;

import java.util.List;

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

  public void init(){
    memoryManager.init( dBManager.getUncompleteTasks() );
  }

  @Override
  public void add(Command command){
    dBManager.add( command );
    memoryManager.add( command );
  }

  @Override
  public void remove(AbstractCommand command) {
    Timber.tag(TAG).e("remove %s", command);
    dBManager.remove( command );
    memoryManager.remove( command );
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
  public void setAsRunning(Command command, Boolean value) {
    dBManager.setAsRunning(command, value);
    memoryManager.setAsRunning(command, value);
  }

  public void getUncompleteTasks(){
    Timber.tag(TAG).d("getUncompleteTasks %s", supervisor.getRunningJobsCount() < THREAD_POOL_SIZE);

    if (supervisor.getRunningJobsCount() < THREAD_POOL_SIZE){

      for ( Command command : memoryManager.getUncompleteCommands(false) ) {
        push(command, false);
      }

      for ( Command command : memoryManager.getUncompleteCommands(true) ) {
        push(command, true);
      }
    }

    if ( dBManager.getRunningJobsCount() > supervisor.getRunningJobsCount() ){
      dBManager.dropRunningJobs();
    }

  }

  private void push(Command command, Boolean remote) {

    if (command != null) {
      dBManager.setAsRunning(command, true);
      memoryManager.setAsRunning(command, true);

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

  public void setUpdateDocumentCommandExecuted(String documentUid) {
    dBManager.setUpdateDocumentCommandExecuted( documentUid, false );
  }
}

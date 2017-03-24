package sapotero.rxtest.services.task;

import sapotero.rxtest.utils.queue.QueueManager;
import timber.log.Timber;

public class UpdateQueueTask implements Runnable {


  private final QueueManager queue;

  public UpdateQueueTask(QueueManager queue) {
    Timber.tag("UpdateQueueTask").i("start");
    this.queue = queue;
  }

  @Override
  public void run() {
    queue.getUncompleteTasks();
  }
}
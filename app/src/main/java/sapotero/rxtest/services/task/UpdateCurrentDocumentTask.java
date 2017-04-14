package sapotero.rxtest.services.task;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;

public class UpdateCurrentDocumentTask implements Runnable {

  private final String uid;

  public UpdateCurrentDocumentTask(String uid) {
    this.uid = uid;
  }

  @Override
  public void run() {
    if (uid != null) {
      EventBus.getDefault().post( new UpdateCurrentDocumentEvent(uid) );
    }
  }
}
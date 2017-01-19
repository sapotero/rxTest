package sapotero.rxtest.utils.queue;

import android.content.Context;

import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.queue.threads.QueueSupervisor;
import sapotero.rxtest.utils.queue.threads.consumers.DelayQueueCommandConsumer;
import sapotero.rxtest.utils.queue.threads.producers.DelayQueueCommandProducer;
import sapotero.rxtest.views.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.views.managers.menu.interfaces.Command;
import timber.log.Timber;

public class QueueManager {

  private final Context context;
  private DelayQueueCommandProducer producer;
  private DelayQueueCommandConsumer consumer;
  private Boolean isConnectedToInternet = false;
  private QueueSupervisor supervisor;

  public QueueManager(Context context) {
    this.context = context;

    EsdApplication.getComponent(context).inject(this);
    isConnectedToInternet();


    supervisor = new QueueSupervisor(context);


  }


  public void add(Command command){
    supervisor.add( command );
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

  public void start() {
  }

  public void stop() {
    supervisor.stop();
  }
}

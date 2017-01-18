package sapotero.rxtest.utils.queue;

import android.content.Context;

import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork;

import java.util.Iterator;
import java.util.concurrent.DelayQueue;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.queue.consumers.DelayQueueCommandConsumer;
import sapotero.rxtest.utils.queue.objects.DelayObject;
import sapotero.rxtest.utils.queue.objects.DelayedCommand;
import sapotero.rxtest.utils.queue.producers.DelayQueueCommandProducer;
import sapotero.rxtest.views.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.views.managers.menu.interfaces.Command;
import timber.log.Timber;

public class QueueManager {

  private final Context context;
  private DelayQueue queue;
  private DelayQueueCommandProducer producer;
  private DelayQueueCommandConsumer consumer;
  private Boolean isConnectedToInternet = false;

  public QueueManager(Context context) {
    this.context = context;

    EsdApplication.getComponent(context).inject(this);
  }


  public void start() {

    queue = new DelayQueue();

//    producer = new DelayQueueCommandProducer(queue, context);
//    producer.start();

    consumer = new DelayQueueCommandConsumer("Thread", queue, context);
    consumer.start();


  }

  public void stop(){
    Timber.e( "TASKS: %s", queue.size() );
    saveToDisc();

//    producer.stop();
    consumer.stop();
  }

  public void saveToDisc() {
    Iterator iterator = queue.iterator();
    while ( iterator.hasNext() ){
      DelayObject element = (DelayObject) iterator.next();
      Timber.e( "OBJECT: %s", element.toString() );
    }
  }

  public void add(Command command){
    DelayedCommand delayedCommand = new DelayedCommand(command, context, 1000L);
    queue.add( delayedCommand );
  }

  public Boolean getConnected() {
    return isConnectedToInternet;
  }

  private void isConnectedToInternet() {
    ReactiveNetwork.observeInternetConnectivity()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(isConnectedToInternet -> {
        this.isConnectedToInternet = isConnectedToInternet;
      });

  }

  public void remove(AbstractCommand command) {
    Timber.e("remove %s", command);
  }
}

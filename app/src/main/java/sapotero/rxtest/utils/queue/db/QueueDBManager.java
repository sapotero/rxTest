package sapotero.rxtest.utils.queue.db;

import android.content.Context;

import com.google.gson.Gson;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.QueueEntity;
import sapotero.rxtest.views.managers.menu.factories.CommandFactory;
import sapotero.rxtest.views.managers.menu.interfaces.Command;
import sapotero.rxtest.views.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class QueueDBManager {
  @Inject SingleEntityStore<Persistable> dataStore;

  private final Context context;
  private final CommandFactory commandFactory;
  private String TAG = this.getClass().getSimpleName();

  public QueueDBManager(Context context) {
    EsdApplication.getComponent(context).inject(this);

    this.context = context;
    this.commandFactory = new CommandFactory(context);
  }

  public void add(Command command){
    if (command != null && command.getParams() != null) {
      Timber.tag(TAG).v( "Command: %s\n\n Params: %s", command, command.getParams() );

      CommandParams params = command.getParams();

      Gson gson = new Gson();
      Calendar calendar  = Calendar.getInstance();
      Date now = calendar.getTime();

      QueueEntity task = new QueueEntity();
      task.setUuid( params.getUuid() );
      task.setCommand( command.getClass().getCanonicalName() );
      task.setParams(  gson.toJson(params) );
      task.setExecuted( false );
      task.setCreatedAt((int) new Timestamp(now.getTime()).getTime());

      int count = dataStore
        .count(QueueEntity.class)
        .where(QueueEntity.UUID.eq(params.getUuid()) )
        .get().value();

      if (count == 0){
        dataStore
          .insert(task)
          .toObservable()
          .subscribeOn(Schedulers.computation())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(data -> {
            Timber.tag(TAG).v("inserted %s [ %s ]", data.getCommand(), data.getId() );
          });
      } else {
        Timber.tag(TAG).v("UUID exist!");
      }


    }
  }

  public ArrayList<Command> getUncompleteTasks() {
    Timber.tag(TAG).e("getUncompleteTasks");
    ArrayList<Command> tasks = new ArrayList<>();

    List<QueueEntity> uncompleted = dataStore
      .select(QueueEntity.class)
      .where(QueueEntity.EXECUTED.eq(false))
      .get().toList();

    for ( QueueEntity task : uncompleted ) {

      Command command = create(task);
      Timber.tag("getUncompleteTasks").v("%s", new Gson().toJson(task) );

    }


    return tasks;
  }

  public Command create(QueueEntity task){

    CommandParams params = new Gson().fromJson( task.getParams(), CommandParams.class );
    String type = task.getCommand();

    // FIX переделать build
    Command command = commandFactory
      .withDocument( new DocumentReceiver( params.getDocument() ) )
      .withParams( params )
      .build( CommandFactory.Operation.getOperation( task.getCommand() ) );

    if (command != null) {
      Timber.tag("create").v("Command %s", command.getParams().toString() );
      command.execute();
    }


    return command;
  }

  public void clear(){
    Integer count = dataStore
      .delete(QueueEntity.class)
      .where(QueueEntity.EXECUTED.eq(false))
      .get().value();
    Timber.tag(TAG).d( "Deleted: %s", count );
  }
}

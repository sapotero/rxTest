package sapotero.rxtest.utils.queue.db;

import android.content.Context;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.queue.QueueEntity;
import sapotero.rxtest.views.managers.menu.interfaces.Command;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class QueueDBManager {
  @Inject SingleEntityStore<Persistable> dataStore;

  private final Context context;
  private String TAG = this.getClass().getSimpleName();

  public QueueDBManager(Context context) {
    EsdApplication.getComponent(context).inject(this);

    this.context = context;
  }

  public void add(Command command){
    if (command != null && command.getParams() != null) {
      Timber.tag(TAG).v( "Command: %s\n\n Params: %s", command, command.getParams() );

      CommandParams params = command.getParams();

      if ( params.getUuid() != null && !exist( params.getUuid() ) ){

        Gson gson = new Gson();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy в HH:mm");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());

        QueueEntity task = new QueueEntity();
        task.setUuid( params.getUuid() );
        task.setCommand( command.getClass().getCanonicalName() );
        task.setParams(  gson.toJson(params) );
        task.setExecuted( false );
        task.setLocal( false );
        task.setRemote( false );
        task.setCreatedAt( date );

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

  private Boolean exist(String uuid) {
    return dataStore
          .count(QueueEntity.class)
          .where(QueueEntity.UUID.eq( uuid ) )
          .get().value() > 0;
  }

  public List<QueueEntity> getUncompleteRemoteTasks() {
    return dataStore
      .select(QueueEntity.class)
      .where(QueueEntity.REMOTE.eq(false))
      .and( QueueEntity.LOCAL.eq(true) )
      .get().toList();
  }

  public List<QueueEntity> getUncompleteLocalTasks() {
    return dataStore
      .select(QueueEntity.class)
      .where(QueueEntity.LOCAL.eq(false))
      .get().toList();
  }

  public void setExecutedLocal(Command command) {
    if (command != null && command.getParams() != null) {
      CommandParams params = command.getParams();

      if ( params.getUuid() != null && exist( params.getUuid() ) ){
        int count = dataStore
          .update(QueueEntity.class)
          .set( QueueEntity.LOCAL, true )
          .get()
          .value();

        if ( count > 0 ){
          Timber.tag(TAG).i( "[%s] - updated local", params.getUuid() );
        }
      }
    }
  }

  public void setExecutedRemote(Command command) {
    if (command != null && command.getParams() != null) {
      CommandParams params = command.getParams();

      if ( params.getUuid() != null && exist( params.getUuid() ) ){
        int count = dataStore
          .update(QueueEntity.class)
          .set( QueueEntity.REMOTE, true )
          .get()
          .value();

        if ( count > 0 ){
          Timber.tag(TAG).i( "[%s] - updated local", params.getUuid() );
        }
      }
    }
  }

  public void removeAll() {
    int count = dataStore
      .delete(QueueEntity.class)
      .where(QueueEntity.UUID.ne(""))
      .get().value();
    Timber.tag(TAG).i("DELETED: %s", count);
  }
}

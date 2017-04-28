package sapotero.rxtest.utils.queue.db;

import android.content.Context;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.queue.QueueEntity;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.utils.queue.interfaces.JobCountInterface;
import timber.log.Timber;

public class QueueDBManager implements JobCountInterface {
  @Inject SingleEntityStore<Persistable> dataStore;

  private final Context context;
  private String TAG = this.getClass().getSimpleName();

  public QueueDBManager(Context context) {
    EsdApplication.getDataComponent().inject(this);

    this.context = context;
  }

  public void add(Command command){
    if (command != null && command.getParams() != null) {
      Timber.tag(TAG).i( "\n----------- ADD ------------\n%s\n Params: %s", command, command.getParams() );

      CommandParams params = command.getParams();
      String commandClass = command.getClass().getCanonicalName();

      // обновим все резолюции и пометим их как отменённые
      if ( params.getUuid() != null && ( commandClass.endsWith("SaveDecision")) ){
        Decision decision = params.getDecisionModel();
        setAsCanceled( decision.getId()  );
      }

      if (
          params.getUuid() != null
            && !exist( params.getUuid() )          // если такой задачи нет в базе
            && !commandClass.endsWith("DoNothing") // и если не заглушка
        ){

        Gson gson = new Gson();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy в HH:mm");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());

        QueueEntity task = new QueueEntity();
        task.setUuid( params.getUuid() );
        task.setCommand( command.getClass().getCanonicalName() );
        task.setParams(  gson.toJson(params) );
        task.setLocal( false );
        task.setRemote( false );
        task.setWithError( false );
        task.setRunning( false );
        task.setCreatedAt( date );

        dataStore
          .insert(task)
          .toObservable()
          .subscribeOn(Schedulers.computation())
          .subscribeOn(Schedulers.computation())
          .subscribe(data -> {
            Timber.tag(TAG).v("inserted %s [ %s ]", data.getCommand(), data.getId() );
          });
      } else {
        Timber.tag(TAG).v("UUID exist!");
      }
    }
  }

  private void setAsCanceled(String decision_id) {

    Timber.tag(TAG).i( "decision_id %s", decision_id);

    int count = dataStore
      .update(QueueEntity.class)
      .set(QueueEntity.RUNNING, false)
      .set(QueueEntity.LOCAL, true)
      .set(QueueEntity.REMOTE, true)
      .set(QueueEntity.CANCELED, true)
      .where(QueueEntity.COMMAND.eq("sapotero.rxtest.managers.menu.commands.decision.SaveDecision"))
      .and( QueueEntity.PARAMS.like("%\"decisionId\":\""+decision_id+"\"%") )
      .and(QueueEntity.WITH_ERROR.ne(true))
      .get().value();
    Timber.tag(TAG).i( "setAsCanceled %s", count );
  }

  private Boolean exist(String uuid) {
    return dataStore
          .count(QueueEntity.class)
          .where(QueueEntity.UUID.eq( uuid ) )
          .get().value() > 0;
  }

  public List<QueueEntity> getUncompleteSignTasks(int limit) {
    return dataStore
      .select(QueueEntity.class)
      .where(QueueEntity.LOCAL.eq(false))
      .and(QueueEntity.RUNNING.eq(false))
      .and(QueueEntity.WITH_ERROR.eq(false))
      .and(QueueEntity.COMMAND.eq("sapotero.rxtest.managers.menu.commands.file.SignFile"))
      .limit(limit)
      .get()
      .toList();
  }

  public List<QueueEntity> getUncompleteLocalTasks(int limit) {
    return dataStore
      .select(QueueEntity.class)
      .where(QueueEntity.LOCAL.eq(false))
      .and(QueueEntity.RUNNING.eq(false))
      .and(QueueEntity.WITH_ERROR.eq(false))
      .limit(limit)
      .get()
      .toList();
  }

  public List<QueueEntity> getUncompleteRemoteTasks(int limit) {
    return dataStore
      .select(QueueEntity.class)
      .where(QueueEntity.REMOTE.eq(false))
      .and( QueueEntity.LOCAL.eq(true) )
      .and(QueueEntity.RUNNING.eq(false))
      .and(QueueEntity.WITH_ERROR.eq(false))
      .limit(limit)
      .get()
      .toList();
  }

  public void setExecutedLocal(Command command) {
    if (command != null && command.getParams() != null) {
      CommandParams params = command.getParams();

      if ( params.getUuid() != null && exist( params.getUuid() ) ){
        int count = dataStore
          .update(QueueEntity.class)
          .set( QueueEntity.LOCAL, true )
          .set( QueueEntity.RUNNING, false )
          .where( QueueEntity.UUID.eq( params.getUuid() ) )
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

      Timber.tag(TAG).i( "begin [%s] - updated remote", new Gson().toJson(params) );

      if ( params.getUuid() != null && exist( params.getUuid() ) ){
        int count = dataStore
          .update(QueueEntity.class)
          .set( QueueEntity.LOCAL, true )
          .set( QueueEntity.REMOTE, true )
          .set( QueueEntity.RUNNING, false )
          .where( QueueEntity.UUID.eq( params.getUuid() ) )
          .get()
          .value();

        if ( count > 0 ){
          Timber.tag(TAG).i( "[%s] - updated remote", params.getUuid() );
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



  public void setAsRunning(String uuid) {
    updateRunningStatus(uuid, true);
  }

  private void updateRunningStatus(String uuid, Boolean running){
    Timber.tag(TAG).i( "[%s] - setAsRunning", uuid );

    if ( uuid != null ) {
      int count = dataStore
        .update(QueueEntity.class)
        .set(   QueueEntity.RUNNING, running )
        .where( QueueEntity.UUID.eq( uuid ) )
        .get()
        .value();

      if ( count > 0 ){
        Timber.tag(TAG).i( "[%s] - setAsRunning", uuid );
      }
    }
  }

  public void dropRunningJobs() {
    dataStore
      .update(QueueEntity.class)
      .set(QueueEntity.RUNNING, false)
      .where(QueueEntity.RUNNING.eq(true))
      .get()
      .value();
  }

  /* JobCountInterface */
  @Override
  public int getRunningJobsCount(){
    return dataStore.count(QueueEntity.class).where(QueueEntity.RUNNING.eq(true)).get().value();
  }

  public void setExecutedWithError(Command command, List<String> errors) {
    if (command != null && command.getParams() != null) {
      CommandParams params = command.getParams();

      if ( params.getUuid() != null && exist( params.getUuid() ) ){
        int count = dataStore
          .update(QueueEntity.class)
          .set( QueueEntity.REMOTE, true )
          .set( QueueEntity.LOCAL,  true )
          .set( QueueEntity.RUNNING, false )
          .set( QueueEntity.WITH_ERROR, true )
          .set( QueueEntity.ERROR, new Gson().toJson(errors) )
          .where( QueueEntity.UUID.eq( params.getUuid() ) )
          .get()
          .value();

        if ( count > 0 ){
          Timber.tag(TAG).i( "[%s] - updated error", params.getUuid() );
        }
      }
    }
  }
}

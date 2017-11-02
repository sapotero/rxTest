package sapotero.rxtest.utils.queue.db;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.RxScalar;
import io.requery.rx.SingleEntityStore;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.images.RSignImageEntity;
import sapotero.rxtest.db.requery.models.queue.QueueEntity;
import sapotero.rxtest.db.requery.models.utils.RApprovalNextPersonEntity;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.utils.queue.interfaces.JobCountInterface;
import sapotero.rxtest.utils.queue.interfaces.QueueRepository;
import timber.log.Timber;

public class QueueDBManager implements JobCountInterface, QueueRepository {
  @Inject SingleEntityStore<Persistable> dataStore;

  private String TAG = this.getClass().getSimpleName();

  public QueueDBManager() {
    EsdApplication.getDataComponent().inject(this);
    dropRunningTasks();
  }


  private void dropRunningTasks() {
    RxScalar<Integer> list = dataStore
      .update(QueueEntity.class)
      .set(QueueEntity.RUNNING, false)
      .where(QueueEntity.RUNNING.eq(true))
      .get();

    Timber.tag(TAG).e("dropRunningTasks %s", list.toString() );


    dataStore
      .update(RSignImageEntity.class)
      .set(RSignImageEntity.SIGNING, false)
      .where(RSignImageEntity.SIGNED.eq(false))
      .and(RSignImageEntity.ERROR.eq(false))
      .get();


    dataStore
      .update(RSignImageEntity.class)
      .set( RSignImageEntity.SIGN_TASK_STARTED, false )
      .where( RSignImageEntity.SIGN_TASK_STARTED.eq( true ) )
      .get()
      .value();

    dataStore
      .update( RApprovalNextPersonEntity.class )
      .set( RApprovalNextPersonEntity.TASK_STARTED, false )
      .where( RApprovalNextPersonEntity.TASK_STARTED.eq( true ) )
      .get()
      .value();

  }

  private void setDecisionCommandAsCanceled(String decision_id) {

    Timber.tag(TAG).i( "decision_id %s", decision_id);

    List<String> commandTypesToCancel = new ArrayList<>();
    commandTypesToCancel.add("sapotero.rxtest.managers.menu.commands.decision.SaveDecision");
    commandTypesToCancel.add("sapotero.rxtest.managers.menu.commands.decision.AddDecision");

    int count = dataStore
      .update(QueueEntity.class)
      .set(QueueEntity.RUNNING, false)
      .set(QueueEntity.LOCAL, true)
      .set(QueueEntity.REMOTE, true)
      .set(QueueEntity.CANCELED, true)
      .where(QueueEntity.COMMAND.in( commandTypesToCancel ))
      .and( QueueEntity.PARAMS.like("%\"decisionId\":\""+decision_id+"\"%") )
      .and(QueueEntity.WITH_ERROR.ne(true))
      .and(QueueEntity.REMOTE.ne(true))
      .get().value();
    Timber.tag(TAG).i( "setDecisionCommandAsCanceled %s", count );
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

  public void removeAll() {
    int count = dataStore
      .delete(QueueEntity.class)
      .where(QueueEntity.UUID.ne(""))
      .get().value();
    Timber.tag(TAG).i("DELETED: %s", count);
  }

  public void dropRunningJobs() {
    dataStore
      .update(QueueEntity.class)
      .set(QueueEntity.RUNNING, false)
      .where(QueueEntity.RUNNING.eq(true))
      .get()
      .value();
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

  public boolean isAllTasksComplete() {
    QueueEntity uncompleteLocalTask = dataStore
      .select(QueueEntity.class)
      .where(QueueEntity.LOCAL.eq(false))
      .and(QueueEntity.WITH_ERROR.eq(false))
      .get().firstOrNull();

    QueueEntity uncompleteRemoteTask = dataStore
      .select(QueueEntity.class)
      .where(QueueEntity.REMOTE.eq(false))
      .and( QueueEntity.LOCAL.eq(true) )
      .and(QueueEntity.WITH_ERROR.eq(false))
      .get().firstOrNull();

    return uncompleteLocalTask == null && uncompleteRemoteTask == null;
  }

  public void setUpdateDocumentCommandExecuted(String documentUid, boolean canceled) {
    if ( documentUid != null ) {
      int count = dataStore
        .update(QueueEntity.class)
        .set( QueueEntity.RUNNING, false )
        .set( QueueEntity.LOCAL, true )
        .set( QueueEntity.REMOTE, true )
        .set( QueueEntity.CANCELED, canceled )
        .where( QueueEntity.COMMAND.eq( "sapotero.rxtest.managers.menu.commands.update.UpdateDocumentCommand" ) )
        .and( QueueEntity.PARAMS.like( "%\"document\":\""+documentUid+"\"%" ) )
        .and( QueueEntity.WITH_ERROR.ne( true ) )
        .and( QueueEntity.REMOTE.ne( true ) )
        .get()
        .value();

      Timber.tag(TAG).d("Set %s UpdateDocumentCommands as executed, canceled ? %s, for doc %s", count, canceled, documentUid);
    }
  }

  /* QueueRepository */
  @Override
  public void add(Command command){
    if (command != null && command.getParams() != null) {
      Timber.tag(TAG).i( "\n----------- ADD ------------\n%s\n Params: %s", command, command.getParams() );

      CommandParams params = command.getParams();
      String commandClass = command.getClass().getCanonicalName();

      // Если поступила новая операция SaveDecision или SaveAndApproveDecision, то отменить все невыполненные
      // операции SaveDecision и AddDecision для данной резолюции
      if ( params.getUuid() != null && ( commandClass.endsWith("SaveDecision") || commandClass.endsWith("SaveAndApproveDecision") ) ) {
        Decision decision = params.getDecisionModel();
        setDecisionCommandAsCanceled( decision.getId() );
      }

      // Если поступила новая операция UpdateDocumentCommand, то отменить все невыполненные
      // операции UpdateDocumentCommand для данного документа (чтобы не порождать лишних запросов на загрузку документа)
      if ( params.getUuid() != null && commandClass.endsWith("UpdateDocumentCommand") ) {
        setUpdateDocumentCommandExecuted( params.getDocument(), true );
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
          }, Timber::e);
      } else {
        Timber.tag(TAG).v("UUID exist!");
      }
    }
  }

  @Override
  public void remove(AbstractCommand command) {

  }

  @Override
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

  @Override
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

  @Override
  public void setAsRunning(Command command, Boolean value) {
    updateRunningStatus(command.getParams().getUuid(), value);
  }

  @Override
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

  /* JobCountInterface */
  @Override
  public int getRunningJobsCount(){
    return dataStore.count(QueueEntity.class).where(QueueEntity.RUNNING.eq(true)).get().value();
  }

  public List<QueueEntity> getUncompleteTasks() {
    return dataStore
      .select(QueueEntity.class)
      .where(QueueEntity.REMOTE.eq(false))
      .or( QueueEntity.LOCAL.eq(true) )
      .and(QueueEntity.RUNNING.eq(false))
      .and(QueueEntity.WITH_ERROR.eq(false))
      .and(QueueEntity.CANCELED.eq(false))
      .get()
      .toList();
  }
}

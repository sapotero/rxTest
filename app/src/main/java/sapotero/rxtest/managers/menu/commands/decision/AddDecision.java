package sapotero.rxtest.managers.menu.commands.decision;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.managers.menu.commands.DecisionCommand;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.v2.DecisionError;
import timber.log.Timber;

public class AddDecision extends DecisionCommand {

  private String TAG = this.getClass().getSimpleName();

  public AddDecision(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    CommandFactory.Operation operation = CommandFactory.Operation.CREATE_TEMPORARY_DECISION;
    CommandParams _params = new CommandParams();
    _params.setUser( getParams().getUser() );
    _params.setToken( getParams().getToken() );
    _params.setDecisionId( getParams().getDecisionModel().getId() );
    _params.setDecisionModel( getParams().getDecisionModel() );
    _params.setDocument( getParams().getDocument() );
    _params.setAssignment( getParams().isAssignment() );
    Command command = operation.getCommand(null, _params);
    command.execute();

    setDocOperationStartedInMemory();

    Timber.tag(TAG).w("ASSIGNMENT: %s", getParams().isAssignment() );

    queueManager.add(this);
  }

  @Override
  public String getType() {
    return "add_decision";
  }

  @Override
  public void executeLocal() {
    // resolved https://tasks.n-core.ru/browse/MVDESD-13366
    // ставим плашку всегда
    dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.CHANGED, true)
      .set(RDocumentEntity.MD5, "")
      .where(RDocumentEntity.UID.eq( getParams().getDocument() ))
      .get()
      .value();

    if (callback != null ){
      callback.onCommandExecuteSuccess( getType() );
    }

    queueManager.setExecutedLocal(this);
  }

  @Override
  public void executeRemote() {
    Timber.tag(TAG).i( "type: %s", this.getClass().getName() );

    Decision decision = getParams().getDecisionModel();
    decision.setLetterhead(null);
    decision.setShowPosition( false );

    if ( getParams().isAssignment() ) {
      decision.setAssignment(true);
    }

    Observable<DecisionError> info = getDecisionCreateOperationObservable(decision, TAG);

    info.subscribeOn( Schedulers.computation() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          onSuccess( this, data, true, true, TAG );
          finishOperationOnSuccess();
        },
        error -> onError( this, error.getLocalizedMessage(), false, TAG )
      );
  }
}

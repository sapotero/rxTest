package sapotero.rxtest.managers.menu;

import android.content.Context;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.invokers.OperationExecutor;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.managers.menu.utils.OperationHistory;
import timber.log.Timber;

public class OperationManager implements CommandFactory.Callback {

  private RxSharedPreferences settings;

  private final String TAG = this.getClass().getSimpleName();

  private  CommandFactory commandBuilder;
  private final OperationHistory histrory;
  private final OperationExecutor operationExecutor;

  private Context context;
  private String uid;

  Callback callback;

  public interface Callback {
    void onExecuteSuccess(String command);
    void onExecuteError();
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public OperationManager(Context context, RxSharedPreferences rxSharedPreferences) {
    settings = rxSharedPreferences;

    histrory          = new OperationHistory(context);
    operationExecutor = new OperationExecutor();

    commandBuilder = new CommandFactory(context);
    commandBuilder.registerCallBack(this);

    this.context = context;
  }

  public void execute(CommandFactory.Operation operation, CommandParams params) {

    Timber.tag(TAG).i("execute start");

    Command command = commandBuilder
      .withDocument( new DocumentReceiver( settings.getString("activity_main_menu.uid").get() ) )
      .withParams( params )
      .build( operation );

    Timber.tag(TAG).i("command get");

    if (command != null) {
      operationExecutor
        .setCommand( command )
        .execute();
    }

    Timber.tag(TAG).i("execute end");
  }


  @Override
  public void onCommandSuccess(String command) {
    Timber.tag(TAG).w("onCommandSuccess");
    if (callback != null) {
      callback.onExecuteSuccess(command);
    }
  }

  @Override
  public void onCommandError() {
    Timber.tag(TAG).w("onCommandError");
  }

}

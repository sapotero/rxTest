package sapotero.rxtest.views.managers.menu;

import android.content.Context;

import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import javax.inject.Inject;

import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.views.managers.menu.factories.CommandFactory;
import sapotero.rxtest.views.managers.menu.interfaces.Command;
import sapotero.rxtest.views.managers.menu.invokers.RemoteExecutor;
import sapotero.rxtest.views.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;
import sapotero.rxtest.views.managers.menu.utils.OperationHistory;
import timber.log.Timber;

public class OperationManager implements CommandFactory.Callback {

  @Inject RxSharedPreferences settings;

  private final String TAG = this.getClass().getSimpleName();

  private static OperationManager instance;
  private final CommandFactory commandBuilder;
  private final OperationHistory histrory;
  private final RemoteExecutor remoteExecutor;

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

  public OperationManager(Context context) {
    EsdApplication.getComponent(context).inject(this);

    histrory       = new OperationHistory(context);
    remoteExecutor = new RemoteExecutor();

    commandBuilder = new CommandFactory(context);
    commandBuilder.registerCallBack(this);

    this.context = context;
  }

  public static OperationManager getInstance() {
    if ( instance == null) {
      instance = new OperationManager( EsdApplication.getContext() );
    }
    return instance;
  }

  public void execute(CommandFactory.Operation operation, CommandParams params) {
    Command command = commandBuilder
      .withDocument( new DocumentReceiver( settings.getString("main_menu.uid").get() ) )
      .withParams( params )
      .build( operation );

    Timber.tag(TAG).i("COMMAND: %s [%s] | %s", operation, command, new Gson().toJson(params) );

    if (command != null) {
      remoteExecutor
        .setCommand( command )
        .execute();
    }
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

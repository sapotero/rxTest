package sapotero.rxtest.views.managers.menu;

import android.content.Context;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import javax.inject.Inject;

import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.views.managers.menu.factories.CommandFactory;
import sapotero.rxtest.views.managers.menu.interfaces.Command;
import sapotero.rxtest.views.managers.menu.invokers.RemoteExecutor;
import sapotero.rxtest.views.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class OperationManager implements CommandFactory.Callback {

  @Inject RxSharedPreferences settings;

  private final String TAG = this.getClass().getSimpleName();

  private final CommandFactory commandBuilder;
  private final RemoteExecutor remoteExecutor;

  private Context context;
  private String uid;

  Callback callback;

  public interface Callback {
    void onExecuteSuccess();
    void onExecuteError();
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public OperationManager(Context context) {
    EsdApplication.getComponent(context).inject(this);

    remoteExecutor = new RemoteExecutor();

    commandBuilder = new CommandFactory(context);
    commandBuilder.registerCallBack(this);

    this.context = context;
  }


  public void execute(String operation, CommandParams params) {
    Command command = commandBuilder
      .withDocument( new DocumentReceiver( settings.getString("main_menu.uid").get() ) )
      .withParams( params )
      .build( operation );

    remoteExecutor
      .setCommand( command )
      .execute();
  }


  @Override
  public void onCommandSuccess() {
    Timber.tag(TAG).w("onCommandSuccess");
  }

  @Override
  public void onCommandError() {
    Timber.tag(TAG).w("onCommandError");
  }

}

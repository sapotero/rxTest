package sapotero.rxtest.views.managers.menu.utils;

import android.content.Context;
import android.widget.Toast;

import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.views.managers.menu.interfaces.Command;
import timber.log.Timber;

public class OperationHistory {

  @Inject RxSharedPreferences settings;

  private final Context context;
  private final ArrayList<Command> commands = new ArrayList<>();

  private Boolean isConnectedToInternet;

  public OperationHistory(Context context) {

    this.context = context;
    EsdApplication.getComponent(context).inject(this);

    isConnectedToInternet();
    historyCheck();

  }

  private void historyCheck() {
    Observable
      .interval( 10, TimeUnit.SECONDS )
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(interval -> {

        if ( commands.size() > 0 && !isConnectedToInternet ){
          Toast.makeText( context, String.format( "History size: %s", commands.size() ), Toast.LENGTH_SHORT).show();

          for (Command command: commands) {
            if (command != null) {
              Timber.tag("COMMAND").e( command.toString() );
            }
          }

        }

      });
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

  public void add(Command command) {
    if (isConnectedToInternet != null && !isConnectedToInternet) {
      commands.add(command);
    }
  }

  public void remove(Command command){
    if (command != null && commands.contains( command )){
      commands.remove(command);
    }
  }


}

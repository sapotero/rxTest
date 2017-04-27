package sapotero.rxtest.managers.menu.commands;

import android.content.Context;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.io.Serializable;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.queue.QueueManager;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.interfaces.Operation;
import sapotero.rxtest.managers.menu.utils.CommandParams;


public abstract class AbstractCommand implements Serializable, Command, Operation {

  @Inject public OkHttpClient okHttpClient;
  @Inject public RxSharedPreferences settings;
  @Inject public SingleEntityStore<Persistable> dataStore;
  @Inject public QueueManager queueManager;

  public CommandParams params;

  public AbstractCommand(Context context) {
    EsdApplication.getComponent().inject(this);
  }

  public abstract void withParams(CommandParams params);

  public Callback callback;
  public interface Callback {
    void onCommandExecuteSuccess(String command);
    void onCommandExecuteError(String type);
  }

}

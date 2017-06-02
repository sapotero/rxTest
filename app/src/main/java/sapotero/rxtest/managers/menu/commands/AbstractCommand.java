package sapotero.rxtest.managers.menu.commands;

import java.io.Serializable;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.queue.QueueManager;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.interfaces.Operation;
import sapotero.rxtest.managers.menu.utils.CommandParams;


public abstract class AbstractCommand implements Serializable, Command, Operation {

  @Inject public OkHttpClient okHttpClient;
  @Inject public Settings settings;
  @Inject public Mappers mappers;
  @Inject public SingleEntityStore<Persistable> dataStore;
  @Inject public QueueManager queueManager;
  @Inject public MemoryStore store;

  public CommandParams params;

  public AbstractCommand() {
    EsdApplication.getManagerComponent().inject(this);
  }

  public abstract void withParams(CommandParams params);

  public Callback callback;
  public interface Callback {
    void onCommandExecuteSuccess(String command);
    void onCommandExecuteError(String type);
  }

}

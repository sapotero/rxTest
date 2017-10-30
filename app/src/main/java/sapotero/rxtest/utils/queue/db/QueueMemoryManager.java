package sapotero.rxtest.utils.queue.db;


import com.google.gson.Gson;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.utils.queue.interfaces.QueueRepository;
import timber.log.Timber;

public class QueueMemoryManager implements QueueRepository{
  private final HashMap<String, List<Command>> commands;
  private String TAG = this.getClass().getSimpleName();

  public QueueMemoryManager() {
    this.commands = new HashMap<>();
  }

  @Override
  public void add(Command command) {
    commands.put( command.getParams().getUuid(), Collections.singletonList(command));
  }

  @Override
  public void remove(AbstractCommand command) {
    if ( commands.containsKey(command.getParams().getUuid()) ){
      commands.remove(command.getParams().getUuid());
    }
  }

  @Override
  public void setExecutedLocal(Command command) {
    executeMock(command);
  }

  @Override
  public void setExecutedRemote(Command command) {
    executeMock(command);
  }

  @Override
  public void setExecutedWithError(Command command, List<String> errors) {
    executeMock(command);
  }

  @Override
  public void setAsRunning(Command command) {
    executeMock(command);
  }

  private void executeMock(Command command) {
    logInfo();
    if ( commands.containsKey(command.getParams().getUuid()) ){
      List<Command> commandList = commands.get(command.getParams().getUuid());
      if ( commandList.size() > 0 ){
        Timber.tag(TAG).d("setExecutedLocal: %s", commandList.size());
      } else {
        remove((AbstractCommand) command);
      }
    }
  }

  private void logInfo() {
    Timber.tag(TAG).d("\n --- QueueMemoryStore ---\n\n%s\n\n", new Gson().toJson(commands));
  }
}

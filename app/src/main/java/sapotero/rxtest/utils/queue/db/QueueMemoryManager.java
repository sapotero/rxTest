package sapotero.rxtest.utils.queue.db;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.utils.queue.interfaces.QueueRepository;
import sapotero.rxtest.utils.queue.models.CommandInfo;
import sapotero.rxtest.utils.transducers.queue.QueueTransduce;
import timber.log.Timber;

public class QueueMemoryManager implements QueueRepository{
  private final HashMap<String, List<CommandInfo>> commands;
  private final String TAG = this.getClass().getSimpleName();

  public QueueMemoryManager() {
    this.commands = new HashMap<>();
  }

  @Override
  public void add(Command command) {
    commands.put( command.getParams().getUuid(), Collections.singletonList( new CommandInfo(command) ));
  }

  @Override
  public void remove(AbstractCommand command) {
    if ( commands.containsKey(command.getParams().getUuid()) ){
      commands.remove(command.getParams().getUuid());
    }
  }

  @Override
  public void setExecutedLocal(Command command) {
    setExecuted(command, false);
  }

  @Override
  public void setExecutedRemote(Command command) {
    setExecuted(command, true);
  }

  @Override
  public void setExecutedWithError(Command command, List<String> errors) {
    executeMock(command);
  }

  @Override
  public void setAsRunning(Command command) {
    if ( commands.containsKey(command.getParams().getUuid()) ){
      List<CommandInfo> commandInfoList = commands.get(command.getParams().getUuid());

      if (commandInfoList.size() > 0) {
        commandInfoList.get(0).setState(CommandInfo.STATE.RUNNING);
        executeMock(command);
      }
    }
  }


  private void setExecuted(Command command, Boolean remote) {
    if ( commands.containsKey(command.getParams().getUuid()) ){
      List<CommandInfo> commandInfoList = commands.get(command.getParams().getUuid());
      if (commandInfoList.size() > 0) {

        if (remote){
          commandInfoList.get(0).setExecutedRemote(true);
//          delayedRemove(command);
        } else {
          commandInfoList.get(0).setExecutedLocal(true);
        }
        commandInfoList.get(0).setState(CommandInfo.STATE.READY);
        executeMock(command);

      } else {
        remove((AbstractCommand) command);
      }
    }
  }

  private void executeMock(Command command) {
    Timber.tag(TAG).d("\n --- QueueMemoryStore ---\n\n%s\n\n", commands.values() );

    List<CommandInfo> availableCommands = QueueTransduce.sortByState(commands.values(), CommandInfo.STATE.READY);
    Timber.tag(TAG).d("\n --- sort by state --- \n\n%s\n\n", availableCommands );

    if (availableCommands.size() > 0){
      List<CommandInfo> notExecutedLocal  = QueueTransduce.sort(
        Collections.singletonList(availableCommands),
        true,
        false);

      Timber.tag(TAG).d("\n --- notExecutedRemote --- \n\n%s\n\n", notExecutedLocal );
    }
  }

}

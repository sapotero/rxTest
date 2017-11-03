package sapotero.rxtest.utils.queue.db;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import sapotero.rxtest.db.requery.models.queue.QueueEntity;
import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.commands.decision.AddDecision;
import sapotero.rxtest.managers.menu.commands.decision.SaveAndApproveDecision;
import sapotero.rxtest.managers.menu.commands.decision.SaveDecision;
import sapotero.rxtest.managers.menu.commands.shared.DoNothing;
import sapotero.rxtest.managers.menu.commands.update.UpdateDocumentCommand;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.interfaces.Command;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.utils.queue.interfaces.QueueRepository;
import sapotero.rxtest.utils.queue.models.CommandInfo;
import sapotero.rxtest.utils.transducers.queue.QueueTransduce;
import timber.log.Timber;

import static com.googlecode.totallylazy.Sequences.sequence;

public class QueueMemoryManager implements QueueRepository{

  private final HashMap<String, ArrayList<CommandInfo>> commands;
  private final String TAG = this.getClass().getSimpleName();

  public QueueMemoryManager() {
    this.commands = new HashMap<>();
  }

  @Override
  public void add(Command command) {

    try {
      if ( !commands.containsKey( command.getParams().getDocument() ) ) {
        commands.put( command.getParams().getDocument(), new ArrayList<>());
      }

      //если не заглушка
      if (!(command instanceof DoNothing) ) {

        // Если поступила новая операция SaveDecision или SaveAndApproveDecision, то отменить все невыполненные
        // операции SaveDecision и AddDecision для данной резолюции
        if ( command instanceof SaveDecision || command instanceof SaveAndApproveDecision ) {
          setDecisionCommandAsCanceled( command.getParams().getDecisionId() );
        }

        // Если поступила новая операция UpdateDocumentCommand, то отменить все невыполненные
        // операции UpdateDocumentCommand для данного документа (чтобы не порождать лишних запросов на загрузку документа)
        if ( command instanceof UpdateDocumentCommand ) {
          setUpdateDocumentCommandExecuted( command.getParams().getDocument() );
        }

        ArrayList<CommandInfo> list = commands.get(command.getParams().getDocument());
        list.add( new CommandInfo(command) );
        commands.put( command.getParams().getDocument(), list);
      }
    } catch (Exception e) {
      Timber.e( e );
    }

    showQueueInfo();
  }

  @Override
  public void remove(AbstractCommand command) {
    Timber.tag(TAG).d(" --- remove --- ");
    if ( commands.containsKey(command.getParams().getDocument()) ){
      ArrayList<CommandInfo> list = commands.get(command.getParams().getDocument());

      Iterator<CommandInfo> iterator = list.iterator();
      while ( iterator.hasNext() ) {
        CommandInfo actual = iterator.next();
        if( actual.getCommand().getParams().getDocument().equals( command.getParams().getDocument() ) ) {
          iterator.remove();
        }
      }

      if (list.size() == 0) {
        commands.remove(command.getParams().getDocument());
      }
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
    Timber.tag(TAG).d("\n --- executed-with-error [%s]--- \n\n%s\n\n", command, errors);
    if ( commands.containsKey(command.getParams().getDocument()) ){
      List<CommandInfo> commandInfoList = commands.get(command.getParams().getDocument());

      if (commandInfoList.size() > 0) {
        commandInfoList.get(0).setState(CommandInfo.STATE.ERROR);
        remove((AbstractCommand) command);
      }
    }
  }

  @Override
  public void setAsRunning(Command command, Boolean value) {
    if ( commands.containsKey(command.getParams().getDocument()) ){
      List<CommandInfo> commandInfoList = commands.get(command.getParams().getDocument());

      if (commandInfoList.size() > 0) {
        commandInfoList.get(0).setState( value ? CommandInfo.STATE.RUNNING : CommandInfo.STATE.READY);
      }
    }
  }


  private void setExecuted(Command command, Boolean remote) {
    if ( commands.containsKey(command.getParams().getDocument()) ){
      List<CommandInfo> commandInfoList = commands.get(command.getParams().getDocument());
      if (commandInfoList.size() > 0) {

        if (remote){
          commandInfoList.get(0).setExecutedRemote(true);
          commandInfoList.get(0).setState(CommandInfo.STATE.COMPLETE);
          remove((AbstractCommand) command);
        } else {
          commandInfoList.get(0).setExecutedLocal(true);
          commandInfoList.get(0).setState(CommandInfo.STATE.READY);
        }

      } else {
        remove((AbstractCommand) command);
      }
    }
  }

  public List<Command> getUncompleteCommands(Boolean remote){

    showQueueInfo();

    List<Command> result = new ArrayList<>();

    List<CommandInfo> availableCommands = QueueTransduce.sortByState(
      sequence(commands.values()).map(info -> info.get(0) ).toList(),
      CommandInfo.STATE.READY
    );

    if (availableCommands.size() > 0){
      List<CommandInfo> uncompleteCommands  = QueueTransduce.sort(
        availableCommands,
        remote,
        false);

      Timber.tag(TAG).d("\n --- not-executed-%s --- \n\n%s\n\n", remote, uncompleteCommands );

      if (uncompleteCommands.size() > 0){
        result = sequence(uncompleteCommands).map(CommandInfo::getCommand).toList();
      }
    }

    return result;
  }

  public void init(List<QueueEntity> tasks) {
    Timber.e("SIZE: %s", tasks.size());


    for ( QueueEntity entity: tasks ) {
//
      Command cmd = create(entity);

      if (cmd != null) {
        CommandInfo command = new CommandInfo( cmd );
//
        if ( entity.isLocal() ){
          command.setExecutedLocal(true);
        }

        if ( entity.isRemote() ){
          command.setExecutedRemote(true);
        }

        Timber.e("new task: %s", command);

        try {
          if ( !commands.containsKey( cmd.getParams().getDocument() ) ) {
            commands.put( cmd.getParams().getDocument(), new ArrayList<>());
          }

          ArrayList<CommandInfo> list = commands.get(cmd.getParams().getDocument());
          list.add( command );
          commands.put( cmd.getParams().getDocument(), list);
        } catch (Exception e) {
          Timber.e( e );
        }
      }
    }
  }

  private Command create(QueueEntity task){
    Command command = null;

    try {
      CommandParams params = new Gson().fromJson( task.getParams(), CommandParams.class );

      CommandFactory.Operation operation = CommandFactory.Operation.getOperation(task.getCommand());
      Timber.e("OPERATION: %s", operation);
      Timber.e("params: %s", params);

      command = operation.getCommand(params);

      if (command != null) {
        Timber.tag(TAG).v("create command %s", command.getParams().toString() );
      }

    } catch (JsonSyntaxException error) {
      Timber.tag(TAG).v("create command error %s", error );
    }

    return command;
  }

  private void setDecisionCommandAsCanceled(String decision_id) {
    for ( List<CommandInfo> commandInfoList : commands.values() ) {
      for ( CommandInfo commandInfo : commandInfoList ) {
        Command command = commandInfo.getCommand();

        if ( ( command instanceof AddDecision || command instanceof SaveDecision )
          && Objects.equals( command.getParams().getDecisionId(), decision_id )
          && !commandInfo.isExecutedRemote()
          && commandInfo.getState() != CommandInfo.STATE.ERROR ) {

          setComplete( commandInfo );
        }
      }
    }
  }

  private void setComplete(CommandInfo commandInfo) {
    commandInfo.setExecutedLocal( true );
    commandInfo.setExecutedRemote( true );
    commandInfo.setState( CommandInfo.STATE.COMPLETE );
  }

  public void setUpdateDocumentCommandExecuted(String documentUid) {
    for ( List<CommandInfo> commandInfoList : commands.values() ) {
      for ( CommandInfo commandInfo : commandInfoList ) {
        Command command = commandInfo.getCommand();

        if ( command instanceof UpdateDocumentCommand
          && Objects.equals( command.getParams().getDocument(), documentUid )
          && !commandInfo.isExecutedRemote()
          && commandInfo.getState() != CommandInfo.STATE.ERROR ) {

          setComplete( commandInfo );
        }
      }
    }
  }

  private void showQueueInfo() {
    Timber.tag(TAG).d("\n\n--- showQueueInfo ---");
    for ( String uid: commands.keySet() ) {
      Timber.tag(TAG).d(" * %s [%s] \n%s", uid, commands.get(uid).size(), showCommandDetails(commands.get(uid)) );
    }
  }

  private String showCommandDetails(ArrayList<CommandInfo> commandInfos) {
    StringBuilder result = new StringBuilder("\n");
    for ( CommandInfo uid: commandInfos ) {
      result.append("   - ");
      result.append(uid.toString());
    }

    return result.toString();
  }

}

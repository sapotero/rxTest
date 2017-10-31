package sapotero.rxtest.utils.queue.models;

import sapotero.rxtest.managers.menu.interfaces.Command;

public class CommandInfo {
  protected enum STATE {
    READY,
    RUNNING
  }

  private STATE   state;
  private Boolean executedLocal;
  private Boolean executedRemote;
  private Command command;

  public CommandInfo( Command command) {
    this.state = STATE.READY;
    this.executedLocal  = false;
    this.executedRemote = false;
    this.command = command;
  }

  public Command getCommand() {
    return command;
  }

  public STATE getState() {
    return state;
  }

  public void setState(STATE state) {
    this.state = state;
  }

  public Boolean isExecutedLocal() {
    return executedLocal;
  }

  public void setExecutedLocal(Boolean executedLocal) {
    this.executedLocal = executedLocal;
  }

  public Boolean isExecutedRemote() {
    return executedRemote;
  }

  public void setExecutedRemote(Boolean executedRemote) {
    this.executedRemote = executedRemote;
  }

  @Override
  public String toString() {
    return "CommandInfo {" +
      " state = " + state +
      ", executedLocal = " + executedLocal +
      ", executedRemote = " + executedRemote +
      ", commandUid = " + command.getParams().getUuid() +
       "}\n";
  }
}

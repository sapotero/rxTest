package sapotero.rxtest.utils.queue.models;

import java.util.concurrent.TimeUnit;

import sapotero.rxtest.managers.menu.interfaces.Command;

public class CommandInfo {
  private static final Long UPDATE_TIME = 5L;

  public enum STATE {
    READY,
    RUNNING,
    COMPLETE,
    ERROR
  }

  private STATE   state;
  private Boolean executedLocal;
  private Boolean executedRemote;
  private Long createdAt;
  private Command command;

  public CommandInfo( Command command) {
    this.state = STATE.READY;
    this.executedLocal  = false;
    this.executedRemote = false;
    this.command = command;
    updateCreatedAtTime();
  }

  public Long getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Long createdAt) {
    this.createdAt = createdAt;
  }

  private void updateCreatedAtTime(){
    this.createdAt = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
  }
  public Boolean canRecreate(){
    return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) > this.createdAt + UPDATE_TIME;
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
       "}\n";
  }
}

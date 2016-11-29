package sapotero.rxtest.views.managers.menu.commands;

import sapotero.rxtest.views.managers.menu.interfaces.Command;


public abstract class AbstractCommand implements Command {

  Callback callback;

  public interface Callback {
    void onCommandExecuteSuccess();
    void onCommandExecuteError();
  }

  abstract String operationType();

}

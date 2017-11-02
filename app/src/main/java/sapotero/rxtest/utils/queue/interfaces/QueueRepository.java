package sapotero.rxtest.utils.queue.interfaces;

import java.util.List;

import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.interfaces.Command;

public interface QueueRepository {
  void add(Command command);
  void remove(AbstractCommand command);
  void setExecutedLocal(Command command);
  void setExecutedRemote(Command command);
  void setExecutedWithError(Command command, List<String> errors);
  void setAsRunning(Command command, Boolean value);
}

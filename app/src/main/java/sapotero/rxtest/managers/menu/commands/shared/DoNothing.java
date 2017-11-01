package sapotero.rxtest.managers.menu.commands.shared;

import java.util.List;

import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.utils.CommandParams;

public class DoNothing extends AbstractCommand {

  public DoNothing(CommandParams params) {
    super(params);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public String getType() {
    return "do_nothing";
  }

  @Override
  public void executeLocal() {
    queueManager.add(this);
    update();
  }

  @Override
  public void executeRemote() {
    update();
  }

  private void update() {
    queueManager.setExecutedLocal(this);
    queueManager.setExecutedRemote(this);
  }

  @Override
  public void finishOnOperationError(List<String> errors) {
  }
}

package sapotero.rxtest.managers.menu.commands.shared;

import android.content.Context;

import sapotero.rxtest.managers.menu.commands.AbstractCommand;
import sapotero.rxtest.managers.menu.receivers.DocumentReceiver;
import sapotero.rxtest.managers.menu.utils.CommandParams;

public class DoNothing extends AbstractCommand {

  private final DocumentReceiver document;
  private final Context context;

  public DoNothing(Context context, DocumentReceiver document){
    super(context);
    this.context = context;
    this.document = document;
  }

  public String getInfo(){
    return null;
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void execute() {
    queueManager.add(this);
  }

  @Override
  public String getType() {
    return "do_nothing";
  }

  @Override
  public void executeLocal() {
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
  public void withParams(CommandParams params) {
    this.params = params;
  }

  @Override
  public CommandParams getParams() {
    return params;
  }
}

package sapotero.rxtest.managers.menu.interfaces;


import sapotero.rxtest.managers.menu.utils.CommandParams;

public interface Command{
  public void executeRemote();
  public void executeLocal();
  public void execute();
  public CommandParams getParams();
}
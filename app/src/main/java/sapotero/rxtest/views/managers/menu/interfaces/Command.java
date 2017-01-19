package sapotero.rxtest.views.managers.menu.interfaces;


import sapotero.rxtest.views.managers.menu.utils.CommandParams;

public interface Command{
  public void execute();
  public CommandParams getParams();
}
package sapotero.rxtest.managers.menu.interfaces;


import sapotero.rxtest.managers.menu.utils.CommandParams;

public interface Command{
  void executeRemote();
  void executeLocal();
  CommandParams getParams();
}
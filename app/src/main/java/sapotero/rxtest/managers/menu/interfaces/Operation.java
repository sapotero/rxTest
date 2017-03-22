package sapotero.rxtest.managers.menu.interfaces;


public interface Operation {
  String getType();

  void executeLocal();
  void executeRemote();
}

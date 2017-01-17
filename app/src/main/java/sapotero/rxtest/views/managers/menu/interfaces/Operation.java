package sapotero.rxtest.views.managers.menu.interfaces;


public interface Operation {
  String getType();

  void executeLocal();
  void executeRemote();
}

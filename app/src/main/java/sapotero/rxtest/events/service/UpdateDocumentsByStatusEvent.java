package sapotero.rxtest.events.service;

import sapotero.rxtest.views.menu.fields.MainMenuButton;
import sapotero.rxtest.views.menu.fields.MainMenuItem;

public class UpdateDocumentsByStatusEvent {
  public final MainMenuItem item;
  public final MainMenuButton button;

  public UpdateDocumentsByStatusEvent(MainMenuItem item, MainMenuButton button) {
    this.item = item;
    this.button = button;
  }
}

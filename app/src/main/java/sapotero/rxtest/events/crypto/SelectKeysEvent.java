package sapotero.rxtest.events.crypto;

import java.util.List;

public class SelectKeysEvent {
  public final List<String> list;

  public SelectKeysEvent(List<String> list) {
    this.list = list;
  }
}

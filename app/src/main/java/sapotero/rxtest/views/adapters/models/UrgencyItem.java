package sapotero.rxtest.views.adapters.models;

import sapotero.rxtest.views.adapters.utils.Listable;

public class UrgencyItem implements Listable {
  private String label;
  private String value;

  public UrgencyItem(String label, String value) {
    this.label = label;
    this.value = value;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}

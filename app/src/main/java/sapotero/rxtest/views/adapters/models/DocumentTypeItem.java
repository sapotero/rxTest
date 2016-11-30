package sapotero.rxtest.views.adapters.models;

public class DocumentTypeItem {

  private int value;
  private String type;
  private String name;
  public DocumentTypeItem(String name, String type, int value) {
    super();
    this.name = name;
    this.type = type;
    this.value = value;
  }

  public String getValue() {
    return String.valueOf(value);
  }

  public void setValue(int value) {
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setType(String type) {
    this.type = type;
  }
}

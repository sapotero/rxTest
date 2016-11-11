package sapotero.rxtest.views.adapters.models;

public class DocumentTypeItem {

  private int value;
  private String count;
  private String name;
  public DocumentTypeItem(String name, String count, int value) {
    super();
    this.name = name;
    this.count = count;
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

  public String getCount() {
    return count;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setCount(String count) {
    this.count = count;
  }
}

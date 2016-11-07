package sapotero.rxtest.views.adapters.models;

public class DocumentTypeItem {

  private String count;
  private String name;

  public DocumentTypeItem(String name, String count) {
    super();
    this.name = name;
    this.count = count;
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

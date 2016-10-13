package sapotero.rxtest.views.adapters.models;

public class FilterItem {

  private String value;
  private String count;
  private String name;

  public FilterItem(String name, String value, String count) {
    super();

    this.count = count;
    this.name = name;
    this.value = value;

  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public String getCount() {
    return count;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setCount(String count) {
    this.count = count;
  }
}

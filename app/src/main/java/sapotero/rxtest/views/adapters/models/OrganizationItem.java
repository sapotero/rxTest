package sapotero.rxtest.views.adapters.models;

import java.util.Objects;

public class OrganizationItem {
  private int id;
  private String name;
  private Integer count;

  private String template = "%-4s %s";

  public OrganizationItem(String name, Integer count) {

    this.name = name;
    this.count = count;
  }

  public String getName() {
    return name;
  }

  public Integer getCount() {
    return count;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public String getTitle() {
    String title = name;
    if (Objects.equals(title, "")){
      title = "Без организации";
    }
    return title;
  }

  public String getTitleForDialog() {
    String title = String.format( template, count, getTitle() );
    int maxLength = 37;
    if (title.length() > maxLength){
      title = title.toString().substring(0, maxLength - 3) + "...";
    }
    return title;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OrganizationItem)) return false;

    OrganizationItem that = (OrganizationItem) o;

    if (!getName().equals(that.getName())) return false;
    return getCount().equals(that.getCount());

  }

  @Override
  public int hashCode() {
    int result = getName().hashCode();
    result = 31 * result + getCount().hashCode();
    return result;
  }
}

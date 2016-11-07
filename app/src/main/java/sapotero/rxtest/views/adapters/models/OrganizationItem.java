package sapotero.rxtest.views.adapters.models;

import java.util.Objects;

public class OrganizationItem {
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
    if (Objects.equals(name, "")){
      setName("Без организации");
    }
    return String.format( template, count, name );
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

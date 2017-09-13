package sapotero.rxtest.db.requery.utils;

public enum V2FilterType{
  SIGNING    ("signing"),
  APPROVAL   ("approval"),
  FOR_REPORT ("sent_to_the_report"),
  PRIMARY    ("primary_consideration");

  private final String name;

  V2FilterType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}

package sapotero.rxtest.db.requery.utils;

import java.util.Objects;

public enum V2FilterType{
  SIGNING    ("signing"),
  APPROVAL   ("approval"),
  FOR_REPORT ("sent_to_the_report"),
  PRIMARY    ("primary_consideration"),
  LINK       ("link"),
  PROJECTS   ("project"),
  PROCESSED  ("processed");

  private final String name;

  V2FilterType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static V2FilterType findFilterType(String typeName) {
    V2FilterType filterType = null;

    for ( V2FilterType item : V2FilterType.values() ) {
      if ( Objects.equals( item.getName(), typeName ) ) {
        filterType = item;
        break;
      }
    }

    return filterType;
  }
}

package sapotero.rxtest.db.requery.models;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;

@Entity
public abstract class RTemplate {

  @Key
  @Generated
  int id;

  String uid;
  String title;
  String type;

}
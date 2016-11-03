package sapotero.rxtest.db.requery.models;


import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;

@Entity
public abstract class RSigner {
  @Key @Generated
  int id;

  String uid;
  String type;
  String name;
  String organisation;
}
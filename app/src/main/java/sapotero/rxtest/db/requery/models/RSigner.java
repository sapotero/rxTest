package sapotero.rxtest.db.requery.models;


import io.requery.Entity;
import io.requery.Generated;
import io.requery.Index;
import io.requery.Key;

@Entity
public abstract class RSigner {
  @Key @Generated
  @Index("sid_user_index")
  int id;

  String uid;
  String type;
  String name;
  String organisation;
}
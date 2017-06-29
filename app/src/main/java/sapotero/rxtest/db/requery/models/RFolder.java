package sapotero.rxtest.db.requery.models;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Index;
import io.requery.Key;

@Entity
public abstract class RFolder {

  @Key
  @Generated
  int id;

  String uid;
  String title;
  String type;

  @Index("folder_user_index")
  String  user;

}

package sapotero.rxtest.db.requery.models;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Index;
import io.requery.Key;

@Entity
public abstract class RUrgency {

  @Key
  @Generated
  int id;

  String uid;
  String code;
  String name;

  @Index("ur_user_index")
  String  user;

}

package sapotero.rxtest.db.requery.models;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Index;
import io.requery.Key;

@Entity
public abstract class RTemplate {

  @Key
  @Generated
  int id;

  String uid;
  String title;
  String type;

  @Index("template_user_index")
  String  user;
}
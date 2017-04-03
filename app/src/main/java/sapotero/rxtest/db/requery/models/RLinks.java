package sapotero.rxtest.db.requery.models;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;

@Entity
public abstract class RLinks {

  @Key
  @Generated
  int id;

  String uid;

  @ManyToOne
  RDocument document;

}

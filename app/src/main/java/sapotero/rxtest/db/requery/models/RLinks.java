package sapotero.rxtest.db.requery.models;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Index;
import io.requery.Key;
import io.requery.ManyToOne;

@Entity
public abstract class RLinks {

  @Key
  @Generated
  int id;

  @Index("links_uid_index")
  String uid;

  @ManyToOne
  RDocument document;

}

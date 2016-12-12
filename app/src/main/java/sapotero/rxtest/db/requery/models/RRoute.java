package sapotero.rxtest.db.requery.models;

import java.util.Set;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.OneToMany;

@Entity
public abstract class RRoute {

  @Key
  @Generated
  int id;

  String text;

  @OneToMany(mappedBy = "route")
  Set<RStep> steps;

}

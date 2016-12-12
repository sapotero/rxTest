package sapotero.rxtest.db.requery.models;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;

@Entity
public abstract class RStep {

  @Key
  @Generated
  int id;

  String number;
  String title;
  String people;
  String cards;
  String another_approvals;


  @ManyToOne
  RRoute route;


}

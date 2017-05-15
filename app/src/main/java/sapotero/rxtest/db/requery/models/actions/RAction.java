package sapotero.rxtest.db.requery.models.actions;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;
import sapotero.rxtest.db.requery.models.RDocument;


@Entity
public abstract class RAction {
  @Key
  @Generated
  int id;

  String officialId;
  String addressedToId;
  String action;
  String actionDescription;
  String updatedAt;
  String toS;

  @ManyToOne
  RDocument document;

}
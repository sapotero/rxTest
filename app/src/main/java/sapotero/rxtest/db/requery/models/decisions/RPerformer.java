package sapotero.rxtest.db.requery.models.decisions;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;

@Entity
public abstract class RPerformer {
  @Key
  @Generated
  int id;

  String number;
  String performer_id;
  String performer_type;
  String performer_text;
  String organization_text;
  Boolean is_original;
  Boolean is_responsible;

  @ManyToOne
  RBlock block;

}
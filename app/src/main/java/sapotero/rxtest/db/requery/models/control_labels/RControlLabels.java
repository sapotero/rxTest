package sapotero.rxtest.db.requery.models.control_labels;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;
import sapotero.rxtest.db.requery.models.RDocument;

@Entity
public abstract class RControlLabels {
  @Key
  @Generated
  int id;

  String created_at;
  String official_id;
  String official_name;
  String skipped_official_id;
  String skipped_official_name;
  String state;

  @ManyToOne
  RDocument document;

}

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

  String createdAt;
  String officialId;
  String officialName;
  String skippedOfficialId;
  String skippedOfficialName;
  String state;

  @ManyToOne
  RDocument document;

}

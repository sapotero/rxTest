package sapotero.rxtest.db.requery.models.decisions;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;

// Содержит UID резолюций, которые создал и где подписант я
@Entity
public class RDisplayFirstDecision {

  @Key
  @Generated
  int id;

  String decisionUid;
  String userId;
}

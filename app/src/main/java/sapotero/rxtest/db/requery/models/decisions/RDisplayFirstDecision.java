package sapotero.rxtest.db.requery.models.decisions;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;

// resolved https://tasks.n-core.ru/browse/MVDESD-13258
// 1. Созданные мной и подписант я
// Содержит UID резолюций, которые создал и где подписант я
@Entity
public class RDisplayFirstDecision {

  @Key
  @Generated
  int id;

  String decisionUid;
  String userId;
}

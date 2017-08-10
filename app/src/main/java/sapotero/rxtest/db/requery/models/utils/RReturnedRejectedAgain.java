package sapotero.rxtest.db.requery.models.utils;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import sapotero.rxtest.db.requery.models.utils.enums.DocumentCondition;

// Keeps current document condition for returned, rejected, again labels
@Entity
public class RReturnedRejectedAgain {
  @Key
  @Generated
  int id;

  String documentUid;
  String user;
  DocumentCondition documentCondition;
}

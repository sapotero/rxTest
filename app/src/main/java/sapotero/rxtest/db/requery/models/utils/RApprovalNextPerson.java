package sapotero.rxtest.db.requery.models.utils;


import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;

// Keeps track of the approval next person process
@Entity
public class RApprovalNextPerson {
  @Key
  @Generated
  int id;

  String documentUid;
  Boolean taskStarted;
}

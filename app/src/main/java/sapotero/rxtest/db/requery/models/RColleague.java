package sapotero.rxtest.db.requery.models;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Index;
import io.requery.Key;

@Entity
public abstract class RColleague {
  @Key
  @Generated
  int id;

  Integer sortIndex;

  String colleagueId;
  String officialId;
  String officialName;
  Boolean actived;

  @Index("colleague_user_index")
  String user;
}

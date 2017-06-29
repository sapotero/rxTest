package sapotero.rxtest.db.requery.models;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Index;
import io.requery.Key;


@Entity
public abstract class RAssistant {

  @Key
  @Generated
  int id;

  String  headId;
  String  headName;

  @Index("assistantId_index")
  String  assistantId;
  String  assistantName;
  Boolean forDecision;
  String  title;

  @Index("assistant_user_index")
  String  user;

}

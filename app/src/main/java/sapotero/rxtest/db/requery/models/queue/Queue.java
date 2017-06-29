package sapotero.rxtest.db.requery.models.queue;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Index;
import io.requery.Key;

@Entity
public abstract class Queue {

  @Key
  @Generated
  int _id;

  @Index("db_queue_uuid_index")
  @Column(unique = true)
  String uuid;

  String  command;
  String  params;

  @Column(value="false")
  Boolean local;

  @Column(value="false")
  Boolean remote;

  @Column(value="false")
  Boolean running;

  @Column(value="false")
  Boolean withError;

  String  error;

  @Column(value="false")
  Boolean canceled;


//  @Index("db_queue_createdAt_index")
  String  createdAt;

}
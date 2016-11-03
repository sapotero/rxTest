package sapotero.rxtest.db.requery.models;

import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Index;
import io.requery.Key;
import io.requery.OneToOne;
import io.requery.Persistable;

@Entity
public abstract class  RDocument implements Persistable {
  @Key
  @Generated
  int _id;

  @Index("uid_index")
  @Column(unique = true)
  String uid;

  String md5;
  Integer sortKey;
  String title;
  String registrationNumber;
  String registrationDate;
  String urgency;
  String shortDescription;
  String comment;
  String externalDocumentNumber;
  String receiptDate;
  Boolean viewed;

  @Index("changed_index")
  Boolean changed;

  @Index("control_index")
  Boolean control;

  @Index("favorites_index")
  Boolean favorites;

  @ForeignKey
  @OneToOne
  RSigner signer;

}
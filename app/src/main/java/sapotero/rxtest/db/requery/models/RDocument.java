package sapotero.rxtest.db.requery.models;

import java.util.Set;

import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Index;
import io.requery.Key;
import io.requery.OneToMany;
import io.requery.OneToOne;
import io.requery.Persistable;
import sapotero.rxtest.db.requery.models.control_labels.RControlLabels;
import sapotero.rxtest.db.requery.models.decisions.RDecision;
import sapotero.rxtest.db.requery.models.exemplars.RExemplar;
import sapotero.rxtest.db.requery.models.images.RImage;

@Entity
public abstract class  RDocument implements Persistable {
  @Key
  @Generated
  int _id;

  @Index("uid_index")
  @Column(unique = true)
  String uid;

  @Index("md5_index")
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

  @ForeignKey
  @OneToOne
  RSigner signer;

  @Index("organization_index")
  String organization;

  @Index("filter_index")
  String filter;

  String infoCard;

  @Index("folder_index")
  String folder;

  @Index("control_index")
  Boolean control;

  @Index("favorites_index")
  Boolean favorites;

  @Index("processed_index")
  Boolean processed;

  @Index("expired_index")
  Boolean expired;


  @OneToMany(mappedBy = "document")
//  @OneToMany(mappedBy = "document", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
    Set<RDecision> decisions;

//  @OneToMany(mappedBy = "document", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})

  @OneToMany(mappedBy = "document")
  Set<RExemplar> exemplars;

//  @OneToMany(mappedBy = "document", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})

  @OneToMany(mappedBy = "document")
  Set<RImage> images;

//  @OneToMany(mappedBy = "document", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})

  @OneToMany(mappedBy = "document")
  Set<RControlLabels> controlLabels;

  @OneToMany(mappedBy = "document")
  Set<RLinks> links;

  @ForeignKey
  @OneToOne
  RRoute route;

//  exemplars: Тип массив структур Экземпляры документа,
//  decisions: Тип массив структур Резолюции документа,
//  images: Тип массив структур Электронные образы,
//  control_labels: Тип массив структур Контрольные отметки,
//  links: Тип массив. Массив UID связанных документов,
//  route: Тип структура Маршрут прохождения документа

}
package sapotero.rxtest.db.requery.models;

import java.util.Set;

import io.requery.CascadeAction;
import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Index;
import io.requery.Key;
import io.requery.OneToMany;
import io.requery.OneToOne;
import io.requery.Persistable;
import sapotero.rxtest.db.requery.models.actions.RAction;
import sapotero.rxtest.db.requery.models.control_labels.RControlLabels;
import sapotero.rxtest.db.requery.models.decisions.RDecision;
import sapotero.rxtest.db.requery.models.exemplars.RExemplar;
import sapotero.rxtest.db.requery.models.images.RImage;

@Entity
public abstract class RDocument implements Persistable {
  @Index("document_id")
  @Key
  @Generated
  int _id;

  @Index("uid_index")
  @Column(unique = true)
  String uid;

  @Index("doc_year_index")
  int year;

  Integer sortKey;

  String md5;
  String user;
  String addressedToType;
  String title;
  String registrationNumber;
  String registrationDate;
  String urgency;
  String shortDescription;
  String comment;
  String externalDocumentNumber;
  String receiptDate;
  String organization;
  String filter;
  String documentType;
  String infoCard;
  String folder;
  String updatedAt;


  Boolean control;
  Boolean viewed;
  Boolean changed;
  Boolean favorites;
  Boolean processed;

  @Column(value="0")
  Integer processedDate;

  Boolean fromProcessedFolder;

  Boolean fromFavoritesFolder;
  Boolean fromLinks;
  Boolean expired;
  String firstLink;

  @ForeignKey
  @OneToOne
  RSigner signer;

  @Column(value="false")
  Boolean withDecision;

  @Column(value="false")
  Boolean red;

  @OneToMany(mappedBy = "document", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
  Set<RDecision> decisions;

  @OneToMany(mappedBy = "document", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
  Set<RExemplar> exemplars;

  @OneToMany(mappedBy = "document", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
  Set<RImage> images;

  @OneToMany(mappedBy = "document", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
  Set<RControlLabels> controlLabels;

  @OneToMany(mappedBy = "document", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
  Set<RAction> actions;

  @ForeignKey
  @OneToOne
  RRoute route;

//  String links;
  @OneToMany(mappedBy = "document", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
  Set<RLinks> links;

  Boolean returned;

  Boolean rejected;

  Boolean again;

//  exemplars: Тип массив структур Экземпляры документа,
//  decisions: Тип массив структур Резолюции документа,
//  images: Тип массив структур Электронные образы,
//  control_labels: Тип массив структур Контрольные отметки,
//  links: Тип массив. Массив UID связанных документов,
//  route: Тип структура Маршрут прохождения документа
//  operations: Операции по документу

}
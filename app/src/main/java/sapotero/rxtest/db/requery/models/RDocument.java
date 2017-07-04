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

//  @Index("md5_index")
  String md5;

//  @Index("user_index")
  String user;

  //  Запрос на получение документов по api v3
  //  /v3/documents.json?status_code=primary_consideration&addressed_to_type=group
//  @Index("addressedToType_index")
  String addressedToType;

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

//  @Index("changed_index")
  Boolean changed;

  @ForeignKey
  @OneToOne
  RSigner signer;

//  @Index("organization_index")
  String organization;

//  @Index("filter_index")
  String filter;

//  @Index("documentType_index")
  String documentType;

  String infoCard;

//  @Index("folder_index")
  String folder;

//  @Index("control_index")
  Boolean control;

//  @Index("favorites_index")
  Boolean favorites;

//  @Index("processed_index")
  Boolean processed;

  // resolved https://tasks.n-core.ru/browse/MVDESD-13232
  // удалять обработанные за период текущая дата - Срок хранения ЭО в обработанных документах
  Integer processedDate;


  // из папки обработанное
//  @Index("fromProcessedFolder_index")
  Boolean fromProcessedFolder;

  // из папки избранное
//  @Index("fromFavoritesFolder_index")
  Boolean fromFavoritesFolder;

  // из папки обработанное
//  @Index("fromLinks_index")
  Boolean fromLinks;

//  @Index("expired_index")
  Boolean expired;

  @Column(value="false")
  Boolean withDecision;

  @Column(value="false")
  Boolean red;

  // Registration number of the first link in the list of links
  String firstLink;

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

//  exemplars: Тип массив структур Экземпляры документа,
//  decisions: Тип массив структур Резолюции документа,
//  images: Тип массив структур Электронные образы,
//  control_labels: Тип массив структур Контрольные отметки,
//  links: Тип массив. Массив UID связанных документов,
//  route: Тип структура Маршрут прохождения документа
//  operations: Операции по документу

}
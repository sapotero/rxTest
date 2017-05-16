package sapotero.rxtest.db.requery.models.images;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;
import sapotero.rxtest.db.requery.models.RDocument;

@Entity
public abstract class RImage {
  @Key
  @Generated
  int id;

  String imageId;
  Integer number;
  Integer size;
  String title;
  String md5;
  String path;
  String contentType;
  String createdAt;
  Boolean signed;


  @Column(value="false")
  Boolean loading;
  @Column(value="false")
  Boolean complete;
  @Column(value="false")
  Boolean error;

  @ManyToOne
  RDocument document;


}

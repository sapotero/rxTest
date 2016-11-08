package sapotero.rxtest.db.requery.models.images;

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

  Integer number;
  Integer size;
  String title;
  String md5;
  String path;
  String contentType;
  Boolean signed;

  @ManyToOne
  RDocument document;


}

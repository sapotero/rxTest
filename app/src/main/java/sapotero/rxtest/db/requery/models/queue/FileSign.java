package sapotero.rxtest.db.requery.models.queue;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;

@Entity
public abstract class FileSign {

  @Key
  @Generated
  int _id;

  String  filename;
  String  documentId;
  String  imageId;
  String  sign;

}
package sapotero.rxtest.db.requery.models.images;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;


// Keeps track of the image signing process
@Entity
public class RSignImage {
  @Key
  @Generated
  int id;

  String imageId;
  Boolean signed;
  Boolean signing;
  Boolean error;
}

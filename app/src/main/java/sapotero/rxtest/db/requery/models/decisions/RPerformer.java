package sapotero.rxtest.db.requery.models.decisions;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;

@Entity
public abstract class RPerformer {
  @Key
  @Generated
  int id;

  Integer number;
  String performerId;
  String performerType;
  String performerText;
  String organizationText;
  Boolean isOriginal;
  Boolean isResponsible;

  @ManyToOne
  RBlock block;

}
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

  String uid;

  Integer number;
  String performerId;
  String image;
  String performerType;
  String performerText;
  String performerGender;
  String organizationText;
  Boolean isOriginal;
  Boolean isResponsible;
  Boolean isOrganization;
  Boolean forInformation;

  @ManyToOne
  RBlock block;

}
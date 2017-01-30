package sapotero.rxtest.db.requery.models;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;


@Entity
public abstract class RFavoriteUser {

  @Key
  @Generated
  int id;

  String uid;
  String name;
  String organization;
  String position;
  String gender;
  String firstName;
  String lastName;
  String middleName;
  Boolean isOrganization;
  Boolean isGroup;

}

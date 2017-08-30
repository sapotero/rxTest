package sapotero.rxtest.db.requery.models;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Index;
import io.requery.Key;


@Entity
public abstract class RPrimaryConsideration {

  @Key
  @Generated
  int id;

  // resolved https://tasks.n-core.ru/browse/MVDESD-13414
  // Отображать порядок ДЛ в МП, также как в группах СЭД
  // Номер элемента в списке из входящего JSON
  Integer sortIndex;

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
  String image;

  @Index("pc_user_index")
  String  user;
}

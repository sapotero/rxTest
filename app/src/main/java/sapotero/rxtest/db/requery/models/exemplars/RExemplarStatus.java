package sapotero.rxtest.db.requery.models.exemplars;

import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;


@Entity
public abstract class RExemplarStatus {
  @Key
  @Generated
  int id;

  String number;
  String _date;
  String status_code;
  String addressed_to_id;
  String addressed_to_name;
  Boolean is_original;

  @ManyToOne
  RExemplar exemplars;

}

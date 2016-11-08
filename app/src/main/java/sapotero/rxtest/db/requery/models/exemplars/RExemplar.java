package sapotero.rxtest.db.requery.models.exemplars;

import java.util.Set;

import io.requery.CascadeAction;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;
import io.requery.OneToMany;
import sapotero.rxtest.db.requery.models.RDocument;

@Entity
public abstract class RExemplar {
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
  RDocument document;

  @OneToMany(mappedBy = "exemplars", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
  Set<RExemplarStatus> statuses;
}

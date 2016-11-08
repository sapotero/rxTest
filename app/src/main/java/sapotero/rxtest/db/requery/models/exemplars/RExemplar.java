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
  String date;
  String statusCode;
  String addressedToId;
  String addressedToName;
  Boolean isOriginal;

  @ManyToOne
  RDocument document;

  @OneToMany(mappedBy = "exemplars", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
  Set<RExemplar> statuses;

  @ManyToOne
  RExemplar exemplars;

}

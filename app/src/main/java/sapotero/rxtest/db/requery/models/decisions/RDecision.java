package sapotero.rxtest.db.requery.models.decisions;

import io.requery.CascadeAction;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;
import io.requery.OneToMany;
import sapotero.rxtest.db.requery.models.RDocument;

@Entity
public abstract class RDecision {
  @Key
  @Generated
  int id;

  String letterhead;
  String approved;
  String signer;
  String signer_id;
  String assistant_id;
  String signer_blank_text;
  String signer_position_s;
  String comment;
  String _date;
  String urgency_text;
  Boolean signer_is_manager;
  Boolean show_position;

  @ManyToOne
  RDocument document;

  @OneToMany(mappedBy = "decision", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
  RBlock blocks;
}
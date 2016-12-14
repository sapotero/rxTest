package sapotero.rxtest.db.requery.models.decisions;

import java.util.Set;

import io.requery.CascadeAction;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Index;
import io.requery.Key;
import io.requery.ManyToOne;
import io.requery.OneToMany;
import sapotero.rxtest.db.requery.models.RDocument;

@Entity
public abstract class RDecision {
  @Key
  @Generated
  int id;

  String uid;
  String letterhead;
  String signer;
  String signerId;
  String assistantId;
  String signerBlankText;
  String signerPositionS;
  String comment;
  String date;
  String urgencyText;
  Boolean approved;
  Boolean signerIsManager;
  Boolean showPosition;

  String signBase64;

  @Index("decision_changed_index")
  Boolean changed;

  @ManyToOne
  RDocument document;

  @OneToMany(mappedBy = "decision", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
  Set<RBlock> blocks;
}
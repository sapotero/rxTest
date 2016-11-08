package sapotero.rxtest.db.requery.models.decisions;

import java.util.Set;

import io.requery.CascadeAction;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;
import io.requery.OneToMany;

@Entity
public abstract class RBlock {
  @Key
  @Generated
  int id;

  String number;
  String text;
  String appeal_text;
  Boolean text_before;
  Boolean hide_performers;
  Boolean to_copy;
  Boolean to_familiarization;

  @ManyToOne
  RDecision decision;

  @OneToMany(mappedBy = "block", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
  Set<RPerformer> performers;

}
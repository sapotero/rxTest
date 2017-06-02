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

  String uid;

  Integer number;
  String text;
  String appealText;
  String fontSize;
  Boolean textBefore;
  Boolean hidePerformers;
  Boolean toCopy;
  Boolean toFamiliarization;

  @ManyToOne
  RDecision decision;

  @OneToMany(mappedBy = "block", cascade = {CascadeAction.DELETE, CascadeAction.SAVE})
  Set<RPerformer> performers;

}
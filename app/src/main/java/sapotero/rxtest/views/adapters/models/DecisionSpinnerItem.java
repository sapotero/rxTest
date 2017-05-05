package sapotero.rxtest.views.adapters.models;

import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;

public class DecisionSpinnerItem {

  private RDecisionEntity decision;

  public DecisionSpinnerItem(RDecisionEntity decision) {
    this.decision = decision;
  }

  public RDecisionEntity getDecision() {
    return decision;
  }

  public void setDecision(RDecisionEntity decision) {
    this.decision = decision;
  }

  public String getDate() {
    return decision.getDate();
  }

  public String getName() {
    return decision.getSignerBlankText();
  }



}

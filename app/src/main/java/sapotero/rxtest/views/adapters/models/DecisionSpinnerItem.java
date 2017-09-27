package sapotero.rxtest.views.adapters.models;

import sapotero.rxtest.retrofit.models.document.Decision;

public class DecisionSpinnerItem {

  private Decision decision;

  public DecisionSpinnerItem(Decision decision) {
    this.decision = decision;
  }

  public Decision getDecision() {
    return decision;
  }

  public void setDecision(Decision decision) {
    this.decision = decision;
  }

  public String getDate() {
    return decision.getDate();
  }

  public String getName() {
    return decision.getSignerBlankText();
  }



}

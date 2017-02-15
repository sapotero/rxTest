package sapotero.rxtest.views.adapters.models;

import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;

public class DecisionSpinnerItem {

  private RDecisionEntity decision;
  private String name;
  private String date;

  public DecisionSpinnerItem(RDecisionEntity decision, String name, String date) {
    this.decision = decision;
    this.name = name;
    this.date = date;
  }

  public DecisionSpinnerItem(RDecisionEntity decision, String name, int size) {
    this.decision = decision;
    this.name = name;
    this.date = String.valueOf(size);
  }

  public RDecisionEntity getDecision() {
    return decision;
  }

  public void setDecision(RDecisionEntity decision) {
    this.decision = decision;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


}

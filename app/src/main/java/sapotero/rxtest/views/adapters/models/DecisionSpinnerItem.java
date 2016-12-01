package sapotero.rxtest.views.adapters.models;

import sapotero.rxtest.retrofit.models.document.Decision;

public class DecisionSpinnerItem {

  private Decision decision;
  private String name;
  private String date;

  public DecisionSpinnerItem(Decision raw_decision, String name, String date) {
    this.decision = raw_decision;
    this.name = name;
    this.date = date;
  }

  public DecisionSpinnerItem(Decision raw_decision, String name, int size) {
    this.decision = raw_decision;
    this.name = name;
    this.date = String.valueOf(size);
  }

  public Decision getDecision() {
    return decision;
  }

  public void setDecision(Decision decision) {
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

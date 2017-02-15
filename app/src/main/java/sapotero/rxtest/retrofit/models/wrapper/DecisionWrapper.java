package sapotero.rxtest.retrofit.models.wrapper;

import com.google.gson.annotations.SerializedName;

import sapotero.rxtest.retrofit.models.document.Decision;


public class DecisionWrapper {
  @SerializedName("decision")
  private Decision decision;

  public Decision getDecision() {
    return decision;
  }

  public void setDecision(Decision decision) {
    this.decision = decision;
  }
}

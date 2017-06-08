package sapotero.rxtest.retrofit.models.v2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class DecisionError {
  @SerializedName("errors")
  @Expose
  private List<String> errors;

  @SerializedName("document_uid")
  @Expose
  private String documentUid = "";

  @SerializedName("id")
  @Expose
  private String decisionUid;

  @SerializedName("signer_id")
  @Expose
  private String decisionSignerId;

  public List<String> getErrors() {
    return errors;
  }

  public void setErrors(List<String> errors) {
    this.errors = errors;
  }

  public String getDocumentUid() {
    return documentUid;
  }

  public void setDocumentUid(String documentUid) {
    this.documentUid = documentUid;
  }

  public String getDecisionUid() {
    return decisionUid;
  }

  public void setDecisionUid(String decisionUid) {
    this.decisionUid = decisionUid;
  }

  public String getDecisionSignerId() {
    return decisionSignerId;
  }

  public void setDecisionSignerId(String decisionSignerId) {
    this.decisionSignerId = decisionSignerId;
  }
}

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
}

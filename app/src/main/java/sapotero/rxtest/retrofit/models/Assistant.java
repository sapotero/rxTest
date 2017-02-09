package sapotero.rxtest.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Assistant implements Serializable {

  @SerializedName("head_id")
  @Expose
  private String headId;
  @SerializedName("head_name")
  @Expose
  private String headName;
  @SerializedName("assistant_id")
  @Expose
  private String assistantId;
  @SerializedName("assistant_name")
  @Expose
  private String assistantName;
  @SerializedName("for_decision")
  @Expose
  private Boolean forDecision;
  @SerializedName("to_s")
  @Expose
  private String toS;

  public String getHeadId() {
    return headId;
  }

  public void setHeadId(String headId) {
    this.headId = headId;
  }

  public String getHeadName() {
    return headName;
  }

  public void setHeadName(String headName) {
    this.headName = headName;
  }

  public String getAssistantId() {
    return assistantId;
  }

  public void setAssistantId(String assistantId) {
    this.assistantId = assistantId;
  }

  public String getAssistantName() {
    return assistantName;
  }

  public void setAssistantName(String assistantName) {
    this.assistantName = assistantName;
  }

  public Boolean getForDecision() {
    return forDecision;
  }

  public void setForDecision(Boolean forDecision) {
    this.forDecision = forDecision;
  }

  public String getToS() {
    return toS;
  }

  public void setToS(String toS) {
    this.toS = toS;
  }

}
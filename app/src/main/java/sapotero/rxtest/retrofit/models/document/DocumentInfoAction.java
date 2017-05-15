package sapotero.rxtest.retrofit.models.document;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DocumentInfoAction {

  @SerializedName("official_id")
  @Expose
  private String officialId;

  @SerializedName("addressed_to_id")
  @Expose
  private String addressedToId;

  @SerializedName("action")
  @Expose
  private String action;

  @SerializedName("action_description")
  @Expose
  private String actionDescription;

  @SerializedName("updated_at")
  @Expose
  private String updatedAt;

  @SerializedName("to_s")
  @Expose
  private String toS;

  public String getOfficialId() {
    return officialId;
  }

  public void setOfficialId(String officialId) {
    this.officialId = officialId;
  }

  public String getAddressedToId() {
    return addressedToId;
  }

  public void setAddressedToId(String addressedToId) {
    this.addressedToId = addressedToId;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getActionDescription() {
    return actionDescription;
  }

  public void setActionDescription(String actionDescription) {
    this.actionDescription = actionDescription;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getToS() {
    return toS;
  }

  public void setToS(String toS) {
    this.toS = toS;
  }
}
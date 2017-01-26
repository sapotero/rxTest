package sapotero.rxtest.retrofit.models.document;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Operation {

  @SerializedName("approve")
  @Expose
  private String approve;

  @SerializedName("cancel")
  @Expose
  private String cancel;

  @SerializedName("to_control")
  @Expose
  private String to_control;

  @SerializedName("skip_control")
  @Expose
  private String skip_control;

  @SerializedName("to_favorites")
  @Expose
  private String to_favorites;

  @SerializedName("to_the_primary_consideration")
  @Expose
  private String to_the_primary_consideration;

  public String getApprove() {
    return approve;
  }

  public void setApprove(String approve) {
    this.approve = approve;
  }

  public String getCancel() {
    return cancel;
  }

  public void setCancel(String cancel) {
    this.cancel = cancel;
  }

  public String getTo_control() {
    return to_control;
  }

  public void setTo_control(String to_control) {
    this.to_control = to_control;
  }

  public String getSkip_control() {
    return skip_control;
  }

  public void setSkip_control(String skip_control) {
    this.skip_control = skip_control;
  }

  public String getTo_favorites() {
    return to_favorites;
  }

  public void setTo_favorites(String to_favorites) {
    this.to_favorites = to_favorites;
  }

  public String getTo_the_primary_consideration() {
    return to_the_primary_consideration;
  }

  public void setTo_the_primary_consideration(String to_the_primary_consideration) {
    this.to_the_primary_consideration = to_the_primary_consideration;
  }
}
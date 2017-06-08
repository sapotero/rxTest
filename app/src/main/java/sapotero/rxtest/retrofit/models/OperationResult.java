package sapotero.rxtest.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OperationResult {

  @SerializedName("ok")
  @Expose
  private String ok;

  @SerializedName("message")
  @Expose
  private String message;

  @SerializedName("type")
  @Expose
  private String type;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getOk() {
    return ok;
  }

  public void setOk(String ok) {
    this.ok = ok;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

}
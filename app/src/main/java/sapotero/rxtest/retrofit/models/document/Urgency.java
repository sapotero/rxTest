package sapotero.rxtest.retrofit.models.document;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Urgency implements Serializable {

  @SerializedName("_id")
  @Expose
  private String id;
  @SerializedName("code")
  @Expose
  private String code;
  @SerializedName("is_deleted")
  @Expose
  private String isDeleted;
  @SerializedName("name")
  @Expose
  private String name;
  
  @SerializedName("priority")
  @Expose
  private String priority;
  @SerializedName("provider_id")
  @Expose
  private String providerId;
  @SerializedName("rating")
  @Expose
  private String rating;
  @SerializedName("system_code")
  @Expose
  private String systemCode;
  @SerializedName("undeletable")
  @Expose
  private Boolean undeletable;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getIsDeleted() {
    return isDeleted;
  }

  public void setIsDeleted(String isDeleted) {
    this.isDeleted = isDeleted;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  public String getRating() {
    return rating;
  }

  public void setRating(String rating) {
    this.rating = rating;
  }

  public String getSystemCode() {
    return systemCode;
  }

  public void setSystemCode(String systemCode) {
    this.systemCode = systemCode;
  }

  public Boolean getUndeletable() {
    return undeletable;
  }

  public void setUndeletable(Boolean undeletable) {
    this.undeletable = undeletable;
  }
}
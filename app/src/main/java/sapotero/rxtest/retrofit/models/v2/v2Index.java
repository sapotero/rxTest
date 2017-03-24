package sapotero.rxtest.retrofit.models.v2;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class v2Index {

  @SerializedName("uid")
  @Expose
  private String uid;
  @SerializedName("cache_key")
  @Expose
  private String cacheKey;
  @SerializedName("sort_key")
  @Expose
  private Integer sortKey;
  @SerializedName("title")
  @Expose
  private String title;
  @SerializedName("description")
  @Expose
  private String description;
  @SerializedName("registration_number")
  @Expose
  private String registrationNumber;
  @SerializedName("signer")
  @Expose
  private v2UserOshs signer;
  @SerializedName("urgency")
  @Expose
  private String urgency;

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  public String getCacheKey() {
    return cacheKey;
  }

  public void setCacheKey(String cacheKey) {
    this.cacheKey = cacheKey;
  }

  public Integer getSortKey() {
    return sortKey;
  }

  public void setSortKey(Integer sortKey) {
    this.sortKey = sortKey;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getRegistrationNumber() {
    return registrationNumber;
  }

  public void setRegistrationNumber(String registrationNumber) {
    this.registrationNumber = registrationNumber;
  }

  public v2UserOshs getSigner() {
    return signer;
  }

  public void setSigner(v2UserOshs signer) {
    this.signer = signer;
  }

  public String getUrgency() {
    return urgency;
  }

  public void setUrgency(String urgency) {
    this.urgency = urgency;
  }

}

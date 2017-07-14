package sapotero.rxtest.retrofit.models;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Colleague implements Serializable {

  @SerializedName("id")
  @Expose
  private String colleagueId;

  @SerializedName("official_id")
  @Expose
  private String officialId;

  @SerializedName("official_name")
  @Expose
  private String officialName;

  @SerializedName("actived")
  @Expose
  private Boolean actived;

  public String getColleagueId() {
    return colleagueId;
  }

  public void setColleagueId(String colleagueId) {
    this.colleagueId = colleagueId;
  }

  public String getOfficialId() {
    return officialId;
  }

  public void setOfficialId(String officialId) {
    this.officialId = officialId;
  }

  public String getOfficialName() {
    return officialName;
  }

  public void setOfficialName(String officialName) {
    this.officialName = officialName;
  }

  public Boolean getActived() {
    return actived;
  }

  public void setActived(Boolean actived) {
    this.actived = actived;
  }
}

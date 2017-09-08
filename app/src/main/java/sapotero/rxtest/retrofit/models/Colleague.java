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

  @SerializedName("auth_token")
  @Expose
  private String authToken;

  @SerializedName("login")
  @Expose
  private String login;

  // Colleague с непустыми authToken и login возвращается в ответ на запрос перехода в режим замещения

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

  public String getAuthToken() {
    return authToken;
  }

  public String getLogin() {
    return login;
  }
}

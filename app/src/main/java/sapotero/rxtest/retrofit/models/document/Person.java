package sapotero.rxtest.retrofit.models.document;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Person {

  @SerializedName("official_id")
  @Expose
  private String officialId;
  @SerializedName("official_name")
  @Expose
  private String officialName;
  @SerializedName("sign_png")
  @Expose
  private Object signPng;
  @SerializedName("actions")
  @Expose
  private List<Action> actions = null;

  /**
   *
   * @return
   * The officialId
   */
  public String getOfficialId() {
    return officialId;
  }

  /**
   *
   * @param officialId
   * The official_id
   */
  public void setOfficialId(String officialId) {
    this.officialId = officialId;
  }

  /**
   *
   * @return
   * The officialName
   */
  public String getOfficialName() {
    return officialName;
  }

  /**
   *
   * @param officialName
   * The official_name
   */
  public void setOfficialName(String officialName) {
    this.officialName = officialName;
  }

  /**
   *
   * @return
   * The signPng
   */
  public Object getSignPng() {
    return signPng;
  }

  /**
   *
   * @param signPng
   * The sign_png
   */
  public void setSignPng(Object signPng) {
    this.signPng = signPng;
  }

  /**
   *
   * @return
   * The actions
   */
  public List<Action> getActions() {
    return actions;
  }

  /**
   *
   * @param actions
   * The actions
   */
  public void setActions(List<Action> actions) {
    this.actions = actions;
  }

}
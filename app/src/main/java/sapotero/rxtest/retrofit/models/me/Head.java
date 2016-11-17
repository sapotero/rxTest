package sapotero.rxtest.retrofit.models.me;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Head {

  @SerializedName("official_id")
  @Expose
  private String officialId;
  @SerializedName("official_name")
  @Expose
  private String officialName;

  /**
   *
   * @return
   *     The officialId
   */
  public String getOfficialId() {
    return officialId;
  }

  /**
   *
   * @param officialId
   *     The official_id
   */
  public void setOfficialId(String officialId) {
    this.officialId = officialId;
  }

  public Head withOfficialId(String officialId) {
    this.officialId = officialId;
    return this;
  }

  /**
   *
   * @return
   *     The officialName
   */
  public String getOfficialName() {
    return officialName;
  }

  /**
   *
   * @param officialName
   *     The official_name
   */
  public void setOfficialName(String officialName) {
    this.officialName = officialName;
  }

  public Head withOfficialName(String officialName) {
    this.officialName = officialName;
    return this;
  }


}

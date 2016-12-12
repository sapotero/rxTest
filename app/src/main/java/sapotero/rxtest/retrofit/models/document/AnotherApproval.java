package sapotero.rxtest.retrofit.models.document;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AnotherApproval {

  @SerializedName("official_name")
  @Expose
  private String officialName;
  @SerializedName("comment")
  @Expose
  private String comment;

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
   * The comment
   */
  public String getComment() {
    return comment;
  }

  /**
   *
   * @param comment
   * The comment
   */
  public void setComment(String comment) {
    this.comment = comment;
  }

}
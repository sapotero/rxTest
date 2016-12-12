package sapotero.rxtest.retrofit.models.document;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Card {

  @SerializedName("uid")
  @Expose
  private String uid;
  @SerializedName("original_approval")
  @Expose
  private String originalApproval;
  @SerializedName("full_text_approval")
  @Expose
  private String fullTextApproval;

  /**
   *
   * @return
   * The uid
   */
  public String getUid() {
    return uid;
  }

  /**
   *
   * @param uid
   * The uid
   */
  public void setUid(String uid) {
    this.uid = uid;
  }

  /**
   *
   * @return
   * The originalApproval
   */
  public String getOriginalApproval() {
    return originalApproval;
  }

  /**
   *
   * @param originalApproval
   * The original_approval
   */
  public void setOriginalApproval(String originalApproval) {
    this.originalApproval = originalApproval;
  }

  /**
   *
   * @return
   * The fullTextApproval
   */
  public String getFullTextApproval() {
    return fullTextApproval;
  }

  /**
   *
   * @param fullTextApproval
   * The full_text_approval
   */
  public void setFullTextApproval(String fullTextApproval) {
    this.fullTextApproval = fullTextApproval;
  }

}
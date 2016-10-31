package sapotero.rxtest.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DownloadLink {

  @SerializedName("expired_link")
  @Expose
  private String expiredLink;
  @SerializedName("digest")
  @Expose
  private String digest;

  /**
   *
   * @return
   * The expiredLink
   */
  public String getExpiredLink() {
    return expiredLink;
  }

  /**
   *
   * @param expiredLink
   * The expired_link
   */
  public void setExpiredLink(String expiredLink) {
    this.expiredLink = expiredLink;
  }

  /**
   *
   * @return
   * The digest
   */
  public String getDigest() {
    return digest;
  }

  /**
   *
   * @param digest
   * The digest
   */
  public void setDigest(String digest) {
    this.digest = digest;
  }

}
package sapotero.rxtest.retrofit.models.document;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Action {

  @SerializedName("date")
  @Expose
  private String date;
  @SerializedName("status")
  @Expose
  private String status;
  @SerializedName("comment")
  @Expose
  private Object comment;

  /**
   *
   * @return
   * The date
   */
  public String getDate() {
    return date;
  }

  /**
   *
   * @param date
   * The date
   */
  public void setDate(String date) {
    this.date = date;
  }

  /**
   *
   * @return
   * The status
   */
  public String getStatus() {
    return status;
  }

  /**
   *
   * @param status
   * The status
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   *
   * @return
   * The comment
   */
  public Object getComment() {
    return comment;
  }

  /**
   *
   * @param comment
   * The comment
   */
  public void setComment(Object comment) {
    this.comment = comment;
  }

}
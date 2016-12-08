package sapotero.rxtest.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Folder implements Serializable {

  @SerializedName("id")
  @Expose
  private String id;
  @SerializedName("title")
  @Expose
  private String title;
  @SerializedName("type")
  @Expose
  private String type;

  /**
   *
   * @return
   * The id
   */
  public String getId() {
    return id;
  }

  /**
   *
   * @param id
   * The id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   *
   * @return
   * The title
   */
  public String getTitle() {
    return title;
  }

  /**
   *
   * @param title
   * The title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   *
   * @return
   * The type
   */
  public String getType() {
    return type;
  }

  /**
   *
   * @param type
   * The type
   */
  public void setType(String type) {
    this.type = type;
  }

}
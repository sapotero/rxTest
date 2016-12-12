package sapotero.rxtest.retrofit.models.document;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Route {

  @SerializedName("title")
  @Expose
  private String title;
  @SerializedName("steps")
  @Expose
  private List<Step> steps = null;

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
   * The steps
   */
  public List<Step> getSteps() {
    return steps;
  }

  /**
   *
   * @param steps
   * The steps
   */
  public void setSteps(List<Step> steps) {
    this.steps = steps;
  }

}
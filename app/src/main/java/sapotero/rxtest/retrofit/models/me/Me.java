
package sapotero.rxtest.retrofit.models.me;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Me {

  @SerializedName("id")
  @Expose
  private String id;
  @SerializedName("name")
  @Expose
  private String name;

  /**
   *
   * @return
   *     The id
   */
  public String getId() {
    return id;
  }

  /**
   *
   * @param id
   *     The id
   */
  public void setId(String id) {
    this.id = id;
  }

  public Me withId(String id) {
    this.id = id;
    return this;
  }

  /**
   *
   * @return
   *     The name
   */
  public String getName() {
    return name;
  }

  /**
   *
   * @param name
   *     The name
   */
  public void setName(String name) {
    this.name = name;
  }

  public Me withName(String name) {
    this.name = name;
    return this;
  }

}

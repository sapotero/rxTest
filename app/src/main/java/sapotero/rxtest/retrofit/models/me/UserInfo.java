
package sapotero.rxtest.retrofit.models.me;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class UserInfo {

  @SerializedName("me")
  @Expose
  private Me me;
  @SerializedName("heads")
  @Expose
  private List<Head> heads = new ArrayList<Head>();

  /**
   *
   * @return
   *     The me
   */
  public Me getMe() {
    return me;
  }

  /**
   *
   * @param me
   *     The me
   */
  public void setMe(Me me) {
    this.me = me;
  }

  public UserInfo withMe(Me me) {
    this.me = me;
    return this;
  }

  /**
   *
   * @return
   *     The heads
   */
  public List<Head> getHeads() {
    return heads;
  }

  /**
   *
   * @param heads
   *     The heads
   */
  public void setHeads(List<Head> heads) {
    this.heads = heads;
  }

  public UserInfo withHeads(List<Head> heads) {
    this.heads = heads;
    return this;
  }
}

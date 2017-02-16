package sapotero.rxtest.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AnotherApproval implements Serializable {
  @SerializedName("comment")
  @Expose
  public String comment;

  @SerializedName("official_name")
  @Expose
  public String official_name;


  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getOfficial_name() {
    return official_name;
  }

  public void setOfficial_name(String official_name) {
    this.official_name = official_name;
  }
}

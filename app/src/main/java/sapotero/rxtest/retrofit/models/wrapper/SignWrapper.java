package sapotero.rxtest.retrofit.models.wrapper;

import com.google.gson.annotations.SerializedName;

public class SignWrapper {

  @SerializedName("sign")
  private String sign;

  public String getSign() {
    return sign;
  }

  public void setSign(String sign) {
    this.sign = sign;
  }
}

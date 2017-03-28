package sapotero.rxtest.retrofit.models.old_decision;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DecisionResponce {

  @SerializedName("id")
  @Expose
  private String id;
  @SerializedName("comment")
  @Expose
  private String comment;
  @SerializedName("document_uid")
  @Expose
  private String documentUid;
  @SerializedName("letterhead")
  @Expose
  private Object letterhead;
  @SerializedName("full_blocked")
  @Expose
  private Boolean fullBlocked;
  @SerializedName("date")
  @Expose
  private String date;
  @SerializedName("letterhead_font_size")
  @Expose
  private Object letterheadFontSize;
  @SerializedName("performers_font_size")
  @Expose
  private Integer performersFontSize;
  @SerializedName("urgency_id")
  @Expose
  private Object urgencyId;
  @SerializedName("urgency_text")
  @Expose
  private Object urgencyText;
  @SerializedName("sign")
  @Expose
  private Object sign;
  @SerializedName("raw_sign")
  @Expose
  private Object rawSign;
  @SerializedName("signer_id")
  @Expose
  private String signerId;
  @SerializedName("signer_text")
  @Expose
  private Object signerText;
  @SerializedName("show_position")
  @Expose
  private Boolean showPosition;


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getDocumentUid() {
    return documentUid;
  }

  public void setDocumentUid(String documentUid) {
    this.documentUid = documentUid;
  }

  public Object getLetterhead() {
    return letterhead;
  }

  public void setLetterhead(Object letterhead) {
    this.letterhead = letterhead;
  }

  public Boolean getFullBlocked() {
    return fullBlocked;
  }

  public void setFullBlocked(Boolean fullBlocked) {
    this.fullBlocked = fullBlocked;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public Object getLetterheadFontSize() {
    return letterheadFontSize;
  }

  public void setLetterheadFontSize(Object letterheadFontSize) {
    this.letterheadFontSize = letterheadFontSize;
  }

  public Integer getPerformersFontSize() {
    return performersFontSize;
  }

  public void setPerformersFontSize(Integer performersFontSize) {
    this.performersFontSize = performersFontSize;
  }

  public Object getUrgencyId() {
    return urgencyId;
  }

  public void setUrgencyId(Object urgencyId) {
    this.urgencyId = urgencyId;
  }

  public Object getUrgencyText() {
    return urgencyText;
  }

  public void setUrgencyText(Object urgencyText) {
    this.urgencyText = urgencyText;
  }

  public Object getSign() {
    return sign;
  }

  public void setSign(Object sign) {
    this.sign = sign;
  }

  public Object getRawSign() {
    return rawSign;
  }

  public void setRawSign(Object rawSign) {
    this.rawSign = rawSign;
  }

  public String getSignerId() {
    return signerId;
  }

  public void setSignerId(String signerId) {
    this.signerId = signerId;
  }

  public Object getSignerText() {
    return signerText;
  }

  public void setSignerText(Object signerText) {
    this.signerText = signerText;
  }

  public Boolean getShowPosition() {
    return showPosition;
  }

  public void setShowPosition(Boolean showPosition) {
    this.showPosition = showPosition;
  }

}
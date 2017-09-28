package sapotero.rxtest.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import sapotero.rxtest.retrofit.models.document.IPerformer;

public class Assistant implements Serializable, IPerformer {

  @SerializedName("head_id")
  @Expose
  private String headId;
  @SerializedName("head_name")
  @Expose
  private String headName;
  @SerializedName("assistant_id")
  @Expose
  private String assistantId;
  @SerializedName("assistant_name")
  @Expose
  private String assistantName;
  @SerializedName("for_decision")
  @Expose
  private Boolean forDecision;
  @SerializedName("to_s")
  @Expose
  private String toS;
  @SerializedName("image")
  @Expose
  private String image;

  @SerializedName("for_information")
  @Expose
  private Boolean forInformation;

  public Boolean getForInformation() {
    return forInformation;
  }

  public Assistant setForInformation(Boolean forInformation) {
    this.forInformation = forInformation;
    return this;
  }

  public String getImage() {
    return image;
  }

  public Assistant setImage(String image) {
    this.image = image;
    return this;
  }

  public String getHeadId() {
    return headId;
  }

  public void setHeadId(String headId) {
    this.headId = headId;
  }

  public String getHeadName() {
    return headName;
  }

  public void setHeadName(String headName) {
    this.headName = headName;
  }

  public String getAssistantId() {
    return assistantId;
  }

  public void setAssistantId(String assistantId) {
    this.assistantId = assistantId;
  }

  public String getAssistantName() {
    return assistantName;
  }

  public void setAssistantName(String assistantName) {
    this.assistantName = assistantName;
  }

  public Boolean getForDecision() {
    return forDecision;
  }

  public void setForDecision(Boolean forDecision) {
    this.forDecision = forDecision;
  }

  public String getToS() {
    return toS;
  }

  public void setToS(String toS) {
    this.toS = toS;
  }


  @Override
  public String getIPerformerUid() {
    return null;
  }

  @Override
  public void setIPerformerUid(String uid) {

  }

  @Override
  public Integer getIPerformerNumber() {
    return null;
  }

  @Override
  public void setIPerformerNumber(Integer number) {

  }

  @Override
  public String getIPerformerId() {
    return null;
  }

  @Override
  public void setIPerformerId(String id) {

  }

  @Override
  public String getIPerformerType() {
    return null;
  }

  @Override
  public void setIPerformerType(String type) {

  }

  @Override
  public String getIPerformerName() {
    return null;
  }

  @Override
  public void setIPerformerName(String name) {

  }

  @Override
  public String getIPerformerGender() {
    return null;
  }

  @Override
  public void setIPerformerGender(String gender) {

  }

  @Override
  public String getIPerformerOrganizationName() {
    return null;
  }

  @Override
  public void setIPerformerOrganizationName(String organizationName) {

  }

  @Override
  public String getIPerformerAssistantId() {
    return null;
  }

  @Override
  public void setIPerformerAssistantId(String assistantId) {

  }

  @Override
  public String getIPerformerPosition() {
    return null;
  }

  @Override
  public void setIPerformerPosition(String position) {

  }

  @Override
  public String getIPerformerLastName() {
    return null;
  }

  @Override
  public void setIPerformerLastName(String lastName) {

  }

  @Override
  public String getIPerformerFirstName() {
    return null;
  }

  @Override
  public void setIPerformerFirstName(String firstName) {

  }

  @Override
  public String getIPerformerMiddleName() {
    return null;
  }

  @Override
  public void setIPerformerMiddleName(String middleName) {

  }

  @Override
  public String getIPerformerImage() {
    return null;
  }

  @Override
  public void setIPerformerImage(String image) {

  }

  @Override
  public Boolean isIPerformerOriginal() {
    return null;
  }

  @Override
  public void setIsIPerformerOriginal(Boolean isOriginal) {

  }

  @Override
  public Boolean isIPerformerResponsible() {
    return null;
  }

  @Override
  public void setIsIPerformerResponsible(Boolean isResponsible) {

  }

  @Override
  public Boolean isIPerformerGroup() {
    return null;
  }

  @Override
  public void setIsIPerformerGroup(Boolean isGroup) {

  }

  @Override
  public Boolean isIPerformerOrganization() {
    return null;
  }

  @Override
  public void setIsIPerformerOrganization(Boolean isOrganization) {

  }

  @Override
  public String getIImage() {
    return getImage();
  }

  @Override
  public void setIImage(String image) {
    setImage(image);
  }

  @Override
  public Boolean getIForInformation() {
    return getForInformation();
  }

  @Override
  public void setIForInformation(Boolean forInformation) {
    setForInformation(forInformation);
  }
}
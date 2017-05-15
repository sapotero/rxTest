package sapotero.rxtest.views.adapters.utils;

import sapotero.rxtest.retrofit.models.document.Performer;

public class PrimaryConsiderationPeople {

  private String assistantId = null;

  private String id;
  private String name;
  private String position;
  private String organization;
  private String gender;
  private boolean isOriginal = false;
  private boolean isResponsible = false;
  private boolean isOrganization = false;

  public PrimaryConsiderationPeople(String id, String name, String position, String organization, String assistantId, String gender, boolean isOrganization) {
    this.id = id;
    this.name = name;
    this.position = position;
    this.organization = organization;
    this.assistantId = assistantId;
    this.gender = gender;
    this.isOrganization = isOrganization;
  }

  public PrimaryConsiderationPeople(Performer u) {
    if (u != null) {
      this.id = u.getPerformerId();

      this.name = u.getPerformerText();
      this.position = String.valueOf( u.getNumber() );
      this.organization = u.getOrganizationText();
      this.gender = u.getPerformerGender();
      this.isOriginal = getBooleanValue(u.getIsOriginal());
      this.isResponsible = getBooleanValue(u.getIsResponsible());
      this.isOrganization = getBooleanValue(u.getOrganization());
    }
  }

  private boolean getBooleanValue(Boolean value) {
    if (value != null) {
      return value;
    } else {
      return false;
    }
  }

  private String uid;
  public String getAssistantId() {
    return assistantId;
  }

  public void setAssistantId(String assistantId) {
    this.assistantId = assistantId;
  }


  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public boolean isOriginal() {
    return isOriginal;
  }

  public void setOriginal(boolean original) {
    this.isOriginal = original;
  }

  public boolean isResponsible() {
    return isResponsible;
  }

  public void setResponsible(boolean responsible) {
    this.isResponsible = responsible;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public boolean isOrganization() {
    return isOrganization;
  }

  public void setIsOrganization(boolean organization) {
    isOrganization = organization;
  }
}

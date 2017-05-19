package sapotero.rxtest.views.adapters.utils;

import sapotero.rxtest.retrofit.models.document.IPerformer;
import sapotero.rxtest.retrofit.models.document.Performer;

public class PrimaryConsiderationPeople implements IPerformer {

  private String assistantId = null;

  private String id;
  private String name;
  private String position;
  private String organization;
  private String gender;
  private boolean isOriginal = false;
  private boolean isResponsible = false;
  private boolean isOrganization = false;

  public PrimaryConsiderationPeople() {
  }

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

  @Override
  public Integer getIPerformerNumber() {
    return null;
  }

  @Override
  public void setIPerformerNumber(Integer number) {
  }

  @Override
  public String getIPerformerId() {
    return getId();
  }

  @Override
  public void setIPerformerId(String id) {
    setId(id);
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
    return getName();
  }

  @Override
  public void setIPerformerName(String name) {
    setName(name);
  }

  @Override
  public String getIPerformerGender() {
    return getGender();
  }

  @Override
  public void setIPerformerGender(String gender) {
    setGender(gender);
  }

  @Override
  public String getIPerformerOrganizationName() {
    return getOrganization();
  }

  @Override
  public void setIPerformerOrganizationName(String organizationName) {
    setOrganization(organizationName);
  }

  @Override
  public String getIPerformerAssistantId() {
    return getAssistantId();
  }

  @Override
  public void setIPerformerAssistantId(String assistantId) {
    setAssistantId(assistantId);
  }

  @Override
  public String getIPerformerPosition() {
    return getPosition();
  }

  @Override
  public void setIPerformerPosition(String position) {
    setPosition(position);
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
    return isOriginal();
  }

  @Override
  public void setIsIPerformerOriginal(Boolean isOriginal) {
    setOriginal(isOriginal);
  }

  @Override
  public Boolean isIPerformerResponsible() {
    return isResponsible();
  }

  @Override
  public void setIsIPerformerResponsible(Boolean isResponsible) {
    setResponsible(isResponsible);
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
    return isOrganization();
  }

  @Override
  public void setIsIPerformerOrganization(Boolean isOrganization) {
    setIsOrganization(isOrganization);
  }
}

package sapotero.rxtest.views.adapters.utils;

import sapotero.rxtest.retrofit.models.document.IPerformer;

public class PrimaryConsiderationPeople implements IPerformer {

  private String assistantId = null;

  private String uid;

  private String id;
  private String name;
  private String position;
  private String image;
  private String organization;
  private String gender;
  private boolean isOriginal = false;
  private boolean isResponsible = false;
  private boolean isOrganization = false;

  // resolved https://tasks.n-core.ru/browse/MVDESD-13414
  // Отображать порядок ДЛ в МП, также как в группах СЭД
  // Номер элемента в списке из входящего JSON
  private Integer sortIndex;

  public PrimaryConsiderationPeople() {
  }

  private boolean getBooleanValue(Boolean value) {
    if (value != null) {
      return value;
    } else {
      return false;
    }
  }

  public String getImage() {
    return image;
  }

  public PrimaryConsiderationPeople setImage(String image) {
    this.image = image;
    return this;
  }

  public String getAssistantId() {
    return assistantId;
  }

  public void setAssistantId(String assistantId) {
    this.assistantId = assistantId;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
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

  public Integer getSortIndex() {
    return sortIndex;
  }

  public void setSortIndex(Integer sortIndex) {
    this.sortIndex = sortIndex;
  }

  @Override
  public String getIPerformerUid() {
    return getUid();
  }

  @Override
  public void setIPerformerUid(String uid) {
    setUid(uid);
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
    setOriginal( getBooleanValue(isOriginal) );
  }

  @Override
  public Boolean isIPerformerResponsible() {
    return isResponsible();
  }

  @Override
  public void setIsIPerformerResponsible(Boolean isResponsible) {
    setResponsible( getBooleanValue(isResponsible) );
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
    setIsOrganization( getBooleanValue(isOrganization) );
  }

  @Override
  public String getIImage() {
    return getImage();
  }

  @Override
  public void setIImage(String image) {
    setImage(image);
  }
}

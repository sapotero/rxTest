package sapotero.rxtest.retrofit.models.document;

// Used for conversions between Performer, PrimaryConsiderationPeople and Oshs
public interface IPerformer {
  String getIPerformerUid();
  void setIPerformerUid(String uid);
  Integer getIPerformerNumber();
  void setIPerformerNumber(Integer number);
  String getIPerformerId();
  void setIPerformerId(String id);
  String getIPerformerType();
  void setIPerformerType(String type);
  String getIPerformerName();
  void setIPerformerName(String name);
  String getIPerformerGender();
  void setIPerformerGender(String gender);
  String getIPerformerOrganizationName();
  void setIPerformerOrganizationName(String organizationName);
  String getIPerformerAssistantId();
  void setIPerformerAssistantId(String assistantId);
  String getIPerformerPosition();
  void setIPerformerPosition(String position);
  String getIPerformerLastName();
  void setIPerformerLastName(String lastName);
  String getIPerformerFirstName();
  void setIPerformerFirstName(String firstName);
  String getIPerformerMiddleName();
  void setIPerformerMiddleName(String middleName);
  String getIPerformerImage();
  void setIPerformerImage(String image);
  Boolean isIPerformerOriginal();
  void setIsIPerformerOriginal(Boolean isOriginal);
  Boolean isIPerformerResponsible();
  void setIsIPerformerResponsible(Boolean isResponsible);
  Boolean isIPerformerGroup();
  void setIsIPerformerGroup(Boolean isGroup);
  Boolean isIPerformerOrganization();
  void setIsIPerformerOrganization(Boolean isOrganization);
  String getIImage();
  void setIImage(String image);

  Boolean getIForInformation();
  void setIForInformation(Boolean forInformation);

}

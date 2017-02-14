package sapotero.rxtest.views.adapters.utils;

import sapotero.rxtest.retrofit.models.document.Performer;

public class PrimaryConsiderationPeople {

  private String uid;
  private String id;
  private String name;
  private String position;
  private String organization;
  private boolean copy = false;
  private boolean out  = false;
  private boolean responsible = false;

  public PrimaryConsiderationPeople(String id, String name, String position, String organization) {
    this.id = id;
    this.name = name;
    this.position = position;
    this.organization = organization;
  }

  public PrimaryConsiderationPeople(Performer u) {
    if (u != null) {
      this.id = u.getPerformerId();
      this.name = u.getPerformerText();
      this.position = String.valueOf( u.getNumber() );
      this.organization = u.getOrganizationText();
      if (u.getIsOriginal() != null) {
        this.copy = u.getIsOriginal();
      } else {
        this.copy = false;
      }
      this.out  = false;

      if (u.getIsResponsible() != null) {
        this.responsible = u.getIsResponsible();
      } else {
        this.responsible = false;
      }
    }
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

  public boolean isCopy() {
    return copy;
  }

  public void setCopy(boolean copy) {
    this.copy = copy;
  }

  public boolean isOut() {
    return out;
  }

  public void setOut(boolean out) {
    this.out = out;
  }

  public boolean isResponsible() {
    return responsible;
  }

  public void setResponsible(boolean responsible) {
    this.responsible = responsible;
  }
}

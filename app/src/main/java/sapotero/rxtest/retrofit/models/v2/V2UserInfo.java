package sapotero.rxtest.retrofit.models.v2;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class V2UserInfo {

  @SerializedName("id")
  @Expose
  private String id;
  @SerializedName("is_organization")
  @Expose
  private Boolean isOrganization;
  @SerializedName("is_group")
  @Expose
  private Boolean isGroup;
  @SerializedName("name")
  @Expose
  private String name;
  @SerializedName("organization")
  @Expose
  private String organization;
  @SerializedName("position")
  @Expose
  private String position;
  @SerializedName("last_name")
  @Expose
  private String lastName;
  @SerializedName("first_name")
  @Expose
  private String firstName;
  @SerializedName("middle_name")
  @Expose
  private String middleName;
  @SerializedName("gender")
  @Expose
  private String gender;
  @SerializedName("image")
  @Expose
  private Object image;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public V2UserInfo withId(String id) {
    this.id = id;
    return this;
  }

  public Boolean getIsOrganization() {
    return isOrganization;
  }

  public void setIsOrganization(Boolean isOrganization) {
    this.isOrganization = isOrganization;
  }

  public V2UserInfo withIsOrganization(Boolean isOrganization) {
    this.isOrganization = isOrganization;
    return this;
  }

  public Boolean getIsGroup() {
    return isGroup;
  }

  public void setIsGroup(Boolean isGroup) {
    this.isGroup = isGroup;
  }

  public V2UserInfo withIsGroup(Boolean isGroup) {
    this.isGroup = isGroup;
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public V2UserInfo withName(String name) {
    this.name = name;
    return this;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public V2UserInfo withOrganization(String organization) {
    this.organization = organization;
    return this;
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public V2UserInfo withPosition(String position) {
    this.position = position;
    return this;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public V2UserInfo withLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public V2UserInfo withFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public String getMiddleName() {
    return middleName;
  }

  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  public V2UserInfo withMiddleName(String middleName) {
    this.middleName = middleName;
    return this;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public V2UserInfo withGender(String gender) {
    this.gender = gender;
    return this;
  }

  public Object getImage() {
    return image;
  }

  public void setImage(Object image) {
    this.image = image;
  }

  public V2UserInfo withImage(Object image) {
    this.image = image;
    return this;
  }

}
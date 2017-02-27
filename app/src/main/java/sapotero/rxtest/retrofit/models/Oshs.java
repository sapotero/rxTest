package sapotero.rxtest.retrofit.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Oshs implements Serializable {

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

  @SerializedName("assistant_id")
  @Expose
  private String assistantId;

  @SerializedName("image")
  @Expose
  private String image;

  public String getAssistantId() {
    return assistantId;
  }

  public void setAssistantId(String assistantId) {
    this.assistantId = assistantId;
  }

  /**
   *
   * @return
   * The id
   */
  public String getId() {
    return id;
  }

  /**
   *
   * @param id
   * The id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   *
   * @return
   * The isOrganization
   */
  public Boolean getIsOrganization() {
    return isOrganization;
  }

  /**
   *
   * @param isOrganization
   * The is_organization
   */
  public void setIsOrganization(Boolean isOrganization) {
    this.isOrganization = isOrganization;
  }

  /**
   *
   * @return
   * The isGroup
   */
  public Boolean getIsGroup() {
    return isGroup;
  }

  /**
   *
   * @param isGroup
   * The is_group
   */
  public void setIsGroup(Boolean isGroup) {
    this.isGroup = isGroup;
  }

  /**
   *
   * @return
   * The name
   */
  public String getName() {
    return name;
  }

  /**
   *
   * @param name
   * The name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   *
   * @return
   * The organization
   */
  public String getOrganization() {
    return organization;
  }

  /**
   *
   * @param organization
   * The organization
   */
  public void setOrganization(String organization) {
    this.organization = organization;
  }

  /**
   *
   * @return
   * The position
   */
  public String getPosition() {
    return position;
  }

  /**
   *
   * @param position
   * The position
   */
  public void setPosition(String position) {
    this.position = position;
  }

  /**
   *
   * @return
   * The lastName
   */
  public String getLastName() {
    return lastName;
  }

  /**
   *
   * @param lastName
   * The last_name
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   *
   * @return
   * The firstName
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   *
   * @param firstName
   * The first_name
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   *
   * @return
   * The middleName
   */
  public String getMiddleName() {
    return middleName;
  }

  /**
   *
   * @param middleName
   * The middle_name
   */
  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  /**
   *
   * @return
   * The gender
   */
  public String getGender() {
    return gender;
  }

  /**
   *
   * @param gender
   * The gender
   */
  public void setGender(String gender) {
    this.gender = gender;
  }

  /**
   *
   * @return
   * The image
   */
  public String getImage() {
    return image;
  }

  /**
   *
   * @param image
   * The image
   */
  public void setImage(String image) {
    this.image = image;
  }

}
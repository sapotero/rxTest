
package sapotero.rxtest.retrofit.models.document;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Performer implements IPerformer {

    @SerializedName("_id")
    @Expose
    private String id;

    @SerializedName("number")
    @Expose
    private Integer number;
    @SerializedName("performer_id")
    @Expose
    private String performerId;
    @SerializedName("performer_type")
    @Expose
    private String performerType;
    @SerializedName("performer_text")
    @Expose
    private String performerText;
    @SerializedName("performer_gender")
    @Expose
    private String performerGender;
    @SerializedName("organization_text")
    @Expose
    private String organizationText;
    @SerializedName("is_original")
    @Expose
    private Boolean isOriginal;
    @SerializedName("is_responsible")
    @Expose
    private Boolean isResponsible;

    @SerializedName("is_group")
    @Expose
    private Boolean isGroup = false;

    @SerializedName("is_organization")
    @Expose
    private Boolean isOrganization = false;

    @SerializedName("is_organisation")
    @Expose
    private Boolean isOrganisation = false;

    @SerializedName("for_information")
    @Expose
    private Boolean forInformation;

    public Boolean getForInformation() {
        return forInformation;
    }

    public void setForInformation(Boolean forInformation) {
        this.forInformation = forInformation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getOriginal() {
        return isOriginal;
    }

    public void setOriginal(Boolean original) {
        isOriginal = original;
    }

    public Boolean getResponsible() {
        return isResponsible;
    }

    public void setResponsible(Boolean responsible) {
        isResponsible = responsible;
    }

    public Boolean getGroup() {
        return isGroup;
    }

    public void setGroup(Boolean group) {
        isGroup = group;
    }

    public Boolean getOrganization() {
        if ( isPerformerTypeOrganization() ) {
            return true;
        } else {
            return isOrganization;
        }
    }

    private boolean isPerformerTypeOrganization() {
        if (performerType != null && performerType.endsWith("organisation")) {
            return true;
        } else {
            return false;
        }
    }

    public void setOrganization(Boolean organization) {
        isOrganization = organization;
        isOrganisation = organization;
    }

    /**
     * 
     * @return
     *     The number
     */
    public Integer getNumber() {
        return number;
    }

    /**
     * 
     * @param number
     *     The number
     */
    public void setNumber(Integer number) {
        this.number = number;
    }

    /**
     * 
     * @return
     *     The performerId
     */
    public String getPerformerId() {
        return performerId;
    }

    /**
     * 
     * @param performerId
     *     The performer_id
     */
    public void setPerformerId(String performerId) {
        this.performerId = performerId;
    }

    /**
     * 
     * @return
     *     The performerType
     */
    public String getPerformerType() {
        return performerType;
    }

    /**
     * 
     * @param performerType
     *     The performer_type
     */
    public void setPerformerType(String performerType) {
        this.performerType = performerType;
    }

    /**
     * 
     * @return
     *     The performerText
     */
    public String getPerformerText() {
        if ( isPerformerTypeOrganization() ) {
            return organizationText;
        } else {
            return performerText;
        }
    }

    /**
     * 
     * @param performerText
     *     The performer_text
     */
    public void setPerformerText(String performerText) {
        this.performerText = performerText;
    }

    /**
     * 
     * @return
     *     The organizationText
     */
    public String getOrganizationText() {
        return organizationText;
    }

    /**
     * 
     * @param organizationText
     *     The organization_text
     */
    public void setOrganizationText(String organizationText) {
        this.organizationText = organizationText;
    }

    /**
     * 
     * @return
     *     The isOriginal
     */
    public Boolean getIsOriginal() {
        return isOriginal;
    }

    /**
     * 
     * @param isOriginal
     *     The is_original
     */
    public void setIsOriginal(Boolean isOriginal) {
        this.isOriginal = isOriginal;
    }

    /**
     * 
     * @return
     *     The isResponsible
     */
    public Boolean getIsResponsible() {
        return isResponsible;
    }

    /**
     * 
     * @param isResponsible
     *     The is_responsible
     */
    public void setIsResponsible(Boolean isResponsible) {
        this.isResponsible = isResponsible;
    }

    public String getPerformerGender() {
        return performerGender;
    }

    public void setPerformerGender(String performerGender) {
        this.performerGender = performerGender;
    }

    @Override
    public String getIPerformerUid() {
        return getId();
    }

    @Override
    public void setIPerformerUid(String uid) {
        setId(uid);
    }

    @Override
    public Integer getIPerformerNumber() {
        return getNumber();
    }

    @Override
    public void setIPerformerNumber(Integer number) {
        setNumber(number);
    }

    @Override
    public String getIPerformerId() {
        return getPerformerId();
    }

    @Override
    public void setIPerformerId(String id) {
        setPerformerId(id);
    }

    @Override
    public String getIPerformerType() {
        return getPerformerType();
    }

    @Override
    public void setIPerformerType(String type) {
        setPerformerType(type);
    }

    @Override
    public String getIPerformerName() {
        return getPerformerText();
    }

    @Override
    public void setIPerformerName(String name) {
        setPerformerText(name);
    }

    @Override
    public String getIPerformerGender() {
        return getPerformerGender();
    }

    @Override
    public void setIPerformerGender(String gender) {
        setPerformerGender(gender);
    }

    @Override
    public String getIPerformerOrganizationName() {
        return getOrganizationText();
    }

    @Override
    public void setIPerformerOrganizationName(String organizationName) {
        setOrganizationText(organizationName);
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
        return getIsOriginal();
    }

    @Override
    public void setIsIPerformerOriginal(Boolean isOriginal) {
        setIsOriginal(isOriginal);
    }

    @Override
    public Boolean isIPerformerResponsible() {
        return getIsResponsible();
    }

    @Override
    public void setIsIPerformerResponsible(Boolean isResponsible) {
        setIsResponsible(isResponsible);
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
        return getOrganization();
    }

    @Override
    public void setIsIPerformerOrganization(Boolean isOrganization) {
        setOrganization(isOrganization);
    }

    @Override
    public String getIImage() {
        return null;
    }

    @Override
    public void setIImage(String image) {

    }

  @Override
  public Boolean getIForInformation() {
    return getForInformation();
  }

  @Override
  public void setIForInformation(Boolean forInformation) {
    setForInformation(forInformation != null ? forInformation : false);
  }
}

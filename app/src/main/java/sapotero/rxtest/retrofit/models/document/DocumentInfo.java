
package sapotero.rxtest.retrofit.models.document;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class DocumentInfo {

    @SerializedName("uid")
    @Expose
    private String uid;
    @SerializedName("md5")
    @Expose
    private String md5;
    @SerializedName("sort_key")
    @Expose
    private Integer sortKey;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("registration_number")
    @Expose
    private String registrationNumber;
    @SerializedName("registration_date")
    @Expose
    private String registrationDate;
    @SerializedName("urgency")
    @Expose
    private String urgency;
    @SerializedName("short_description")
    @Expose
    private String shortDescription;
    @SerializedName("comment")
    @Expose
    private String comment;
    @SerializedName("external_document_number")
    @Expose
    private String externalDocumentNumber;
    @SerializedName("receipt_date")
    @Expose
    private String receiptDate;
    @SerializedName("signer")
    @Expose
    private Signer signer;
    @SerializedName("viewed")
    @Expose
    private Boolean viewed;
    @SerializedName("exemplars")
    @Expose
    private List<Exemplar> exemplars = new ArrayList<Exemplar>();
    @SerializedName("decisions")
    @Expose
    private List<Decision> decisions = new ArrayList<Decision>();
    @SerializedName("info_card")
    @Expose
    private String infoCard;
    @SerializedName("links")
    @Expose
    private List<String> links = new ArrayList<String>();
    @SerializedName("images")
    @Expose
    private List<Image> images = new ArrayList<Image>();
    @SerializedName("control_labels")
    @Expose
    private List<ControlLabel> controlLabels = new ArrayList<ControlLabel>();
    @SerializedName("actions")
    @Expose
    private List<Object> actions = new ArrayList<Object>();

    /**
     * 
     * @return
     *     The uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * 
     * @param uid
     *     The uid
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * 
     * @return
     *     The md5
     */
    public String getMd5() {
        return md5;
    }

    /**
     * 
     * @param md5
     *     The md5
     */
    public void setMd5(String md5) {
        this.md5 = md5;
    }

    /**
     * 
     * @return
     *     The sortKey
     */
    public Integer getSortKey() {
        return sortKey;
    }

    /**
     * 
     * @param sortKey
     *     The sort_key
     */
    public void setSortKey(Integer sortKey) {
        this.sortKey = sortKey;
    }

    /**
     * 
     * @return
     *     The position
     */
    public String getTitle() {
        return title;
    }

    /**
     * 
     * @param title
     *     The position
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 
     * @return
     *     The registrationNumber
     */
    public String getRegistrationNumber() {
        return registrationNumber;
    }

    /**
     * 
     * @param registrationNumber
     *     The registration_number
     */
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    /**
     * 
     * @return
     *     The registrationDate
     */
    public String getRegistrationDate() {
        return registrationDate;
    }

    /**
     * 
     * @param registrationDate
     *     The registration_date
     */
    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    /**
     * 
     * @return
     *     The urgency
     */
    public String getUrgency() {
        return urgency;
    }

    /**
     * 
     * @param urgency
     *     The urgency
     */
    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    /**
     * 
     * @return
     *     The shortDescription
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * 
     * @param shortDescription
     *     The short_description
     */
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * 
     * @return
     *     The comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * 
     * @param comment
     *     The comment
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * 
     * @return
     *     The externalDocumentNumber
     */
    public String getExternalDocumentNumber() {
        return externalDocumentNumber;
    }

    /**
     * 
     * @param externalDocumentNumber
     *     The external_document_number
     */
    public void setExternalDocumentNumber(String externalDocumentNumber) {
        this.externalDocumentNumber = externalDocumentNumber;
    }

    /**
     * 
     * @return
     *     The receiptDate
     */
    public String getReceiptDate() {
        return receiptDate;
    }

    /**
     * 
     * @param receiptDate
     *     The receipt_date
     */
    public void setReceiptDate(String receiptDate) {
        this.receiptDate = receiptDate;
    }

    /**
     * 
     * @return
     *     The signer
     */
    public Signer getSigner() {
        return signer;
    }

    /**
     * 
     * @param signer
     *     The signer
     */
    public void setSigner(Signer signer) {
        this.signer = signer;
    }

    /**
     * 
     * @return
     *     The viewed
     */
    public Boolean getViewed() {
        return viewed;
    }

    /**
     * 
     * @param viewed
     *     The viewed
     */
    public void setViewed(Boolean viewed) {
        this.viewed = viewed;
    }

    /**
     * 
     * @return
     *     The exemplars
     */
    public List<Exemplar> getExemplars() {
        return exemplars;
    }

    /**
     * 
     * @param exemplars
     *     The exemplars
     */
    public void setExemplars(List<Exemplar> exemplars) {
        this.exemplars = exemplars;
    }

    /**
     * 
     * @return
     *     The decisions
     */
    public List<Decision> getDecisions() {
        return decisions;
    }

    /**
     * 
     * @param decisions
     *     The decisions
     */
    public void setDecisions(List<Decision> decisions) {
        this.decisions = decisions;
    }

    /**
     * 
     * @return
     *     The infoCard
     */
    public String getInfoCard() {
        return infoCard;
    }

    /**
     * 
     * @param infoCard
     *     The info_card
     */
    public void setInfoCard(String infoCard) {
        this.infoCard = infoCard;
    }

    /**
     * 
     * @return
     *     The links
     */
    public List<String> getLinks() {
        return links;
    }

    /**
     * 
     * @param links
     *     The links
     */
    public void setLinks(List<String> links) {
        this.links = links;
    }

    /**
     * 
     * @return
     *     The images
     */
    public List<Image> getImages() {
        return images;
    }

    /**
     * 
     * @param images
     *     The images
     */
    public void setImages(List<Image> images) {
        this.images = images;
    }

    /**
     * 
     * @return
     *     The controlLabels
     */
    public List<ControlLabel> getControlLabels() {
        return controlLabels;
    }

    /**
     * 
     * @param controlLabels
     *     The control_labels
     */
    public void setControlLabels(List<ControlLabel> controlLabels) {
        this.controlLabels = controlLabels;
    }

    /**
     * 
     * @return
     *     The actions
     */
    public List<Object> getActions() {
        return actions;
    }

    /**
     * 
     * @param actions
     *     The actions
     */
    public void setActions(List<Object> actions) {
        this.actions = actions;
    }

}

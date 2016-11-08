
package sapotero.rxtest.retrofit.models.document;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Decision {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("letterhead")
    @Expose
    private String letterhead;
    @SerializedName("approved")
    @Expose
    private Boolean approved;
    @SerializedName("signer")
    @Expose
    private String signer;
    @SerializedName("signer_id")
    @Expose
    private String signerId;
    @SerializedName("signer_blank_text")
    @Expose
    private String signerBlankText;
    @SerializedName("signer_position_s")
    @Expose
    private String signerPositionS;
    @SerializedName("signer_is_manager")
    @Expose
    private Boolean signerIsManager;
    @SerializedName("sign_base64")
    @Expose
    private String signBase64;
    @SerializedName("assistant_id")
    @Expose
    private String assistantId;
    @SerializedName("comment")
    @Expose
    private String comment;
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("urgency_text")
    @Expose
    private String urgencyText;
    @SerializedName("show_position")
    @Expose
    private Boolean showPosition;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("red")
    @Expose
    private Boolean red;
    @SerializedName("blocks")
    @Expose
    private List<Block> blocks = new ArrayList<Block>();

    /**
     * 
     * @return
     *     The id
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @param id
     *     The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The letterhead
     */
    public String getLetterhead() {
        return letterhead;
    }

    /**
     * 
     * @param letterhead
     *     The letterhead
     */
    public void setLetterhead(String letterhead) {
        this.letterhead = letterhead;
    }

    /**
     * 
     * @return
     *     The approved
     */
    public Boolean getApproved() {
        return approved;
    }

    /**
     * 
     * @param approved
     *     The approved
     */
    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    /**
     * 
     * @return
     *     The signer
     */
    public String getSigner() {
        return signer;
    }

    /**
     * 
     * @param signer
     *     The signer
     */
    public void setSigner(String signer) {
        this.signer = signer;
    }

    /**
     * 
     * @return
     *     The signerId
     */
    public String getSignerId() {
        return signerId;
    }

    /**
     * 
     * @param signerId
     *     The signer_id
     */
    public void setSignerId(String signerId) {
        this.signerId = signerId;
    }

    /**
     * 
     * @return
     *     The signerBlankText
     */
    public String getSignerBlankText() {
        return signerBlankText;
    }

    /**
     * 
     * @param signerBlankText
     *     The signer_blank_text
     */
    public void setSignerBlankText(String signerBlankText) {
        this.signerBlankText = signerBlankText;
    }

    /**
     * 
     * @return
     *     The signerPositionS
     */
    public String getSignerPositionS() {
        return signerPositionS;
    }

    /**
     * 
     * @param signerPositionS
     *     The signer_position_s
     */
    public void setSignerPositionS(String signerPositionS) {
        this.signerPositionS = signerPositionS;
    }

    /**
     * 
     * @return
     *     The signerIsManager
     */
    public Boolean getSignerIsManager() {
        return signerIsManager;
    }

    /**
     * 
     * @param signerIsManager
     *     The signer_is_manager
     */
    public void setSignerIsManager(Boolean signerIsManager) {
        this.signerIsManager = signerIsManager;
    }

    /**
     * 
     * @return
     *     The signBase64
     */
    public String getSignBase64() {
        return signBase64;
    }

    /**
     * 
     * @param signBase64
     *     The sign_base64
     */
    public void setSignBase64(String signBase64) {
        this.signBase64 = signBase64;
    }

    /**
     * 
     * @return
     *     The assistantId
     */
    public String getAssistantId() {
        return assistantId;
    }

    /**
     * 
     * @param assistantId
     *     The assistant_id
     */
    public void setAssistantId(String assistantId) {
        this.assistantId = assistantId;
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
     *     The date
     */
    public String getDate() {
        return date;
    }

    /**
     * 
     * @param date
     *     The date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * 
     * @return
     *     The urgencyText
     */
    public String getUrgencyText() {
        return urgencyText;
    }

    /**
     * 
     * @param urgencyText
     *     The urgency_text
     */
    public void setUrgencyText(String urgencyText) {
        this.urgencyText = urgencyText;
    }

    /**
     * 
     * @return
     *     The showPosition
     */
    public Boolean getShowPosition() {
        return showPosition;
    }

    /**
     * 
     * @param showPosition
     *     The show_position
     */
    public void setShowPosition(Boolean showPosition) {
        this.showPosition = showPosition;
    }

    /**
     * 
     * @return
     *     The status
     */
    public String getStatus() {
        return status;
    }

    /**
     * 
     * @param status
     *     The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 
     * @return
     *     The red
     */
    public Boolean getRed() {
        return red;
    }

    /**
     * 
     * @param red
     *     The red
     */
    public void setRed(Boolean red) {
        this.red = red;
    }

    /**
     * 
     * @return
     *     The blocks
     */
    public List<Block> getBlocks() {
        return blocks;
    }

    /**
     * 
     * @param blocks
     *     The blocks
     */
    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

}

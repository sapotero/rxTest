
package sapotero.rxtest.retrofit.models.document;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ControlLabel {

    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("official_id")
    @Expose
    private String officialId;
    @SerializedName("official_name")
    @Expose
    private String officialName;
    @SerializedName("skipped_official_id")
    @Expose
    private String skippedOfficialId;
    @SerializedName("skipped_official_name")
    @Expose
    private String skippedOfficialName;
    @SerializedName("state")
    @Expose
    private String state;

    /**
     * 
     * @return
     *     The createdAt
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * 
     * @param createdAt
     *     The created_at
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 
     * @return
     *     The officialId
     */
    public String getOfficialId() {
        return officialId;
    }

    /**
     * 
     * @param officialId
     *     The official_id
     */
    public void setOfficialId(String officialId) {
        this.officialId = officialId;
    }

    /**
     * 
     * @return
     *     The officialName
     */
    public String getOfficialName() {
        return officialName;
    }

    /**
     * 
     * @param officialName
     *     The official_name
     */
    public void setOfficialName(String officialName) {
        this.officialName = officialName;
    }

    /**
     * 
     * @return
     *     The skippedOfficialId
     */
    public String getSkippedOfficialId() {
        return skippedOfficialId;
    }

    /**
     * 
     * @param skippedOfficialId
     *     The skipped_official_id
     */
    public void setSkippedOfficialId(String skippedOfficialId) {
        this.skippedOfficialId = skippedOfficialId;
    }

    /**
     * 
     * @return
     *     The skippedOfficialName
     */
    public String getSkippedOfficialName() {
        return skippedOfficialName;
    }

    /**
     * 
     * @param skippedOfficialName
     *     The skipped_official_name
     */
    public void setSkippedOfficialName(String skippedOfficialName) {
        this.skippedOfficialName = skippedOfficialName;
    }

    /**
     * 
     * @return
     *     The state
     */
    public String getState() {
        return state;
    }

    /**
     * 
     * @param state
     *     The state
     */
    public void setState(String state) {
        this.state = state;
    }

}

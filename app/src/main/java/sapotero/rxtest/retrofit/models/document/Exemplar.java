
package sapotero.rxtest.retrofit.models.document;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Exemplar {

    @SerializedName("number")
    @Expose
    private Integer number;
    @SerializedName("is_original")
    @Expose
    private Boolean isOriginal;
    @SerializedName("status_code")
    @Expose
    private String statusCode;
    @SerializedName("addressed_to_id")
    @Expose
    private String addressedToId;
    @SerializedName("addressed_to_name")
    @Expose
    private String addressedToName;
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("statuses")
    @Expose
    private List<Status> statuses = new ArrayList<Status>();

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
     *     The statusCode
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * 
     * @param statusCode
     *     The status_code
     */
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * 
     * @return
     *     The addressedToId
     */
    public String getAddressedToId() {
        return addressedToId;
    }

    /**
     * 
     * @param addressedToId
     *     The addressed_to_id
     */
    public void setAddressedToId(String addressedToId) {
        this.addressedToId = addressedToId;
    }

    /**
     * 
     * @return
     *     The addressedToName
     */
    public String getAddressedToName() {
        return addressedToName;
    }

    /**
     * 
     * @param addressedToName
     *     The addressed_to_name
     */
    public void setAddressedToName(String addressedToName) {
        this.addressedToName = addressedToName;
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
     *     The statuses
     */
    public List<Status> getStatuses() {
        return statuses;
    }

    /**
     * 
     * @param statuses
     *     The statuses
     */
    public void setStatuses(List<Status> statuses) {
        this.statuses = statuses;
    }

}

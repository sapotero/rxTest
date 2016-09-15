
package sapotero.rxtest.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Meta {

    @SerializedName("total")
    @Expose
    private String total;
    @SerializedName("skip_count")
    @Expose
    private Object skipCount;
    @SerializedName("limit")
    @Expose
    private Object limit;

    /**
     * 
     * @return
     *     The total
     */
    public String getTotal() {
        return total;
    }

    /**
     * 
     * @param total
     *     The total
     */
    public void setTotal(String total) {
        this.total = total;
    }

    /**
     * 
     * @return
     *     The skipCount
     */
    public Object getSkipCount() {
        return skipCount;
    }

    /**
     * 
     * @param skipCount
     *     The skip_count
     */
    public void setSkipCount(Object skipCount) {
        this.skipCount = skipCount;
    }

    /**
     * 
     * @return
     *     The limit
     */
    public Object getLimit() {
        return limit;
    }

    /**
     * 
     * @param limit
     *     The limit
     */
    public void setLimit(Object limit) {
        this.limit = limit;
    }

}

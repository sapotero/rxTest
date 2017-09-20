
package sapotero.rxtest.retrofit.models.documents;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Meta implements Serializable {

    @SerializedName("total")
    @Expose
    private String total;

    @SerializedName("skip_count")
    @Expose
    private Object skipCount;

    @SerializedName("limit")
    @Expose
    private Object limit;

    @SerializedName("scroll_id")
    @Expose
    private String scrollId;

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

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }
}

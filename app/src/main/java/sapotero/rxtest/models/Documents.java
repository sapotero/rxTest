
package sapotero.rxtest.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Documents {

    @SerializedName("documents")
    @Expose
    private List<Document> documents = new ArrayList<Document>();
    @SerializedName("meta")
    @Expose
    private Meta meta;

    /**
     * 
     * @return
     *     The documents
     */
    public List<Document> getDocuments() {
        return documents;
    }

    /**
     * 
     * @param documents
     *     The documents
     */
    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    /**
     * 
     * @return
     *     The meta
     */
    public Meta getMeta() {
        return meta;
    }

    /**
     * 
     * @param meta
     *     The meta
     */
    public void setMeta(Meta meta) {
        this.meta = meta;
    }

}

package sapotero.rxtest.utils.memory.models;


import java.util.HashMap;
import java.util.List;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.utils.Processor;

public final class NotifyMessageModel {
  private List<String> uidDocsLIst ;
  private HashMap<String, Document> documentsMap;
  private String filter;
  private Processor.Source source;

  public NotifyMessageModel(List<String> uidDocsLIst, HashMap<String, Document> documentsMap, String filter, Processor.Source source) {
    this.uidDocsLIst = uidDocsLIst;
    this.documentsMap = documentsMap;
    this.filter = filter;
    this.source = source;
  }


  public Processor.Source getSource() {
    return source;
  }

  public List<String> getUidDocsLIst() {
    return uidDocsLIst;
  }

  public HashMap<String, Document> getDocumentsMap() {
    return documentsMap;
  }
  public String getFilter() {
    return filter;
  }
}

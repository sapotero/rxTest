package sapotero.rxtest.utils.memory.models;


import java.util.HashMap;
import java.util.List;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.utils.Processor;

public final class NotifyMessageModel {

  private List<String> uidDocsLIst ;
//  private HashMap<String, Document> documentsMap;
  private String filter;
  private String index;
  private boolean isFirstRunApp;
  private Processor.Source source;

  private Document document;

//  public NotifyMessageModel(List<String> uidDocsLIst, HashMap<String, Document> documentsMap, String filter, String index, boolean isFirstRunApp, Processor.Source source) {
//    this.uidDocsLIst = uidDocsLIst;
//    this.documentsMap = documentsMap;
//    this.filter = filter;
//    this.isFirstRunApp = isFirstRunApp;
//    this.source = source;
//    this.index = index;
//  }

  public NotifyMessageModel(Document document, String filter, String index, boolean isFirstRunApp, Processor.Source source) {
    this.document = document;
    this.filter = filter;
    this.index = index;
    this.isFirstRunApp = isFirstRunApp;
    this.source = source;
  }

  public Document getDocument() {
    return document;
  }

  public String getIndex() {
    return index;
  }

  public boolean isFirstRunApp() {
    return isFirstRunApp;
  }

  public Processor.Source getSource() {
    return source;
  }

  public List<String> getUidDocsLIst() {
    return uidDocsLIst;
  }

//  public HashMap<String, Document> getDocumentsMap() {
//    return documentsMap;
//  }

  public String getFilter() {
    return filter;
  }
}

package sapotero.rxtest.utils.memory.models;

import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.utils.Processor;

public final class NotifyMessageModel {

  private String filter;
  private String index;
  private boolean isFirstRunApp;
  private Processor.Source source;
  private Document document;

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

  public String getFilter() {
    return filter;
  }
}

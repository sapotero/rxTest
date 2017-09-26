package sapotero.rxtest.utils.memory.models;

import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.fields.DocumentType;
import sapotero.rxtest.utils.memory.fields.InMemoryState;
import sapotero.rxtest.utils.memory.utils.Processor;

public final class NotifyMessageModel {

  private String filter;
  private String index;
  private boolean isFirstRunApp;
  private Processor.Source source;
  private Document document;
  private DocumentType documentType;

  public NotifyMessageModel(Document newAddedDocument, String filter, String index, boolean isFirstRunApp, Processor.Source source, DocumentType documentType) {
    this.document = newAddedDocument;
    this.filter = filter;
    this.index = index;
    this.isFirstRunApp = isFirstRunApp;
    this.source = source;
    this.documentType = documentType;
  }

  public DocumentType getDocumentType() {
    return documentType;
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

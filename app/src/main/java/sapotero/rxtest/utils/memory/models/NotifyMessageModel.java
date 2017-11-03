package sapotero.rxtest.utils.memory.models;

import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.fields.DocumentType;
import sapotero.rxtest.utils.memory.utils.Processor;

public final class NotifyMessageModel {

  private String filter;
  private String index;
  private Processor.Source source;
  private Document document;
  private DocumentType documentType;

  public NotifyMessageModel(Document newAddedDocument, String filter, String index, Processor.Source source, DocumentType documentType ) {
    this.document = newAddedDocument;
    this.filter = filter;
    this.index = index;
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

  public Processor.Source getSource() {
    return source;
  }

  public String getFilter() {
    return filter;
  }
}

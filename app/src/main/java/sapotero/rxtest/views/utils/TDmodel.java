package sapotero.rxtest.views.utils;

import java.util.List;

import sapotero.rxtest.retrofit.models.documents.Document;

public class TDmodel {
  private final String type;
  private final List<Document> documents;

  public TDmodel(String type, List<Document> documents) {
    this.type = type;
    this.documents = documents;
  }

  public String getType() {
    return type;
  }

  public List<Document> getDocuments() {
    return documents;
  }
}

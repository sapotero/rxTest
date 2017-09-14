package sapotero.rxtest.views.adapters.utils;

import java.util.List;

import sapotero.rxtest.db.requery.utils.JournalStatus;
import sapotero.rxtest.retrofit.models.documents.Document;

public class TDmodel {
  private final String type;
  private final List<Document> documents;

  public TDmodel(JournalStatus type, List<Document> documents) {
    this.type = type.getName();
    this.documents = documents;
  }

  public String getType() {
    return type;
  }

  public List<Document> getDocuments() {
    return documents;
  }
}

package sapotero.rxtest.views.managers.db.utils;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.retrofit.models.documents.Document;

public class DocumentManagerEntity {

  public void set(Document document) {

  }

  public void set(String document) {

  }

  public void set(RDocumentEntity document) {

  }

  enum Type {
    MODEL,
    JSON,
    DB;
  }

  private Type type;

}

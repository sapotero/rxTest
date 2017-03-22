package sapotero.rxtest.managers.db;

import android.content.Context;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.managers.db.managers.DBDocumentManager;
import sapotero.rxtest.managers.db.utils.DocumentManagerEntity;

public class DocumentManager {

  private DocumentManagerEntity entity;
  private DBDocumentManager loader;
  private Context context;

  private RDocumentEntity document;

  public DocumentManager(Context context) {
    this.context = context;
    this.loader = new DBDocumentManager(context);
    this.entity = new DocumentManagerEntity();
  }

  public DocumentInfo toJson(){
    DocumentInfo doc = null;
    if (document != null){
      doc = loader.toModel( document );
    }

    return doc;
  }


  public DocumentManager get(String uid) {
    document = loader.get(uid);
    return this;
  }

  public RDocumentEntity getDocument(String uid) {
    document = loader.get(uid);
    return document;
  }




  public void from(Document document){
    entity.set(document);
  }

  public void from(String json){
    entity.set(json);
  }

  public void from(RDocumentEntity document){
    entity.set(document);
  }


}
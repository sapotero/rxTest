package sapotero.rxtest.views.managers.db;

import android.content.Context;

import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.views.managers.db.factories.DocumentFactory;


public class DocumentManager {

  private DocumentFactory loader;
  private Context context;

  private RDocumentEntity document;
  private Document object;
  private String uid;

  private Type type;
  private DocumentManager manager;

  enum Type {JSON, DB;}


  public DocumentManager() {}

  private DocumentManager(Context context) {
    this.context = context;
    this.loader = new DocumentFactory(context);
    this.type = Type.DB;
  }

  public DocumentManager getInstance(Context context){
    if ( manager == null ){
      manager = new DocumentManager(context);
    }

    return manager;
  }

  public DocumentInfo toJson(){
    DocumentInfo doc = null;
    if (document != null){
      doc = loader.toObject( document );
    }

    return doc;
  }


  public DocumentManager get(String uid) {
    document = loader.fromDb(uid);
    return this;
  }

  public RDocumentEntity getDocument(String uid) {
    document = loader.fromDb(uid);
    return document;
  }

}
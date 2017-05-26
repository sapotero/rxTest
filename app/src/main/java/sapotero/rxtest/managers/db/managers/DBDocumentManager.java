package sapotero.rxtest.managers.db.managers;

import com.google.gson.Gson;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.query.Result;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.mapper.utils.Mappers;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import timber.log.Timber;

public class DBDocumentManager {
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject Mappers mappers;

  private final String TAG = this.getClass().getSimpleName();

  public DBDocumentManager() {

    EsdApplication.getDataComponent().inject(this);
  }

  public RDocumentEntity get(String uid) {

    Result<RDocumentEntity> doc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(uid))
      .get();

    return exist(uid) ? doc.first() : null;
  }

  private Boolean exist(String uid) {
    boolean result = false;

    Integer count = dataStore
      .count(RDocumentEntity.UID)
      .where(RDocumentEntity.UID.eq(uid))
      .get().value();

    if (count != 0) {
      result = true;
    }

    return result;
  }


  public DocumentInfo toModel(RDocumentEntity document) {

    Timber.e("%s", document.getSigner() );

    DocumentInfo doc = mappers.getDocumentMapper().toModel( document );

    Timber.w( "JSON: %s", new Gson().toJson(doc, DocumentInfo.class) );

    return doc;
  }

  public String toJSON(RDocumentEntity document) {
    return new Gson().toJson( toModel(document) );
  }
}

package sapotero.rxtest.managers.db.managers;

import com.google.gson.Gson;

import java.util.ArrayList;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.query.Result;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.mapper.BlockMapper;
import sapotero.rxtest.db.mapper.DecisionMapper;
import sapotero.rxtest.db.mapper.DocumentMapper;
import sapotero.rxtest.db.mapper.ExemplarMapper;
import sapotero.rxtest.db.mapper.ImageMapper;
import sapotero.rxtest.db.mapper.PerformerMapper;
import sapotero.rxtest.db.mapper.SignerMapper;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.db.requery.models.decisions.RBlock;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecision;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformer;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.db.requery.models.exemplars.RExemplar;
import sapotero.rxtest.db.requery.models.exemplars.RExemplarEntity;
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.document.Exemplar;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.retrofit.models.document.Signer;
import timber.log.Timber;

public class DBDocumentManager {
  @Inject SingleEntityStore<Persistable> dataStore;

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

    DocumentInfo doc = new DocumentMapper().toModel( document );

    Timber.w( "JSON: %s", new Gson().toJson(doc, DocumentInfo.class) );

    return doc;
  }

  public String toJSON(RDocumentEntity document) {
    return new Gson().toJson( toModel(document) );
  }
}

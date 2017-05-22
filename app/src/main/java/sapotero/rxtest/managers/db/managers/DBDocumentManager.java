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

    DocumentInfo doc = new DocumentInfo();


    doc.setUid(document.getUid());
    doc.setMd5(document.getMd5());
    doc.setTitle(document.getTitle());
    doc.setSortKey(document.getSortKey());
    doc.setRegistrationNumber(document.getRegistrationNumber());
    doc.setRegistrationDate(document.getRegistrationDate());
    doc.setUrgency(document.getUrgency());
    doc.setShortDescription(document.getShortDescription());
    doc.setComment(document.getComment());
    doc.setExternalDocumentNumber(document.getExternalDocumentNumber());
    doc.setReceiptDate(document.getReceiptDate());
    doc.setInfoCard( document.getInfoCard() );

    doc.setViewed(document.isViewed());

    RSignerEntity raw_signer = (RSignerEntity) document.getSigner();

    if (raw_signer != null){
      Signer signer = new SignerMapper().toModel(raw_signer);
      doc.setSigner(signer);
    }

    if (document.getDecisions().size() >= 1) {
      ArrayList<Decision> decisions_list = new ArrayList<Decision>();
      DecisionMapper decisionMapper = new DecisionMapper();

      for (RDecision rDecision : document.getDecisions()) {
        RDecisionEntity decisionEntity = (RDecisionEntity) rDecision;
        Decision raw_decision = decisionMapper.toModel(decisionEntity);
        decisions_list.add(raw_decision);
      }

      doc.setDecisions( decisions_list );
    }



    if (document.getExemplars().size() >= 1) {
      ArrayList<Exemplar> exemplar_list = new ArrayList<Exemplar>();
      ExemplarMapper exemplarMapper = new ExemplarMapper();

      for (RExemplar ex: document.getExemplars() ) {
        RExemplarEntity e = (RExemplarEntity) ex;
        Exemplar exemplar = exemplarMapper.toModel(e);
        exemplar_list.add(exemplar);
      }

      doc.setExemplars(exemplar_list);
    }

    if ( document.getImages().size() >= 1 ){
      ArrayList<Image> image_list = new ArrayList<Image>();
      ImageMapper imageMapper = new ImageMapper();

      for (RImage im: document.getImages() ) {
        RImageEntity i = (RImageEntity) im;
        Image image = imageMapper.toModel(i);
        image_list.add(image);
      }
      doc.setImages(image_list);
    }


    Timber.w( "JSON: %s", new Gson().toJson(doc, DocumentInfo.class) );

    return doc;
  }

  public String toJSON(RDocumentEntity document) {
    return new Gson().toJson( toModel(document) );
  }
}

package sapotero.rxtest.views.managers.db.factories;

import android.content.Context;

import com.google.gson.Gson;

import java.util.ArrayList;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.query.Result;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.application.EsdApplication;
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
import sapotero.rxtest.views.managers.db.builders.DBBuilder;
import sapotero.rxtest.views.managers.db.builders.ObjectBuilder;
import timber.log.Timber;

public class DocumentFactory {
  @Inject
  SingleEntityStore<Persistable> dataStore;

  private final String TAG = this.getClass().getSimpleName();

  private final boolean debug;

  private final DBBuilder fromDb;
  private final ObjectBuilder fromJson;

  public DocumentFactory(Context context) {
    EsdApplication.getComponent(context).inject(this);

    this.fromDb = new DBBuilder(context);
    this.fromJson = new ObjectBuilder(context);
    this.debug = true;
  }


  public RDocumentEntity fromDb(String uid) {

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

    if (debug) {
      Timber.tag(TAG).v("exist " + result);
    }

    return result;
  }


  public DocumentInfo toObject(RDocumentEntity document) {

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
      Signer signer = new Signer();
      signer.setId(raw_signer.getUid());
      signer.setName(raw_signer.getName());
      signer.setOrganisation(raw_signer.getOrganisation());
      signer.setType(raw_signer.getType());

      doc.setSigner(signer);
    }

    if (document.getDecisions().size() >= 1) {
      ArrayList<Decision> decisions_list = new ArrayList<Decision>();


      for (RDecision rDecision : document.getDecisions()) {
        Decision raw_decision = new Decision();

        RDecisionEntity decision = (RDecisionEntity) rDecision;

        raw_decision.setId(String.valueOf(decision.getUid()));
        raw_decision.setLetterhead(decision.getLetterhead());
        raw_decision.setSigner(decision.getSigner());
        raw_decision.setSignerId(decision.getSignerId());
        raw_decision.setAssistantId(decision.getAssistantId());
        raw_decision.setSignerBlankText(decision.getSignerBlankText());
        raw_decision.setComment(decision.getComment());
        raw_decision.setDate(decision.getDate());
        raw_decision.setApproved(decision.isApproved());
        raw_decision.setUrgencyText(decision.getUrgencyText());
        raw_decision.setSignerIsManager(decision.isSignerIsManager());
        raw_decision.setShowPosition(decision.isShowPosition());

        for (RBlock rBlock : decision.getBlocks()) {
          RBlockEntity block = (RBlockEntity) rBlock;
          Block raw_block = new Block();

          raw_block.setNumber(block.getNumber());
          raw_block.setText(block.getText());
          raw_block.setAppealText(block.getAppealText());
          raw_block.setTextBefore(block.isTextBefore());
          raw_block.setHidePerformers(block.isHidePerformers());
          raw_block.setToCopy(block.isToCopy());
          raw_block.setToFamiliarization(block.isToFamiliarization());

          for (RPerformer rPerformer : block.getPerformers()) {
            RPerformerEntity performer = (RPerformerEntity) rPerformer;
            Performer raw_performer = new Performer();

            raw_performer.setNumber(performer.getNumber());
            raw_performer.setPerformerId(performer.getPerformerId());
            raw_performer.setPerformerType(performer.getPerformerType());
            raw_performer.setPerformerText(performer.getPerformerText());
            raw_performer.setOrganizationText(performer.getOrganizationText());
            raw_performer.setIsOriginal(performer.isIsOriginal());
            raw_performer.setIsResponsible(performer.isIsResponsible());

            raw_block.getPerformers().add(raw_performer);
          }

          raw_decision.getBlocks().add(raw_block);
        }

        decisions_list.add(raw_decision);
      }

      doc.setDecisions( decisions_list );
    }



    if (document.getExemplars().size() >= 1) {
      ArrayList<Exemplar> exemplar_list = new ArrayList<Exemplar>();

      for (RExemplar ex: document.getExemplars() ) {
        RExemplarEntity e = (RExemplarEntity) ex;

        Exemplar exemplar = new Exemplar();
        exemplar.setNumber(Integer.valueOf(e.getNumber()));
        exemplar.setIsOriginal(e.isIsOriginal());
        exemplar.setStatusCode(e.getStatusCode());
        exemplar.setAddressedToId(e.getAddressedToId());
        exemplar.setAddressedToName(e.getAddressedToName());
        exemplar.setDate(e.getDate());
      }

      doc.setExemplars(exemplar_list);
    }

    if ( document.getImages().size() >= 1 ){
      ArrayList<Image> image_list = new ArrayList<Image>();

      for (RImage im: document.getImages() ) {

        RImageEntity i = (RImageEntity) im;
        Image image = new Image();
        image.setTitle(i.getTitle());
        image.setNumber(i.getNumber());
        image.setMd5(i.getMd5());
        image.setSize(i.getSize());
        image.setPath(i.getPath());
        image.setContentType(i.getContentType());
        image.setSigned(i.isSigned());
      }
      doc.setImages(image_list);
    }


    Timber.w( "JSON: %s", new Gson().toJson(doc, DocumentInfo.class) );

    return doc;
  }
}

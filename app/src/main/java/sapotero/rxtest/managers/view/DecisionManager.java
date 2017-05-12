package sapotero.rxtest.managers.view;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.Subscription;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.views.activities.DecisionConstructorActivity;
import sapotero.rxtest.managers.view.builders.DecisionBuilder;
import sapotero.rxtest.managers.view.builders.PreviewBuilder;
import sapotero.rxtest.managers.view.interfaces.DecisionInterface;
import sapotero.rxtest.views.adapters.models.UrgencyItem;
import timber.log.Timber;

public class DecisionManager implements DecisionInterface, DecisionBuilder.Callback, PreviewBuilder.Callback {

  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject Settings settings;

  private Decision decision;
  private final String md5;
  private final Activity activity;
  private final Context context;
  private final FragmentManager fragmentManger;

  private final PreviewBuilder preview_builder;
  private final DecisionBuilder decision_builder;

  private final String TAG = this.getClass().getSimpleName();
  private String urgency;
  private String date;
  private String signerId;
  private String signer;
  private CharSequence comment;
  private String assistantId;
  private String signerBlankText;

  public DecisionManager(Context context, FragmentManager supportFragmentManager, Decision decision) {

    EsdApplication.getDataComponent().inject(this);

    this.context = context;
    this.decision = decision;
    this.activity = (DecisionConstructorActivity) context;
    this.fragmentManger = supportFragmentManager;

    this.md5 = setDecisionHash( decision );

    this.preview_builder  = new PreviewBuilder(fragmentManger, decision);
    this.decision_builder = new DecisionBuilder(fragmentManger, decision);

    this.decision_builder.registerCallBack(this);

  }

  @NonNull
  private String setDecisionHash( Decision decision) {

    String data = new Gson().toJson(decision, Decision.class);
    StringBuilder sb = new StringBuilder();

    byte[] digest = data.getBytes();

    for (byte aDigest : digest) {
      if ((0xff & aDigest) < 0x10) {
        sb.append("0").append(Integer.toHexString((0xFF & aDigest)));
      } else {
        sb.append(Integer.toHexString(0xFF & aDigest));
      }
    }
  return sb.toString();
  }

  public DecisionBuilder getDecisionBuilder(){
    return this.decision_builder;
  }

  public void build(){
    preview_builder.build();
    decision_builder.build();
  }

  public Boolean isChanged(){
    return !Objects.equals( md5, setDecisionHash(decision) );
  }

  public void update(){
    preview_builder.setDecision(decision);
    preview_builder.update();
  }

  public void saveDecision(){

    Decision d = decision;

    Timber.tag(TAG).w("[%s] -  %s:%s ", settings.getUid(), d.getId(), d.getLetterhead() );

    RDocumentEntity document = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(settings.getUid()))
      .get().first();

    document.setChanged(true);

    RDecisionEntity dec = dataStore
      .select(RDecisionEntity.class)
      .where(RDecisionEntity.UID.eq(d.getId()))
      .get().first();

    dec.setUid( d.getId() );
    dec.setLetterhead(d.getLetterhead());
    dec.setApproved(d.getApproved());
    dec.setSigner(d.getSigner());
    dec.setSignerId(d.getSignerId());
    dec.setAssistantId(d.getAssistantId());
    dec.setSignerBlankText(d.getSignerBlankText());
    dec.setSignerIsManager(d.getSignerIsManager());
    dec.setComment(d.getComment());
    dec.setDate(d.getDate());
    dec.setUrgencyText(d.getUrgencyText());
    dec.setShowPosition(d.getShowPosition());
    dec.setChanged(true);

    if ( d.getBlocks() != null && d.getBlocks().size() >= 1 ){

      ArrayList<RBlockEntity> list = new ArrayList<>();

      for (Block b: d.getBlocks() ) {
        RBlockEntity block = new RBlockEntity();
        block.setNumber(b.getNumber());
        block.setText(b.getText());
        block.setAppealText(b.getAppealText());
        block.setTextBefore(b.getTextBefore());
        block.setHidePerformers(b.getHidePerformers());
        block.setToCopy(b.getToCopy());
        block.setToFamiliarization(b.getToFamiliarization());

        if ( b.getPerformers() != null && b.getPerformers().size() >= 1 ) {

          for (Performer p : b.getPerformers()) {
            RPerformerEntity performer = new RPerformerEntity();

            performer.setNumber(p.getNumber());
            performer.setPerformerId(p.getPerformerId());
            performer.setPerformerType(p.getPerformerType());
            performer.setPerformerText(p.getPerformerText());
            performer.setPerformerGender(p.getPerformerGender());
            performer.setOrganizationText(p.getOrganizationText());
            performer.setIsOriginal(p.getIsOriginal());
            performer.setIsResponsible(p.getIsResponsible());

            performer.setBlock(block);
            block.getPerformers().add(performer);
          }
        }


        block.setDecision(dec);
        list.add(block);
      }

      dec.getBlocks().clear();
      dec.getBlocks().addAll(list);
    }

      //FIX DECISION
    dec.setDocument(document);

//    RDecisionEntity temp = dataStore.addByOne(dec).toBlocking().value();
    Subscription temp = dataStore
      .update(dec)
      .subscribe( data ->{
        Timber.tag(TAG).w( "addByOne : %s", data.getBlocks().isEmpty() );
      }, error -> {
        Timber.e( String.valueOf(error.getStackTrace()) );
      });



  }

  /* DecisionInterface */
  @Override
  public Decision getDecision() {
    return decision;
  }

  @Override
  public void setDecision(Decision _decision_) {
    decision = _decision_;
    preview_builder.setDecision(_decision_);
  }

  /* DecisionBuilder.Callback */
  @Override
  public void onUpdateSuccess(Decision decision) {
    Timber.tag(TAG).i("onUpdateSuccess");
    setDecision(decision);

    update();
  }

  @Override
  public void onUpdateError(Throwable error) {

  }

  public void setUrgency(UrgencyItem urgency) {
    decision.setUrgencyText(urgency.getLabel());
    decision.setUrgency(urgency.getValue());
    update();
  }


  public void setDate(String date) {
    decision.setDate(date);
  }

  public void setSignerId(String signerId) {
    decision.setSignerId(signerId);
  }

  public void setSigner(String signer) {
    decision.setSigner(signer);
  }

  public boolean allSignersSet() {
    Boolean result = true;



    if ( decision.getBlocks().size() > 0 ){
      for (Block block : decision.getBlocks()){
        if (block.getPerformers().size() == 0){
          result = false;
          break;
        }
      }
    }

    if ( decision.getBlocks().size() <= 0 ){
      result = false;
    }

    return result  ;
  }

  public boolean hasBlocks() {
    Boolean result = true;

    if ( decision.getBlocks().size() == 0 ){
      result = false;
    }

    return result  ;
  }

  public boolean hasSigner() {
    Boolean result = true;

    if (decision.getSigner() == null || decision.getSignerId() == null){
      result = false;
    }

    return result  ;
  }

  public void setComment(CharSequence comment) {
    this.comment = comment;
    decision.setComment( comment.toString() );
    update();
  }

  public void setAssistantId(String assistantId) {
    this.assistantId = assistantId;
    decision.setAssistantId( assistantId );
  }

  public String getAssistantId() {
    return assistantId;
  }

  public void setSignerBlankText(String signerBlankText) {
    this.signerBlankText = signerBlankText;
    decision.setSignerBlankText(signerBlankText);
  }


  public void setUrgencyText(String urgencyText) {
    decision.setUrgencyText(urgencyText);
  }
  public void setUrgency(String urgencyText) {
    decision.setUrgency(urgencyText);
  }

  public void setPerformersFontSize(String size) {
    decision.setPerformersFontSize(size);
  }
}

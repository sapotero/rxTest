package sapotero.rxtest.managers.view;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.managers.view.builders.DecisionBuilder;
import sapotero.rxtest.managers.view.builders.PreviewBuilder;
import sapotero.rxtest.managers.view.interfaces.DecisionInterface;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.views.activities.DecisionConstructorActivity;
import sapotero.rxtest.views.adapters.models.UrgencyItem;
import timber.log.Timber;

public class DecisionManager implements DecisionInterface, DecisionBuilder.Callback {

  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject ISettings settings;

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

  private <T> List<T> nullGuard(List<T> list) {
    return list != null ? list : Collections.EMPTY_LIST;
  }

  @NonNull
  private String setDecisionHash( Decision decision) {

    String data = getDecisionJsonForHash( decision );
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

  // Return decision JSON with ignored fields
  // (this is needed for correct detection of changes in decision)
  private String getDecisionJsonForHash(Decision sourceDecision) {
    // Create new decision from source decision (needed to keep source decision unchanged)
    Gson gson = new Gson();
    String data = gson.toJson(sourceDecision, Decision.class);
    Decision decisionForHash = gson.fromJson(data, Decision.class);

    decisionForHash.setUrgency( null );

    if ( settings.isShowDecisionDateUpdate() ) {
      decisionForHash.setDate( null );
    }

    for ( Block block : nullGuard( decisionForHash.getBlocks() ) ) {
      if ( block.getAppealText() == null ) {
        block.setAppealText( "" );
      }

      block.setAskToReport( block.getAppealText().contains("дол") );
      block.setAskToAcquaint( block.getAppealText().contains("озн") );
      block.setNumber( null );
      block.setToCopy( null );
      block.setToFamiliarization( null );

      for ( Performer performer : nullGuard( block.getPerformers() ) ) {
        if ( performer.getIsOriginal() == null ) {
          performer.setIsOriginal( false );
        }
        if ( performer.getIsResponsible() == null ) {
          performer.setIsResponsible( false );
        }

        performer.setPerformerType( null );
        performer.setNumber( null );
      }
    }

    data = gson.toJson(decisionForHash, Decision.class);

    return data;
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

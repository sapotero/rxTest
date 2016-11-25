package sapotero.rxtest.views.managers;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.views.activities.DecisionConstructorActivity;
import sapotero.rxtest.views.managers.builders.DecisionBuilder;
import sapotero.rxtest.views.managers.builders.PreviewBuilder;
import sapotero.rxtest.views.managers.interfaces.DecisionInterface;
import timber.log.Timber;

public class DecisionManager implements DecisionInterface, DecisionBuilder.Callback {

  private Decision decision;
  private final String md5;
  private final Activity activity;
  private final Context context;
  private final FragmentManager fragmentManger;

  private final PreviewBuilder preview_builder;
  private final DecisionBuilder decision_builder;

  private final String TAG = this.getClass().getSimpleName();

  public DecisionManager(Context context, FragmentManager supportFragmentManager, Decision decision) {
    this.context = context;
    this.decision = decision;
    this.activity = (DecisionConstructorActivity) context;
    this.fragmentManger = supportFragmentManager;

    this.md5 = setDecisionHash( decision.toString() );

    this.preview_builder  = new PreviewBuilder(fragmentManger, decision);
    this.decision_builder = new DecisionBuilder(fragmentManger, decision);

    this.decision_builder.registerCallBack(this);

  }

  @NonNull
  private String setDecisionHash( String data) {
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


  public void build(){
    preview_builder.build();
    decision_builder.build();
  }

  public void update(){
    preview_builder.build();
  }

  /* DecisionInterface */
  @Override
  public Decision getDecision() {
    return null;
  }

  @Override
  public void setDecision(Decision _decision_) {
    decision = _decision_;

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
}

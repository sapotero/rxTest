package sapotero.rxtest.views.managers.builders;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.google.gson.Gson;

import sapotero.rxtest.R;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.views.fragments.DecisionPreviewFragment;
import sapotero.rxtest.views.managers.interfaces.DecisionInterface;

public class PreviewBuilder implements DecisionInterface {
  private final FragmentManager fragmentManger;
  private Decision decision;
  private final String TAG = this.getClass().getSimpleName();

  public PreviewBuilder(FragmentManager fragmentManger, Decision decision) {
    this.fragmentManger = fragmentManger;
    this.decision = decision;
  }

  public void build() {

    Bundle bundle = new Bundle();
    Gson gson = new Gson();

    if ( decision != null ){
      bundle.putString("decision", gson.toJson(decision, Decision.class) );
    } else {
      bundle.putString("decision", gson.toJson( getNewDecision() , Decision.class) );
    }

    FragmentTransaction transaction = fragmentManger.beginTransaction();
    DecisionPreviewFragment body = new DecisionPreviewFragment();
    body.setArguments(bundle);
    transaction.add( R.id.decision_constructor_decision_preview, body );
    transaction.commit();

  }

  private Decision getNewDecision() {
    Decision empty_decision = new Decision();
    return empty_decision;
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

}

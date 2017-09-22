package sapotero.rxtest.managers.view.builders;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import sapotero.rxtest.R;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.managers.view.interfaces.DecisionInterface;
import sapotero.rxtest.views.fragments.DecisionPreviewFragment;

public class PreviewBuilder implements DecisionInterface {
  private final FragmentManager fragmentManger;

  private Decision decision;
  private DecisionPreviewFragment preview;

  public PreviewBuilder(FragmentManager fragmentManger, Decision decision) {
    this.fragmentManger = fragmentManger;
    this.decision = decision;
  }

  public void build() {
    if (preview == null){
      preview = new DecisionPreviewFragment().withInEditor( true );
      preview.setDecision( decision != null ? decision : getNewDecision() );
      FragmentTransaction transaction = fragmentManger.beginTransaction();
      transaction.add( R.id.decision_constructor_decision_preview, preview, DecisionPreviewFragment.class.getSimpleName() );
      transaction.commit();
    } else {
      preview.update();
    }
  }

  private Decision getNewDecision() {
    Decision empty_decision = new Decision();

    Block block = new Block();
    block.setNumber(1);

    empty_decision.getBlocks().add( block );
    return empty_decision;
  }

  public void update() {
    if (preview != null) {
      preview.setDecision(decision);
      preview.update();
    }
  }

  /* DecisionInterface */
  @Override
  public Decision getDecision() {
    return decision;
  }

  @Override
  public void setDecision(Decision _decision_) {
    decision = _decision_;
  }
}

package sapotero.rxtest.managers.view.builders;

import android.support.v4.app.FragmentManager;

import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.managers.view.interfaces.DecisionInterface;
import timber.log.Timber;

public class DecisionBuilder implements DecisionInterface, BlockFactory.Callback {
  private final FragmentManager fragmentManger;
  private final BlockFactory block_builder;
  private final String TAG = this.getClass().getSimpleName();
  private Decision decision;

  public Callback callback;

  public interface Callback {
    void onUpdateSuccess(Decision decision);
    void onUpdateError(Throwable error);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public DecisionBuilder(FragmentManager fragmentManger, Decision decision) {
    this.fragmentManger = fragmentManger;
    this.decision = decision;
    this.block_builder = new BlockFactory( fragmentManger, this.decision );

    this.block_builder.registerCallBack(this);
  }

  public void build() {
    block_builder.build();
  }

  public void addBlock() {
    block_builder.addNewBlock();
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



  /* BlockFactory.Callback */
  @Override
  public void onUpdateSuccess(Decision decision) {
    setDecision(decision);

    Timber.tag(TAG).i("onUpdateSuccess");
    if (callback != null){
      callback.onUpdateSuccess(decision);
    }
  }

  @Override
  public void onUpdateError(Throwable error) {

  }
}

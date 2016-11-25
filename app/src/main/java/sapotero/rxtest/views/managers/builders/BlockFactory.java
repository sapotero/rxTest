package sapotero.rxtest.views.managers.builders;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.google.gson.Gson;

import java.util.ArrayList;

import sapotero.rxtest.R;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.views.fragments.DecisionFragment;
import sapotero.rxtest.views.managers.interfaces.DecisionInterface;
import timber.log.Timber;

public class BlockFactory implements DecisionInterface, DecisionFragment.Callback {
  private final FragmentManager fragmentManger;
  private final ArrayList<DecisionFragment> blocks;
  private Decision decision;

  private int index;
  private final String TAG = this.getClass().getSimpleName();


  private Callback callback;

  public interface Callback {
    void onUpdateSuccess(Decision decision);
    void onUpdateError(Throwable error);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }


  public BlockFactory(FragmentManager fragmentManger, Decision decision) {
    this.fragmentManger = fragmentManger;
    this.decision = decision;

    this.blocks = new ArrayList<>();
    this.index = 0;
  }

  public void build() {
    if (decision!= null && decision.getBlocks().size() > 0) {
      for (Block block : decision.getBlocks()) {
        add(block);
      }
    } else {
      add(new Block());
    }
  }

  private void add(Block block) {
    Bundle bundle = new Bundle();
    Gson gson = new Gson();

    FragmentTransaction transaction = fragmentManger.beginTransaction();
    DecisionFragment fragment = new DecisionFragment();
    fragment.registerCallBack(this);

    bundle.putInt( "number", ++index );
    bundle.putString( "block", gson.toJson(block) );

    fragment.setArguments(bundle);

    transaction.add(R.id.decisions_container, fragment );
    transaction.commit();

    blocks.add( fragment );
  }

  public void remove( DecisionFragment fragment ){
    try{
      --index;
      blocks.remove( fragment );
    } catch (Exception e){
      Timber.tag(TAG).e( e );
    }
  }

  @Override
  public void setDecision(Decision _decision_) {
    decision = _decision_;
  }

  @Override
  public Decision getDecision() {

    if (blocks.size() > 0){
      ArrayList<Block> _blocks_ = new ArrayList<>();

      decision.setBlocks( _blocks_ );

      for (DecisionFragment block: blocks) {
        _blocks_.add( block.getBlock() );
      }

      decision.setBlocks( _blocks_ );
    }



    return decision;
  }

  /* DecisionFragment.Callback */

  @Override
  public void onUpdateSuccess() {
    Timber.tag(TAG).i("onUpdateSuccess");
    if (callback != null){
      callback.onUpdateSuccess( decision );
    }
  }

  @Override
  public void onUpdateError(Throwable error) {
  }
}

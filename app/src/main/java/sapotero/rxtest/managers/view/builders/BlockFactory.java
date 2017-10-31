package sapotero.rxtest.managers.view.builders;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.google.gson.Gson;

import java.util.ArrayList;

import sapotero.rxtest.R;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.views.adapters.PrimaryConsiderationAdapter;
import sapotero.rxtest.views.fragments.DecisionFragment;
import sapotero.rxtest.managers.view.interfaces.DecisionInterface;
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

  public void addNewBlock() {
    Bundle bundle = new Bundle();
    Gson gson = new Gson();

    FragmentTransaction transaction = fragmentManger.beginTransaction();
    DecisionFragment fragment = new DecisionFragment();
    fragment.setBlockFactory(this);
    fragment.registerCallBack(this);

    Block block = new Block();

    bundle.putInt( "number", ++index );
    bundle.putString( "block", gson.toJson(block) );

    fragment.setArguments(bundle);
    fragment.withScrollTo(true);

    transaction.add(R.id.decisions_container, fragment );
    transaction.commit();
    

    blocks.add( fragment );
    decision.getBlocks().add( block );
  }

  private void add(Block block) {
    Bundle bundle = new Bundle();
    Gson gson = new Gson();

    FragmentTransaction transaction = fragmentManger.beginTransaction();
    DecisionFragment fragment = new DecisionFragment();
    fragment.setBlockFactory(this);
    fragment.registerCallBack(this);

    bundle.putInt( "number", ++index );
    bundle.putString( "block", gson.toJson(block) );

    fragment.setArguments(bundle);

    transaction.add(R.id.decisions_container, fragment );
    transaction.commit();


    blocks.add( fragment );
//    decision.getBlocks().addByOne( block );
  }

  public void remove( DecisionFragment fragment ){
    --index;

    if (blocks.contains(fragment)){
      decision.getBlocks().remove( fragment.getBlock() );
      blocks.remove( fragment );
    }

    recalculate();
  }

  public int size(){
    return blocks.size();
  }

  private void recalculate() {
    for (int i = 0; i < blocks.size(); i++) {
      blocks.get(i).setNumber(i+1);
    }

    index = blocks.size();
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
  public void onUpdateSuccess(int lastUpdated) {

    int originCount = 0;

    for (DecisionFragment block: blocks) {
      PrimaryConsiderationAdapter adapter = block.getPerformerAdapter();
      if ( adapter != null && adapter.hasOriginal() ) {
        originCount++;
      }
    }
    Timber.tag("BlockF.onUpdateSuccess").i("lastUpdated %s | originCount %s", lastUpdated, originCount);

    if ( originCount > 1 && lastUpdated != -1 ){

      for (DecisionFragment block: blocks) {
        Timber.tag("BlockF.block").i("num %s", block.getNumber());
        if ( block.getNumber() != lastUpdated ) {
          Timber.tag("dropAll").i("num %s", block.getNumber());
          block.dropAllOriginal();
        }
      }

    }

    // Save block changes into decision
    getDecision();

    Timber.tag(TAG).i("onUpdateSuccess");
    if (callback != null){
      callback.onUpdateSuccess( decision );
    }
  }

  @Override
  public void onUpdateError(Throwable error) {
  }
}
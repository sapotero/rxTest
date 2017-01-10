package sapotero.rxtest.views.views.stepper.build;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import sapotero.rxtest.views.views.stepper.adapter.AbstractStepAdapter;
import sapotero.rxtest.views.views.stepper.build.steps.FirstStepFragment;
import sapotero.rxtest.views.views.stepper.build.steps.SecondStepFragment;


public class StepperAdapter extends AbstractStepAdapter {

  private static final String CURRENT_STEP_POSITION_KEY = "0";

  public StepperAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override
  public Fragment createStep(int position) {

    Fragment step = null;


    switch (position){
      case 0:
        step = new FirstStepFragment();
        break;
      case 1:
        step = new SecondStepFragment();
        break;
      case 2:
        step = new FirstStepFragment();
        break;
      default:
        step = new FirstStepFragment();
        break;

    }

    if (step != null) {
      Bundle b = new Bundle();
      b.putInt(CURRENT_STEP_POSITION_KEY, position);
      step.setArguments(b);
    }

    return step;
  }

  @Override
  public int getCount() {
    return 3;
  }
}
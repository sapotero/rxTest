package sapotero.rxtest.views.custom.stepper.build;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import sapotero.rxtest.views.custom.stepper.adapter.AbstractStepAdapter;
import sapotero.rxtest.views.custom.stepper.build.steps.StepperAuthFragment;
import sapotero.rxtest.views.custom.stepper.build.steps.StepperChooseAuthTypeFragment;
import sapotero.rxtest.views.custom.stepper.build.steps.StepperFinishFragment;
import sapotero.rxtest.views.custom.stepper.build.steps.StepperLoadDataFragment;


public class StepperAdapter extends AbstractStepAdapter {

  private static final String CURRENT_STEP_POSITION_KEY = "0";

  public StepperAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override
  public Fragment createStep(int position) {

    Fragment step;


    switch (position){
      case 0:
        step = new StepperChooseAuthTypeFragment();
        break;
      case 1:
        step = new StepperAuthFragment();
        break;
      case 2:
        step = new StepperLoadDataFragment();
        break;
      case 3:
        step = new StepperFinishFragment();
        break;
      default:
        step = new StepperChooseAuthTypeFragment();
        break;
    }

    Bundle b = new Bundle();
    b.putInt(CURRENT_STEP_POSITION_KEY, position);
    step.setArguments(b);

    return step;
  }

  @Override
  public int getCount() {
    return 4;
  }
}
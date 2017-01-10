package sapotero.rxtest.views.views.stepper.type;

import android.support.annotation.NonNull;
import android.view.View;

import sapotero.rxtest.R;
import sapotero.rxtest.views.views.stepper.StepperLayout;
import sapotero.rxtest.views.views.stepper.adapter.AbstractStepAdapter;
import sapotero.rxtest.views.views.stepper.internal.DottedProgressBar;

public class DotsStepperType extends AbstractStepperType {

    private final DottedProgressBar mDottedProgressBar;

    public DotsStepperType(StepperLayout stepperLayout) {
        super(stepperLayout);
        mDottedProgressBar = (DottedProgressBar) stepperLayout.findViewById(R.id.ms_stepDottedProgressBar);

        mDottedProgressBar.setSelectedColor(getSelectedColor());
        mDottedProgressBar.setUnselectedColor(getUnselectedColor());
    }

    @Override
    public void onStepSelected(int newStepPosition) {
        mDottedProgressBar.setCurrent(newStepPosition, true);
    }

    @Override
    public void onNewAdapter(@NonNull AbstractStepAdapter stepAdapter) {
        final int stepCount = stepAdapter.getCount();
        mDottedProgressBar.setDotCount(stepCount);
        mDottedProgressBar.setVisibility(stepCount > 1 ? View.VISIBLE : View.GONE);
    }
}

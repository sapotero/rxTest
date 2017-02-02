package sapotero.rxtest.views.custom.stepper.type;

import android.support.annotation.NonNull;
import android.view.View;

import sapotero.rxtest.R;
import sapotero.rxtest.views.custom.stepper.StepperLayout;
import sapotero.rxtest.views.custom.stepper.adapter.AbstractStepAdapter;
import sapotero.rxtest.views.custom.stepper.internal.ColorableProgressBar;


/**
 * Stepper type which displays a mobile step progress bar.
 */
public class ProgressBarStepperType extends AbstractStepperType {

    private final ColorableProgressBar mProgressBar;

    public ProgressBarStepperType(StepperLayout stepperLayout) {
        super(stepperLayout);
        mProgressBar = (ColorableProgressBar) stepperLayout.findViewById(R.id.ms_stepProgressBar);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setProgressColor(getSelectedColor());
        mProgressBar.setProgressBackgroundColor(getUnselectedColor());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStepSelected(int newStepPosition) {
        mProgressBar.setProgress(newStepPosition + 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNewAdapter(@NonNull AbstractStepAdapter stepAdapter) {
        final int stepCount = stepAdapter.getCount();
        mProgressBar.setMax(stepAdapter.getCount());
        mProgressBar.setVisibility(stepCount > 1 ? View.VISIBLE : View.GONE);
    }
}

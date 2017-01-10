package sapotero.rxtest.views.views.stepper.type;

import android.support.annotation.NonNull;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.views.views.stepper.Step;
import sapotero.rxtest.views.views.stepper.StepperLayout;
import sapotero.rxtest.views.views.stepper.adapter.AbstractStepAdapter;
import sapotero.rxtest.views.views.stepper.internal.TabsContainer;

public class TabsStepperType extends AbstractStepperType {

    private final TabsContainer mTabsContainer;

    public TabsStepperType(StepperLayout stepperLayout) {
        super(stepperLayout);
        mTabsContainer = (TabsContainer) stepperLayout.findViewById(R.id.ms_stepTabsContainer);
        mTabsContainer.setVisibility(View.VISIBLE);
        mTabsContainer.setSelectedColor(stepperLayout.getSelectedColor());
        mTabsContainer.setUnselectedColor(stepperLayout.getUnselectedColor());
        mTabsContainer.setDividerWidth(stepperLayout.getTabStepDividerWidth());
        mTabsContainer.setListener(stepperLayout);
    }

    @Override
    public void onStepSelected(int newStepPosition) {
        mTabsContainer.setCurrentStep(newStepPosition);
    }

    @Override
    public void onNewAdapter(@NonNull AbstractStepAdapter<?> stepAdapter) {
        List<Integer> titles = new ArrayList<>();
        final int stepCount = stepAdapter.getCount();
        for (int i = 0; i < stepCount; i++) {
            final Step step = stepAdapter.getItem(i);
            titles.add(step.getName());
        }
        mTabsContainer.setSteps(titles);
        mTabsContainer.setVisibility(stepCount > 1 ? View.VISIBLE : View.GONE);
    }
}

package sapotero.rxtest.views.custom.stepper.type;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import sapotero.rxtest.views.custom.stepper.StepperLayout;
import sapotero.rxtest.views.custom.stepper.adapter.AbstractStepAdapter;


public abstract class AbstractStepperType {

    public static final int DOTS = 0x01;
    public static final int PROGRESS_BAR = 0x02;
    public static final int TABS = 0x03;

    protected final StepperLayout stepperLayout;

    public AbstractStepperType(StepperLayout stepperLayout) {
        this.stepperLayout = stepperLayout;
    }

    public abstract void onStepSelected(int newStepPosition);

    public abstract void onNewAdapter(@NonNull AbstractStepAdapter<?> stepAdapter);

    @ColorInt
    protected int getSelectedColor() {
        return stepperLayout.getSelectedColor();
    }

    @ColorInt
    protected int getUnselectedColor() {
        return stepperLayout.getUnselectedColor();
    }

}
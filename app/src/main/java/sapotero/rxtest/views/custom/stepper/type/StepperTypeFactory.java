package sapotero.rxtest.views.custom.stepper.type;

import android.util.Log;

import sapotero.rxtest.views.custom.stepper.StepperLayout;

public class StepperTypeFactory {

    private static final String TAG = StepperTypeFactory.class.getSimpleName();

    public static AbstractStepperType createType(int stepType, StepperLayout stepperLayout) {
        switch (stepType) {
            case AbstractStepperType.DOTS:
                return new DotsStepperType(stepperLayout);
            case AbstractStepperType.PROGRESS_BAR:
                return new ProgressBarStepperType(stepperLayout);
            case AbstractStepperType.TABS:
                return new TabsStepperType(stepperLayout);
            default:
                Log.e(TAG, "Unsupported type: " + stepType);
                throw new IllegalArgumentException("Unsupported type: " + stepType);
        }
    }
}

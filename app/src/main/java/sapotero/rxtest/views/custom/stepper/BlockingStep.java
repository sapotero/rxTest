package sapotero.rxtest.views.custom.stepper;

import android.support.annotation.UiThread;


public interface BlockingStep extends Step {

    @UiThread
    void onNextClicked(StepperLayout.OnNextClickedCallback callback);

    @UiThread
    void onBackClicked(StepperLayout.OnBackClickedCallback callback);

}

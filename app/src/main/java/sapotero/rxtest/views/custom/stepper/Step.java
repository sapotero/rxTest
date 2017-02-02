package sapotero.rxtest.views.custom.stepper;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

public interface Step {
    @StringRes int getName();
    VerificationError verifyStep();
    void onSelected();
    void onError(@NonNull VerificationError error);
}

package sapotero.rxtest.views.custom.stepper.internal;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import sapotero.rxtest.R;

public class RightNavigationButton extends AppCompatButton {

    private static final int[] STATE_VERIFICATION_FAILED = {R.attr.state_verification_failed};

    private boolean mVerificationFailed = false;

    public RightNavigationButton(Context context) {
        this(context, null);
    }

    public RightNavigationButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RightNavigationButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        if (mVerificationFailed) {
            final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
            mergeDrawableStates(drawableState, STATE_VERIFICATION_FAILED);
            return drawableState;
        } else {
            return super.onCreateDrawableState(extraSpace);
        }
    }

    public void setVerificationFailed(boolean verificationFailed) {
        if (this.mVerificationFailed != verificationFailed) {
            this.mVerificationFailed = verificationFailed;
            refreshDrawableState();
        }
    }

}

package sapotero.rxtest.views.custom.stepper.internal;


import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import sapotero.rxtest.R;
import sapotero.rxtest.views.custom.stepper.StepperLayout;
import sapotero.rxtest.views.custom.stepper.util.TintUtil;

public class StepTab extends RelativeLayout {

    private static final float INACTIVE_STEP_TITLE_ALPHA = 0.54f;

    private static final int OPAQUE_ALPHA = 1;

    @ColorInt
    private int mUnselectedColor;

    @ColorInt
    private int mSelectedColor;

    private final TextView mStepNumber;

    private final View mStepDivider;

    private final TextView mStepTitle;

    private final ImageView mStepDoneIndicator;

    public StepTab(Context context) {
        this(context, null);
    }

    public StepTab(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepTab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(getContext()).inflate(R.layout.ms_step_tab, this, true);

        mSelectedColor = ContextCompat.getColor(context, R.color.ms_selectedColor);
        mUnselectedColor = ContextCompat.getColor(context, R.color.ms_unselectedColor);

        mStepNumber = (TextView) findViewById(R.id.ms_stepNumber);
        mStepDoneIndicator = (ImageView) findViewById(R.id.ms_stepDoneIndicator);
        mStepDivider = findViewById(R.id.ms_stepDivider);
        mStepTitle = ((TextView) findViewById(R.id.ms_stepTitle));
    }
    public void toggleDividerVisibility(boolean show) {
        mStepDivider.setVisibility(show ? VISIBLE : GONE);
    }

    public void updateState(final boolean done, final boolean current) {
        mStepDoneIndicator.setVisibility(done ? View.VISIBLE : View.GONE);
        mStepNumber.setVisibility(!done ? View.VISIBLE : View.GONE);
        colorViewBackground(done ? mStepDoneIndicator : mStepNumber, done || current);

        mStepTitle.setTypeface(current ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        mStepTitle.setAlpha(done || current ? OPAQUE_ALPHA : INACTIVE_STEP_TITLE_ALPHA);
    }

    public void setStepTitle(CharSequence title) {
        mStepTitle.setText(title);
    }

    public void setStepTitle(@StringRes int title) {
        mStepTitle.setText(title);
    }

    public void setStepNumber(CharSequence number) {
        mStepNumber.setText(number);
    }

    public void setUnselectedColor(int unselectedColor) {
        this.mUnselectedColor = unselectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        this.mSelectedColor = selectedColor;
    }

    private void colorViewBackground(View view, boolean selected) {
        Drawable d = view.getBackground();
        TintUtil.tintDrawable(d, selected ? mSelectedColor : mUnselectedColor);
    }

    public void setDividerWidth(int dividerWidth) {
        mStepDivider.getLayoutParams().width = dividerWidth != StepperLayout.DEFAULT_TAB_DIVIDER_WIDTH
                ? dividerWidth
                : getResources().getDimensionPixelOffset(R.dimen.ms_step_tab_divider_length);
    }

}

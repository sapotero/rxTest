package sapotero.rxtest.views.custom.stepper.internal;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;

import sapotero.rxtest.R;
import sapotero.rxtest.views.custom.stepper.util.TintUtil;

public class DottedProgressBar extends LinearLayout {

    private static final float FULL_SCALE = 1f;
    private static final float HALF_SCALE = 0.5f;
    private static final int DURATION_IMMEDIATE = 0;
    private static final int SCALE_ANIMATION_DEFAULT_DURATION = 300;

    @ColorInt
    private int mUnselectedColor;

    @ColorInt
    private int mSelectedColor;

    private int mDotCount;

    private int mCurrent;

    public DottedProgressBar(Context context) {
        this(context, null);
    }

    public DottedProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DottedProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSelectedColor = ContextCompat.getColor(context, R.color.ms_selectedColor);
        mUnselectedColor = ContextCompat.getColor(context, R.color.ms_unselectedColor);
        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(
                    attrs, R.styleable.DottedProgressBar, defStyleAttr, 0);

            if (a.hasValue(R.styleable.DottedProgressBar_ms_activeDotColor)) {
                mSelectedColor = a.getColor(R.styleable.DottedProgressBar_ms_activeDotColor, mSelectedColor);
            }
            if (a.hasValue(R.styleable.DottedProgressBar_ms_inactiveDotColor)) {
                mUnselectedColor = a.getColor(R.styleable.DottedProgressBar_ms_inactiveDotColor, mUnselectedColor);
            }

            a.recycle();
        }
    }

    public void setUnselectedColor(@ColorInt int unselectedColor) {
        this.mUnselectedColor = unselectedColor;
    }

    public void setSelectedColor(@ColorInt int selectedColor) {
        this.mSelectedColor = selectedColor;
    }

    public void setDotCount(int dotCount) {
        this.mDotCount = dotCount;
        removeAllViews();
        for (int i = 0; i < dotCount; i++) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.ms_dot, this, false);
            addView(view);
        }
        setCurrent(0, false);
    }

    public void setCurrent(int current, boolean shouldAnimate) {
        this.mCurrent = current;
        update(shouldAnimate);
    }

    private void update(boolean shouldAnimate) {
        for (int i = 0; i < mDotCount; i++) {
            final AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
            if (i == mCurrent) {
                getChildAt(i).animate()
                        .scaleX(FULL_SCALE)
                        .scaleY(FULL_SCALE)
                        .setDuration(shouldAnimate ? SCALE_ANIMATION_DEFAULT_DURATION : DURATION_IMMEDIATE)
                        .setInterpolator(interpolator)
                        .start();
                colorChildAtPosition(i, true);
            } else {
                getChildAt(i).animate()
                        .scaleX(HALF_SCALE)
                        .scaleY(HALF_SCALE)
                        .setDuration(shouldAnimate ? SCALE_ANIMATION_DEFAULT_DURATION : DURATION_IMMEDIATE)
                        .setInterpolator(interpolator)
                        .start();
                colorChildAtPosition(i, false);
            }
        }
    }

    private void colorChildAtPosition(int i, boolean selected) {
        Drawable d = getChildAt(i).getBackground();
        TintUtil.tintDrawable(d, selected ? mSelectedColor : mUnselectedColor);
    }

}

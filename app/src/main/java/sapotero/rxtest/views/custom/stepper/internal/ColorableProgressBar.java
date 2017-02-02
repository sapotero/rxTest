package sapotero.rxtest.views.custom.stepper.internal;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import sapotero.rxtest.R;
import sapotero.rxtest.views.custom.stepper.util.TintUtil;


public class ColorableProgressBar extends ProgressBar {

    @ColorInt
    private int mProgressColor;

    @ColorInt
    private int mProgressBackgroundColor;

    public ColorableProgressBar(Context context) {
        this(context, null);
    }

    public ColorableProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorableProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mProgressColor = ContextCompat.getColor(context, R.color.ms_selectedColor);
        mProgressBackgroundColor = ContextCompat.getColor(context, R.color.ms_unselectedColor);
        super.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.ms_colorable_progress_bar));

        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(
                    attrs, R.styleable.ColorableProgressBar, defStyleAttr, 0);

            if (a.hasValue(R.styleable.ColorableProgressBar_ms_progressPrimaryColor)) {
                mProgressColor = a.getColor(R.styleable.ColorableProgressBar_ms_progressPrimaryColor, mProgressColor);
            }
            if (a.hasValue(R.styleable.ColorableProgressBar_ms_progressBackgroundColor)) {
                mProgressBackgroundColor = a.getColor(R.styleable.ColorableProgressBar_ms_progressBackgroundColor, mProgressBackgroundColor);
            }

            a.recycle();
        }
        updateProgressDrawable();
    }

    @Override
    public void setProgressDrawable(Drawable d) {
        // no-op
    }

    @Override
    public void setProgressDrawableTiled(Drawable d) {
        // no-op
    }

    public void setProgressColor(@ColorInt int progressColor) {
        this.mProgressColor = progressColor;
        updateProgressDrawable();
    }

    public void setProgressBackgroundColor(@ColorInt int backgroundColor) {
        this.mProgressBackgroundColor = backgroundColor;
        updateProgressDrawable();
    }

    private void updateProgressDrawable() {
        LayerDrawable progressBarDrawable = (LayerDrawable) getProgressDrawable();
        Drawable backgroundDrawable = progressBarDrawable.findDrawableByLayerId(android.R.id.background);
        Drawable progressDrawable = progressBarDrawable.findDrawableByLayerId(android.R.id.progress);
        TintUtil.tintDrawable(backgroundDrawable, mProgressBackgroundColor);
        TintUtil.tintDrawable(progressDrawable, mProgressColor);
    }
}

package sapotero.rxtest.views.custom.stepper;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import sapotero.rxtest.R;
import sapotero.rxtest.views.custom.stepper.adapter.AbstractStepAdapter;
import sapotero.rxtest.views.custom.stepper.internal.ColorableProgressBar;
import sapotero.rxtest.views.custom.stepper.internal.DottedProgressBar;
import sapotero.rxtest.views.custom.stepper.internal.RightNavigationButton;
import sapotero.rxtest.views.custom.stepper.internal.TabsContainer;
import sapotero.rxtest.views.custom.stepper.type.AbstractStepperType;
import sapotero.rxtest.views.custom.stepper.type.StepperTypeFactory;
import sapotero.rxtest.views.custom.stepper.util.AnimationUtil;
import sapotero.rxtest.views.custom.stepper.util.TintUtil;

public class StepperLayout extends LinearLayout implements TabsContainer.TabItemListener {

    public static final int DEFAULT_TAB_DIVIDER_WIDTH = -1;


    public interface StepperListener {

        void onCompleted(View completeButton);

        void onError(VerificationError verificationError);

        void onStepSelected(int newStepPosition);

        void onReturn();

        StepperListener NULL = new StepperListener() {
            @Override
            public void onCompleted(View completeButton) {
            }

            @Override
            public void onError(VerificationError verificationError) {
            }

            @Override
            public void onStepSelected(int newStepPosition) {
            }

            @Override
            public void onReturn() {
            }
        };
    }

    public final class OnNextClickedCallback {

        @UiThread
        public final void goToNextStep() {
            final int totalStepCount = mStepAdapter.getCount();

            if (mCurrentStepPosition >= totalStepCount - 1) {
                return;
            }

            mCurrentStepPosition++;
            onUpdate(mCurrentStepPosition, true);
        }

    }

    public final class OnBackClickedCallback {

        @UiThread
        public final void goToPrevStep() {
            if (mCurrentStepPosition <= 0) {
                if (mShowBackButtonOnFirstStep) {
                    mListener.onReturn();
                }
                return;
            }
            mCurrentStepPosition--;
            onUpdate(mCurrentStepPosition, true);
        }

    }


    public ViewPager getmPager() {
        return mPager;
    }

    public ViewPager mPager;

    public Button mBackNavigationButton;
    public RightNavigationButton mNextNavigationButton;
    public RightNavigationButton mCompleteNavigationButton;

    private ViewGroup mStepNavigation;

    private DottedProgressBar mDottedProgressBar;

    private ColorableProgressBar mProgressBar;

    private TabsContainer mTabsContainer;

    private ColorStateList mBackButtonColor;

    private ColorStateList mNextButtonColor;

    private ColorStateList mCompleteButtonColor;

    @ColorInt
    private int mUnselectedColor;

    @ColorInt
    private int mSelectedColor;

    @DrawableRes
    private int mBottomNavigationBackground;

    @DrawableRes
    private int mBackButtonBackground;

    @DrawableRes
    private int mNextButtonBackground;

    @DrawableRes
    private int mCompleteButtonBackground;

    private int mTabStepDividerWidth = DEFAULT_TAB_DIVIDER_WIDTH;

    private String mBackButtonText;

    private String mNextButtonText;

    private String mCompleteButtonText;

    private boolean mShowBackButtonOnFirstStep;

    private int mTypeIdentifier = AbstractStepperType.PROGRESS_BAR;

    private AbstractStepAdapter mStepAdapter;

    private AbstractStepperType mStepperType;

    private int mCurrentStepPosition;

    public Button getmBackNavigationButton() {
      return mBackNavigationButton;
    }

    public RightNavigationButton getmNextNavigationButton() {
      return mNextNavigationButton;
    }

    public RightNavigationButton getmCompleteNavigationButton() {
      return mCompleteNavigationButton;
    }

    @NonNull
    private StepperListener mListener = StepperListener.NULL;

    private OnClickListener mOnBackClickListener = v -> onPrevious();
    private OnClickListener mOnNextClickListener = v -> onNext();
    private OnClickListener mOnCompleteClickListener = v -> onComplete(v);

    public StepperLayout(Context context) {
        this(context, null);
    }

    public StepperLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.ms_stepperStyle);
    }

    public StepperLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    public void setListener(@NonNull StepperListener listener) {
        this.mListener = listener;
    }

    public void setAdapter(@NonNull AbstractStepAdapter stepAdapter) {
        this.mStepAdapter = stepAdapter;
        mPager.setAdapter(stepAdapter);

        mStepperType.onNewAdapter(stepAdapter);

        new Handler().post(() -> onUpdate(mCurrentStepPosition, false));

    }

    public void setAdapter(@NonNull AbstractStepAdapter stepAdapter, @IntRange(from = 0) int currentStepPosition) {
        this.mCurrentStepPosition = currentStepPosition;
        setAdapter(stepAdapter);
    }
    public void setPageTransformer(@Nullable ViewPager.PageTransformer pageTransformer) {
        mPager.setPageTransformer(false, pageTransformer);
    }

    public int getSelectedColor() {
        return mSelectedColor;
    }

    public int getUnselectedColor() {
        return mUnselectedColor;
    }

    public int getTabStepDividerWidth() {
        return mTabStepDividerWidth;
    }

    @Override
    @UiThread
    public void onTabClicked(int position) {
        if (position > mCurrentStepPosition) {
            onNext();
        } else if (position < mCurrentStepPosition) {
            setCurrentStepPosition(position);
        }
    }

    public void setCurrentStepPosition(int currentStepPosition) {
        this.mCurrentStepPosition = currentStepPosition;
        onUpdate(currentStepPosition, true);
    }

    public int getCurrentStepPosition() {
        return mCurrentStepPosition;
    }

    public void setNextButtonVerificationFailed(boolean verificationFailed) {
        mNextNavigationButton.setVerificationFailed(verificationFailed);
    }

    public void setCompleteButtonVerificationFailed(boolean verificationFailed) {
        mCompleteNavigationButton.setVerificationFailed(verificationFailed);
    }

    private void init(AttributeSet attrs, @AttrRes int defStyleAttr) {
        initDefaultValues();
        extractValuesFromAttributes(attrs, defStyleAttr);

        final Context context = getContext();
        LayoutInflater.from(context).inflate(R.layout.ms_stepper_layout, this, true);

        bindViews();

        mPager.setOnTouchListener((view, motionEvent) -> true);

        initNavigation();

        mDottedProgressBar.setVisibility(GONE);
        mProgressBar.setVisibility(GONE);
        mTabsContainer.setVisibility(GONE);

        mStepperType = StepperTypeFactory.createType(mTypeIdentifier, this);
    }

    private void initNavigation() {
        mStepNavigation.setBackgroundResource(mBottomNavigationBackground);

        mBackNavigationButton.setText(mBackButtonText);
        mNextNavigationButton.setText(mNextButtonText);
        mCompleteNavigationButton.setText(mCompleteButtonText);

        Drawable chevronEndDrawable = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.ic_chevron_right, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mNextNavigationButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, chevronEndDrawable, null);
        } else {
            mNextNavigationButton.setCompoundDrawablesWithIntrinsicBounds(null, null, chevronEndDrawable, null);
        }

        TintUtil.tintTextView(mBackNavigationButton, mBackButtonColor);
        TintUtil.tintTextView(mNextNavigationButton, mNextButtonColor);
        TintUtil.tintTextView(mCompleteNavigationButton, mCompleteButtonColor);

        setBackgroundIfPresent(mBackButtonBackground, mBackNavigationButton);
        setBackgroundIfPresent(mNextButtonBackground, mNextNavigationButton);
        setBackgroundIfPresent(mCompleteButtonBackground, mCompleteNavigationButton);

        mBackNavigationButton.setOnClickListener(mOnBackClickListener);
        mNextNavigationButton.setOnClickListener(mOnNextClickListener);
        mCompleteNavigationButton.setOnClickListener(mOnCompleteClickListener);
    }

    private void setBackgroundIfPresent(@DrawableRes int backgroundRes, View view) {
        if (backgroundRes != 0) {
            view.setBackgroundResource(backgroundRes);
        }
    }

    private void bindViews() {
        mPager = (ViewPager) findViewById(R.id.ms_stepPager);

        mBackNavigationButton = (Button) findViewById(R.id.ms_stepPrevButton);
        mNextNavigationButton = (RightNavigationButton) findViewById(R.id.ms_stepNextButton);
        mCompleteNavigationButton = (RightNavigationButton) findViewById(R.id.ms_stepCompleteButton);

        mStepNavigation = (ViewGroup) findViewById(R.id.ms_stepNavigation);

        mDottedProgressBar = (DottedProgressBar) findViewById(R.id.ms_stepDottedProgressBar);

        mProgressBar = (ColorableProgressBar) findViewById(R.id.ms_stepProgressBar);

        mTabsContainer = (TabsContainer) findViewById(R.id.ms_stepTabsContainer);
    }

    private void extractValuesFromAttributes(AttributeSet attrs, @AttrRes int defStyleAttr) {
        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(
                    attrs, R.styleable.StepperLayout, defStyleAttr, 0);

            if (a.hasValue(R.styleable.StepperLayout_ms_backButtonColor)) {
                mBackButtonColor = a.getColorStateList(R.styleable.StepperLayout_ms_backButtonColor);
            }
            if (a.hasValue(R.styleable.StepperLayout_ms_nextButtonColor)) {
                mNextButtonColor = a.getColorStateList(R.styleable.StepperLayout_ms_nextButtonColor);
            }
            if (a.hasValue(R.styleable.StepperLayout_ms_completeButtonColor)) {
                mCompleteButtonColor = a.getColorStateList(R.styleable.StepperLayout_ms_completeButtonColor);
            }

            if (a.hasValue(R.styleable.StepperLayout_ms_activeStepColor)) {
                mSelectedColor = a.getColor(R.styleable.StepperLayout_ms_activeStepColor, mSelectedColor);
            }
            if (a.hasValue(R.styleable.StepperLayout_ms_inactiveStepColor)) {
                mUnselectedColor = a.getColor(R.styleable.StepperLayout_ms_inactiveStepColor, mUnselectedColor);
            }
            if (a.hasValue(R.styleable.StepperLayout_ms_bottomNavigationBackground)) {
                mBottomNavigationBackground = a.getResourceId(R.styleable.StepperLayout_ms_bottomNavigationBackground, mBottomNavigationBackground);
            }

            if (a.hasValue(R.styleable.StepperLayout_ms_backButtonBackground)) {
                mBackButtonBackground = a.getResourceId(R.styleable.StepperLayout_ms_backButtonBackground, 0);
            }
            if (a.hasValue(R.styleable.StepperLayout_ms_nextButtonBackground)) {
                mNextButtonBackground = a.getResourceId(R.styleable.StepperLayout_ms_nextButtonBackground, 0);
            }
            if (a.hasValue(R.styleable.StepperLayout_ms_completeButtonBackground)) {
                mCompleteButtonBackground = a.getResourceId(R.styleable.StepperLayout_ms_completeButtonBackground, 0);
            }

            if (a.hasValue(R.styleable.StepperLayout_ms_backButtonText)) {
                mBackButtonText = a.getString(R.styleable.StepperLayout_ms_backButtonText);
            }
            if (a.hasValue(R.styleable.StepperLayout_ms_nextButtonText)) {
                mNextButtonText = a.getString(R.styleable.StepperLayout_ms_nextButtonText);
            }
            if (a.hasValue(R.styleable.StepperLayout_ms_completeButtonText)) {
                mCompleteButtonText = a.getString(R.styleable.StepperLayout_ms_completeButtonText);
            }

            if (a.hasValue(R.styleable.StepperLayout_ms_tabStepDividerWidth)) {
                mTabStepDividerWidth = a.getDimensionPixelOffset(R.styleable.StepperLayout_ms_tabStepDividerWidth, -1);
            }

            mShowBackButtonOnFirstStep = a.getBoolean(R.styleable.StepperLayout_ms_showBackButtonOnFirstStep, false);

            if (a.hasValue(R.styleable.StepperLayout_ms_stepperType)) {
                mTypeIdentifier = a.getInt(R.styleable.StepperLayout_ms_stepperType, DEFAULT_TAB_DIVIDER_WIDTH);
            }

            a.recycle();
        }
    }

    private void initDefaultValues() {
        mBackButtonColor = mNextButtonColor = mCompleteButtonColor = ContextCompat.getColorStateList(getContext(), R.color.ms_bottomNavigationButtonTextColor);
        mSelectedColor = ContextCompat.getColor(getContext(), R.color.ms_selectedColor);
        mUnselectedColor = ContextCompat.getColor(getContext(), R.color.ms_unselectedColor);
        mBottomNavigationBackground = R.color.ms_bottomNavigationBackgroundColor;
        mBackButtonText = getContext().getString(R.string.ms_back);
        mNextButtonText = getContext().getString(R.string.ms_next);
        mCompleteButtonText = getContext().getString(R.string.ms_complete);
    }

    private boolean isLastPosition(int position) {
        return position == mStepAdapter.getCount() - 1;
    }

    private Step findCurrentStep() {
        return mStepAdapter.findStep(mPager, mCurrentStepPosition);
    }

    private void onPrevious() {
        Step step = findCurrentStep();

        OnBackClickedCallback onBackClickedCallback = new OnBackClickedCallback();
        if (step instanceof BlockingStep) {
            ((BlockingStep) step).onBackClicked(onBackClickedCallback);
        } else {
            onBackClickedCallback.goToPrevStep();
        }
    }

    @UiThread
    private void onNext() {
        Step step = findCurrentStep();

        if (verifyCurrentStep(step)) {
            return;
        }
        OnNextClickedCallback onNextClickedCallback = new OnNextClickedCallback();
        if (step instanceof BlockingStep) {
            ((BlockingStep) step).onNextClicked(onNextClickedCallback);
        } else {
            onNextClickedCallback.goToNextStep();
        }
    }

    private boolean verifyCurrentStep(Step step) {
        final VerificationError verificationError = step.verifyStep();
        if (verificationError != null) {
            onError(verificationError);
            return true;
        }
        return false;
    }

    private void onError(@NonNull VerificationError verificationError) {
        Step step = findCurrentStep();
        if (step != null) {
            step.onError(verificationError);
        }
        mListener.onError(verificationError);
    }

    private void onComplete(View completeButton) {
        Step step = findCurrentStep();
        if (verifyCurrentStep(step)) {
            return;
        }
        mListener.onCompleted(completeButton);
    }

    public void onUpdate(int newStepPosition, boolean animate) {
        mPager.setCurrentItem(newStepPosition);
        boolean isLast = isLastPosition(newStepPosition);
        boolean isFirst = newStepPosition == 0;

        AnimationUtil.fadeViewVisibility(mNextNavigationButton, isLast ? View.GONE : View.VISIBLE, animate);
        AnimationUtil.fadeViewVisibility(mCompleteNavigationButton, !isLast ? View.GONE : View.VISIBLE, animate);
        AnimationUtil.fadeViewVisibility(mBackNavigationButton, isFirst && !mShowBackButtonOnFirstStep ? View.GONE : View.VISIBLE, animate);

        if (!isLast) {
            int nextButtonTextForStep = mStepAdapter.getNextButtonText(newStepPosition);
            if (nextButtonTextForStep == AbstractStepAdapter.DEFAULT_NEXT_BUTTON_TEXT) {
                mNextNavigationButton.setText(mNextButtonText);
            } else {
                mNextNavigationButton.setText(nextButtonTextForStep);
            }
        }

        mStepperType.onStepSelected(newStepPosition);
        mListener.onStepSelected(newStepPosition);
        Step step = mStepAdapter.findStep(mPager, newStepPosition);
        if (step != null) {
            step.onSelected();
        }
    }
}

package sapotero.rxtest.views.views.stepper.internal;


import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.views.views.stepper.StepperLayout;


public class TabsContainer extends FrameLayout {

    public interface TabItemListener {

        @UiThread
        void onTabClicked(int position);

        TabItemListener NULL = position -> {
        };
    }

    @ColorInt
    private int mUnselectedColor;

    @ColorInt
    private int mSelectedColor;

    private int mDividerWidth = StepperLayout.DEFAULT_TAB_DIVIDER_WIDTH;

    private final int mContainerLateralPadding;

    private HorizontalScrollView mTabsScrollView;

    private LinearLayout mTabsInnerContainer;

    private TabItemListener mListener = TabItemListener.NULL;

    private List<Integer> mStepTitles;

    public TabsContainer(Context context) {
        this(context, null);
    }

    public TabsContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabsContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.ms_tabs_container, this, true);

        mSelectedColor = ContextCompat.getColor(context, R.color.ms_selectedColor);
        mUnselectedColor = ContextCompat.getColor(context, R.color.ms_unselectedColor);
        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(
                    attrs, R.styleable.TabsContainer, defStyleAttr, 0);

            if (a.hasValue(R.styleable.TabsContainer_ms_activeTabColor)) {
                mSelectedColor = a.getColor(R.styleable.TabsContainer_ms_activeTabColor, mSelectedColor);
            }
            if (a.hasValue(R.styleable.TabsContainer_ms_inactiveTabColor)) {
                mUnselectedColor = a.getColor(R.styleable.TabsContainer_ms_inactiveTabColor, mUnselectedColor);
            }

            a.recycle();
        }
        mContainerLateralPadding = context.getResources().getDimensionPixelOffset(R.dimen.ms_tabs_container_lateral_padding);

        mTabsInnerContainer = (LinearLayout) findViewById(R.id.ms_stepTabsInnerContainer);
        mTabsScrollView = (HorizontalScrollView) findViewById(R.id.ms_stepTabsScrollView);
    }

    public void setUnselectedColor(@ColorInt int unselectedColor) {
        this.mUnselectedColor = unselectedColor;
    }

    public void setSelectedColor(@ColorInt int selectedColor) {
        this.mSelectedColor = selectedColor;
    }

    public void setDividerWidth(int dividerWidth) {
        this.mDividerWidth = dividerWidth;
    }

    public void setListener(@NonNull TabItemListener listener) {
        this.mListener = listener;
    }

    public void setSteps(List<Integer> stepTitles) {
        this.mStepTitles = stepTitles;

        mTabsInnerContainer.removeAllViews();
        for (int i = 0; i < stepTitles.size(); i++) {
            final View tab = createStepTab(i, stepTitles.get(i));
            mTabsInnerContainer.addView(tab, tab.getLayoutParams());
        }
    }

    public void setCurrentStep(int newStepPosition) {
        int size = mStepTitles.size();
        for (int i = 0; i < size; i++) {
            StepTab childTab = (StepTab) mTabsInnerContainer.getChildAt(i);
            boolean done = i < newStepPosition;
            final boolean current = i == newStepPosition;
            childTab.updateState(done, current);
            if (current) {
                mTabsScrollView.smoothScrollTo(childTab.getLeft() - mContainerLateralPadding, 0);
            }
        }
    }

    private View createStepTab(final int position, @StringRes int title) {
        StepTab view = (StepTab) LayoutInflater.from(getContext()).inflate(R.layout.ms_step_tab_container, mTabsInnerContainer, false);
        view.setStepNumber(String.valueOf(position + 1));
        view.toggleDividerVisibility(!isLastPosition(position));
        view.setStepTitle(title);
        view.setSelectedColor(mSelectedColor);
        view.setUnselectedColor(mUnselectedColor);
        view.setDividerWidth(mDividerWidth);

        view.setOnClickListener(view1 -> mListener.onTabClicked(position));

        return view;
    }

    private boolean isLastPosition(int position) {
        return position == mStepTitles.size() - 1;
    }
}

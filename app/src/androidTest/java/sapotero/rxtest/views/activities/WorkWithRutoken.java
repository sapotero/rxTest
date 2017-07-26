package sapotero.rxtest.views.activities;


import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import sapotero.rxtest.R;
import timber.log.Timber;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class WorkWithRutoken {

  @Rule
  public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

  @Test
  public void loginActivityTest2() {
    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction recyclerView = onView(
      allOf(withId(R.id.md_contentRecyclerView),
        withParent(withId(R.id.md_contentListViewFrame)),
        isDisplayed()));
    recyclerView.perform(actionOnItemAtPosition(5, click()));

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction mDButton = onView(
      allOf(withId(R.id.md_buttonDefaultPositive), withText("Продолжить"), isDisplayed()));
    mDButton.perform(click());
    

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction appCompatButton2 = onView(
      allOf(withId(R.id.stepper_auth_choose_cert), withText("Войти"), isDisplayed()));
    appCompatButton2.perform(click());

    ViewInteraction nonSwipeableViewPager = onView(
      allOf(withId(R.id.ms_stepPager),
        withParent(withId(R.id.stepperLayout)),
        isDisplayed()));
    nonSwipeableViewPager.perform(swipeLeft());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction appCompatEditText7 = onView(
      allOf(withId(R.id.stepper_auth_dc_password), isDisplayed()));
    appCompatEditText7.perform(replaceText("12345678"), closeSoftKeyboard());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction appCompatEditText9 = onView(
      allOf(withId(R.id.stepper_auth_dc_password), withText("12345678"), isDisplayed()));
    appCompatEditText9.perform(pressImeActionButton());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction rightNavigationButton = onView(
      allOf(withId(R.id.ms_stepNextButton), withText("Далее"),
        withParent(allOf(withId(R.id.ms_stepNavigation),
          withParent(withId(R.id.stepperLayout)))),
        isDisplayed()));
    rightNavigationButton.perform(click());


    try {
      Thread.sleep(25000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }



    Boolean guard = true;
    do {

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      ViewInteraction button = onView(withId(R.id.ms_stepNextButton));
      button.check(matches(isEnabled()));
      button.check(matches(isClickable()));

      try {
        button.perform(click());
        guard = false;
      } catch (NoMatchingViewException e) {
        Timber.e(e);
        guard = true;
      }

    } while (guard);

    ViewInteraction nonSwipeableViewPager2 = onView(
      allOf(withId(R.id.ms_stepPager),
        withParent(withId(R.id.stepperLayout)),
        isDisplayed()));
    nonSwipeableViewPager2.perform(swipeLeft());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction rightNavigationButton2 = onView(
      allOf(withId(R.id.ms_stepNextButton), withText("Далее"),
        withParent(allOf(withId(R.id.ms_stepNavigation),
          withParent(withId(R.id.stepperLayout)))),
        isDisplayed()));
    try {
      rightNavigationButton2.perform(click());
    } catch (Exception e) {
      Timber.e(e);
    }

    ViewInteraction nonSwipeableViewPager3 = onView(
      allOf(withId(R.id.ms_stepPager),
        withParent(withId(R.id.stepperLayout)),
        isDisplayed()));
    nonSwipeableViewPager3.perform(swipeLeft());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction rightNavigationButton3 = onView(
      allOf(withId(R.id.ms_stepCompleteButton), withText("Завершить"),
        withParent(allOf(withId(R.id.ms_stepNavigation),
          withParent(withId(R.id.stepperLayout)))),
        isDisplayed()));
    rightNavigationButton3.perform(click());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction radioButton = onView(
      allOf(withText("Проекты 22"), isDisplayed()));
    radioButton.perform(click());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction recyclerView3 = onView(
      allOf(withId(R.id.documentsRecycleView), isDisplayed()));
    recyclerView3.perform(actionOnItemAtPosition(21, click()));

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction appCompatTextView2 = onView(
      allOf(withText("Поля документа"), isDisplayed()));
    appCompatTextView2.perform(click());

    ViewInteraction viewPagerFixed = onView(
      allOf(withId(R.id.tab_main), isDisplayed()));
    viewPagerFixed.perform(swipeLeft());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction appCompatTextView3 = onView(
      allOf(withText("Связанные документы"), isDisplayed()));
    appCompatTextView3.perform(click());

    ViewInteraction viewPagerFixed2 = onView(
      allOf(withId(R.id.tab_main), isDisplayed()));
    viewPagerFixed2.perform(swipeLeft());

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction appCompatImageButton2 = onView(
      allOf(withClassName(is("android.support.v7.widget.AppCompatImageButton")),
        withParent(allOf(withId(R.id.toolbar),
          withParent(withId(R.id.activity_info_wrapper)))),
        isDisplayed()));
    appCompatImageButton2.perform(click());

  }

}

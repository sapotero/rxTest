package sapotero.rxtest.views.activities;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import sapotero.rxtest.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

  @Rule
  public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

  @Test
  public void loginActivityTest() {
    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction recyclerView = onView(
      allOf(withId(R.id.md_contentRecyclerView),
        withParent(withId(R.id.md_contentListViewFrame)),
        isDisplayed()));
    recyclerView.perform(actionOnItemAtPosition(4, click()));

    ViewInteraction recyclerView2 = onView(
      allOf(withId(R.id.md_contentRecyclerView),
        withParent(withId(R.id.md_contentListViewFrame)),
        isDisplayed()));
    recyclerView2.perform(actionOnItemAtPosition(6, click()));

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction recyclerView3 = onView(
      allOf(withId(R.id.md_contentRecyclerView),
        withParent(withId(R.id.md_contentListViewFrame)),
        isDisplayed()));
    recyclerView3.perform(actionOnItemAtPosition(5, click()));

    ViewInteraction recyclerView4 = onView(
      allOf(withId(R.id.md_contentRecyclerView),
        withParent(withId(R.id.md_contentListViewFrame)),
        isDisplayed()));
    recyclerView4.perform(actionOnItemAtPosition(5, click()));

    ViewInteraction recyclerView5 = onView(
      allOf(withId(R.id.md_contentRecyclerView),
        withParent(withId(R.id.md_contentListViewFrame)),
        isDisplayed()));
    recyclerView5.perform(actionOnItemAtPosition(5, click()));

    ViewInteraction recyclerView6 = onView(
      allOf(withId(R.id.md_contentRecyclerView),
        withParent(withId(R.id.md_contentListViewFrame)),
        isDisplayed()));
    recyclerView6.perform(actionOnItemAtPosition(5, click()));

    ViewInteraction recyclerView7 = onView(
      allOf(withId(R.id.md_contentRecyclerView),
        withParent(withId(R.id.md_contentListViewFrame)),
        isDisplayed()));
    recyclerView7.perform(actionOnItemAtPosition(5, click()));

    // Added a sleep statement to match the app's execution delay.
    // The recommended way to handle such scenarios is to use Espresso idling resources:
    // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction recyclerView8 = onView(
      allOf(withId(R.id.md_contentRecyclerView),
        withParent(withId(R.id.md_contentListViewFrame)),
        isDisplayed()));
    recyclerView8.perform(actionOnItemAtPosition(5, click()));

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

    ViewInteraction appCompatTextView = onView(
      allOf(withId(R.id.stepper_auth_settings), withText("Настройки"), isDisplayed()));
    appCompatTextView.perform(click());

  }

}

package sapotero.rxtest.views.activities;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import sapotero.rxtest.R;
import sapotero.rxtest.views.dialogs.DecisionMagniferFragment;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class CardViewTest {

  private TestActivity activity;
  private Instrumentation instrumentation;

  @Rule
  public ActivityTestRule<TestActivity> activityTestRule = new ActivityTestRule<>(TestActivity.class);

  @Before
  public void init() {
    instrumentation = InstrumentationRegistry.getInstrumentation();

    activity = activityTestRule.getActivity();
    activity.runOnUiThread(() -> activity.addView(R.layout.documents_adapter_item_layout));
    instrumentation.waitForIdleSync();
  }

  @Test
  public void viewContainerIsPresent() throws Exception {
    View viewById = activity.findViewById(R.id.testactivity_view_container);
    assertThat(viewById, notNullValue());
    assertThat(viewById, instanceOf(FrameLayout.class));
  }

  @Test
  public void cardViewIsPresent() throws Exception {
    View viewById = activity.findViewById(R.id.swipe_layout_cv);
    assertThat(viewById, notNullValue());
    assertThat(viewById, instanceOf(CardView.class));
  }

  @Test
  public void cardViewVisibility() throws Exception {
    String dummyTitle = "dummyTitle";
    String dummySubtitle = "dummySubtitle";
    String dummyBadge = "dummyBadge";
    String dummyFrom = "dummyFrom";
    String dummyDate = "dummyDate";
    String dummyFavorite = "dummyFavorite";
    String dummySync = "dummySync";
    String dummyControl = "dummyControl";
    String dummyLock = "dummyLock";

    activity.runOnUiThread(() -> {
      TextView title = (TextView) activity.findViewById(R.id.swipe_layout_title);
      TextView subtitle = (TextView) activity.findViewById(R.id.swipe_layout_subtitle);
      TextView badge = (TextView) activity.findViewById(R.id.swipe_layout_urgency_badge);
      TextView from = (TextView) activity.findViewById(R.id.swipe_layout_from);
      TextView date = (TextView) activity.findViewById(R.id.swipe_layout_date);
      TextView favorite_label = (TextView) activity.findViewById(R.id.favorite_label);
      TextView sync_label = (TextView) activity.findViewById(R.id.sync_label);
      TextView control_label = (TextView) activity.findViewById(R.id.control_label);
      TextView lock_label = (TextView) activity.findViewById(R.id.lock_label);

      title.setText( dummyTitle );
      subtitle.setText( dummySubtitle );
      badge.setText( dummyBadge );
      from.setText( dummyFrom );
      date.setText( dummyDate );
      favorite_label.setText( dummyFavorite );
      sync_label.setText( dummySync );
      control_label.setText( dummyControl );
      lock_label.setText( dummyLock );
    });

    instrumentation.waitForIdleSync();

    onView( withText( dummyTitle )).check(matches(isDisplayed()));
    onView( withText( dummySubtitle )).check(matches(isDisplayed()));
    onView( withText( dummyBadge )).check(matches(isDisplayed()));
    onView( withText( dummyFrom )).check(matches(isDisplayed()));
    onView( withText( dummyDate )).check(matches(isDisplayed()));
    onView( withText( dummyFavorite )).check(matches(isDisplayed()));
    onView( withText( dummySync )).check(matches(isDisplayed()));
    onView( withText( dummyControl )).check(matches(isDisplayed()));
    onView( withText( dummyLock )).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
  }

  @Test
  public void addFragment() {
    DecisionMagniferFragment fragment = new DecisionMagniferFragment();

    activity.runOnUiThread(() -> activity.addFragment(fragment));
    instrumentation.waitForIdleSync();

    View viewById = activity.findViewById(R.id.dialog_magnifer_decision_seekbar_font_size);
    assertThat(viewById, notNullValue());
    assertThat(viewById, instanceOf(SeekBar.class));
  }
}

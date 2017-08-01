package sapotero.rxtest.views.utils;


import android.support.test.espresso.intent.Checks;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class TestUtils {
  public static Matcher<View> withTextColor(final int color) {
    Checks.checkNotNull(color);
    return new BoundedMatcher<View, TextView>(TextView.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText("with text color: ");
      }

      @Override
      public boolean matchesSafely(TextView warning) {
        return color == warning.getCurrentTextColor();
      }
    };
  }
}

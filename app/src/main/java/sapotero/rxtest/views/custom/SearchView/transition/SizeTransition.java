package sapotero.rxtest.views.custom.SearchView.transition;

import android.content.Context;
import android.util.AttributeSet;

import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.TransitionSet;

public class SizeTransition extends TransitionSet {

  public SizeTransition() {
      init();
  }

  public SizeTransition(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    setOrdering(ORDERING_TOGETHER);
    addTransition(new ChangeBounds()).addTransition(new EmptyTransition());
  }
}

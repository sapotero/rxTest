package sapotero.rxtest.views.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ViewPagerFixed extends android.support.v4.view.ViewPager {

  private Boolean swipeable = false;

  public ViewPagerFixed(Context context) {
    super(context);
  }

  public ViewPagerFixed(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setSwipeable(Boolean swipeable) {
    this.swipeable = swipeable;
  }


  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (this.swipeable) {
      return super.onTouchEvent(event);
    }
    return false;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {
    if (this.swipeable) {
      return super.onInterceptTouchEvent(event);
    }

    return false;
  }
}
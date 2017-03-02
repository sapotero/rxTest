package sapotero.rxtest.views.adapters.utils;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.events.view.ShowPrevDocumentEvent;
import timber.log.Timber;

public class OnSwipeTouchListener implements View.OnTouchListener {

  private final GestureDetector gestureDetector;
  private String TAG = this.getClass().getSimpleName();

  public OnSwipeTouchListener(Context ctx){
    gestureDetector = new GestureDetector(ctx, new GestureListener());
  }

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    return gestureDetector.onTouchEvent(event);
  }

  public final class GestureListener extends GestureDetector.SimpleOnGestureListener {

    public static final int SWIPE_THRESHOLD = 400;
    public static final int SWIPE_VELOCITY_THRESHOLD = 400;

    @Override
    public boolean onDown(MotionEvent e) {
      return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      boolean result = false;

      try {

        float diffY = e2.getY() - e1.getY();
        float diffX = e2.getX() - e1.getX();

        if (Math.abs(diffX) > Math.abs(diffY)) {
          if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            if (diffX > 0) {
              onSwipeRight();
            } else {
              onSwipeLeft();
            }
            result = true;
          }
        }
        else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
          if (diffY > 0) {
            onSwipeBottom();
          } else {
            onSwipeTop();
          }

          super.onFling(e1,e2, velocityX,velocityY);

          result = true;
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }

      return result;
    }
  }

  private void onSwipeRight() {
    Timber.tag(TAG).e("onSwipeRight");
    EventBus.getDefault().post( new ShowPrevDocumentEvent());
  }

  private void onSwipeLeft() {
    Timber.tag(TAG).e("onSwipeLeft");
    EventBus.getDefault().post( new ShowNextDocumentEvent());
  }

  private void onSwipeTop() {
    Timber.tag(TAG).e("onSwipeTop");
  }

  private void onSwipeBottom() {
    Timber.tag(TAG).e("onSwipeBottom");
  }
}


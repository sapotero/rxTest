/**
 * Copyright 2016 Bartosz Schiller
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sapotero.rxtest.views.custom.pdf;

import android.graphics.PointF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.events.view.ShowPrevDocumentEvent;
import sapotero.rxtest.views.custom.pdf.scroll.ScrollHandle;
import timber.log.Timber;

import static sapotero.rxtest.views.adapters.utils.OnSwipeTouchListener.GestureListener.SWIPE_THRESHOLD;
import static sapotero.rxtest.views.adapters.utils.OnSwipeTouchListener.GestureListener.SWIPE_VELOCITY_THRESHOLD;
import static sapotero.rxtest.views.custom.pdf.util.Constants.Pinch.MAXIMUM_ZOOM;
import static sapotero.rxtest.views.custom.pdf.util.Constants.Pinch.MINIMUM_ZOOM;

class DragPinchManager implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener {

  private PDFView pdfView;
  private AnimationManager animationManager;

  private GestureDetector gestureDetector;
  private ScaleGestureDetector scaleGestureDetector;

  private boolean isSwipeEnabled;

  private boolean swipeVertical;

  private boolean scrolling = false;
  private boolean scaling = false;
  private String TAG = this.getClass().getSimpleName();

  public DragPinchManager(PDFView pdfView, AnimationManager animationManager) {
      this.pdfView = pdfView;
      this.animationManager = animationManager;
      this.isSwipeEnabled = false;
      this.swipeVertical = pdfView.isSwipeVertical();
      gestureDetector = new GestureDetector(pdfView.getContext(), this);
      scaleGestureDetector = new ScaleGestureDetector(pdfView.getContext(), this);
      pdfView.setOnTouchListener(this);
  }

  public void enableDoubletap(boolean enableDoubletap) {
      if (enableDoubletap) {
          gestureDetector.setOnDoubleTapListener(this);
      } else {
          gestureDetector.setOnDoubleTapListener(null);
      }
  }

  public boolean isZooming() {
      return pdfView.isZooming();
  }

  private boolean isPageChange(float distance) {
      return Math.abs(distance) > Math.abs(pdfView.toCurrentScale(swipeVertical ? pdfView.getOptimalPageHeight() : pdfView.getOptimalPageWidth()) / 2);
  }

  public void setSwipeEnabled(boolean isSwipeEnabled) {
      this.isSwipeEnabled = isSwipeEnabled;
  }

  public void setSwipeVertical(boolean swipeVertical) {
      this.swipeVertical = swipeVertical;
  }

  @Override
  public boolean onSingleTapConfirmed(MotionEvent e) {
      ScrollHandle ps = pdfView.getScrollHandle();
      if (ps != null && !pdfView.documentFitsView()) {
          if (!ps.shown()) {
              ps.show();
          } else {
              ps.hide();
          }
      }
      pdfView.performClick();
      return true;
  }

  @Override
  public boolean onDoubleTap(MotionEvent e) {
      if (pdfView.getZoom() < pdfView.getMidZoom()) {
          pdfView.zoomWithAnimation(e.getX(), e.getY(), pdfView.getMidZoom());
      } else if (pdfView.getZoom() < pdfView.getMaxZoom()) {
          pdfView.zoomWithAnimation(e.getX(), e.getY(), pdfView.getMaxZoom());
      } else {
          pdfView.resetZoomWithAnimation();
      }
      return true;
  }

  @Override
  public boolean onDoubleTapEvent(MotionEvent e) {
      return false;
  }

  @Override
  public boolean onDown(MotionEvent e) {
      animationManager.stopFling();
      return true;
  }

  @Override
  public void onShowPress(MotionEvent e) {

  }

  @Override
  public boolean onSingleTapUp(MotionEvent e) {
      return false;
  }

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      scrolling = true;
      if (isZooming() || isSwipeEnabled) {
          pdfView.moveRelativeTo(-distanceX, -distanceY);
      }
      if (!scaling || pdfView.doRenderDuringScale()) {
        pdfView.loadPageByOffset();
      }
      return true;
  }

  public void onScrollEnd(MotionEvent event) {
      pdfView.loadPages();
      hideHandle();
  }

  @Override
  public void onLongPress(MotionEvent e) {

  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      int xOffset = (int) pdfView.getCurrentXOffset();
      int yOffset = (int) pdfView.getCurrentYOffset();

      float diffY = e2.getY() - e1.getY();
      float diffX = e2.getX() - e1.getX();

      if (Math.abs(diffX) > Math.abs(diffY)) {
          if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
              if (diffX > 0) {
                  onSwipeRight();
              } else {
                  onSwipeLeft();
              }
          }
      }
      else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
          if (diffY > 0) {
              onSwipeBottom();
          } else {
              onSwipeTop();
          }
      }

      animationManager.startFlingAnimation(xOffset,
              yOffset, (int) (velocityX),
              (int) (velocityY),
              xOffset * (swipeVertical ? 2 : pdfView.getPageCount()), 0,
              yOffset * (swipeVertical ? pdfView.getPageCount() : 2), 0);

      return true;
  }

  @Override
  public boolean onScale(ScaleGestureDetector detector) {
      float dr = detector.getScaleFactor();
      float wantedZoom = pdfView.getZoom() * dr;
      if (wantedZoom < MINIMUM_ZOOM) {
          dr = MINIMUM_ZOOM / pdfView.getZoom();
      } else if (wantedZoom > MAXIMUM_ZOOM) {
          dr = MAXIMUM_ZOOM / pdfView.getZoom();
      }
      pdfView.zoomCenteredRelativeTo(dr, new PointF(detector.getFocusX(), detector.getFocusY()));
      return true;
  }

  @Override
  public boolean onScaleBegin(ScaleGestureDetector detector) {
      scaling = true;
      return true;
  }

  @Override
  public void onScaleEnd(ScaleGestureDetector detector) {
    pdfView.loadPages();
    hideHandle();
    scaling = false;
  }

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    boolean retVal = scaleGestureDetector.onTouchEvent(event);
    retVal = gestureDetector.onTouchEvent(event) || retVal;

    if (event.getAction() == MotionEvent.ACTION_UP) {
        if (scrolling) {
            scrolling = false;
            onScrollEnd(event);
        }
    }
    return retVal;
  }

  private void hideHandle() {
    if (pdfView.getScrollHandle() != null && pdfView.getScrollHandle().shown()) {
        pdfView.getScrollHandle().hideDelayed();
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

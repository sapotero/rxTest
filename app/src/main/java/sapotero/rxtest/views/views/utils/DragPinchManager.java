package sapotero.rxtest.views.views.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Spinner;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.views.activities.DocumentImageFullScreenActivity;
import sapotero.rxtest.views.adapters.DocumentLinkAdapter;
import timber.log.Timber;

import static com.github.barteksc.pdfviewer.util.Constants.Pinch.MAXIMUM_ZOOM;
import static com.github.barteksc.pdfviewer.util.Constants.Pinch.MINIMUM_ZOOM;

public class DragPinchManager implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener {

  private Spinner spinner;
  private final Context context;
  private DocumentLinkAdapter adapter;
  private PDFView pdfView;
  private AnimationManager animationManager;

  private GestureDetector gestureDetector;
  private ScaleGestureDetector scaleGestureDetector;

  private boolean isSwipeEnabled;

  private boolean swipeVertical;

  private boolean scrolling = false;

  public DragPinchManager(Context context, PDFView pdfView, AnimationManager animationManager, Spinner mDocumentList) {
    this.context = context;
    this.pdfView = pdfView;
    this.animationManager = animationManager;
    this.isSwipeEnabled = false;
    this.swipeVertical = pdfView.isSwipeVertical();
    gestureDetector = new GestureDetector(pdfView.getContext(), this);
    scaleGestureDetector = new ScaleGestureDetector(pdfView.getContext(), this);
    pdfView.setOnTouchListener(this);

    this.spinner = mDocumentList;
  }

  public DragPinchManager(Context context, PDFView pdfView, AnimationManager animationManager, DocumentLinkAdapter adapter) {
    this.context = context;
    this.pdfView = pdfView;
    this.animationManager = animationManager;
    this.isSwipeEnabled = false;
    this.swipeVertical = pdfView.isSwipeVertical();
    gestureDetector = new GestureDetector(pdfView.getContext(), this);
    scaleGestureDetector = new ScaleGestureDetector(pdfView.getContext(), this);
    pdfView.setOnTouchListener(this);

    this.adapter = adapter;
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
//    ScrollHandle ps = pdfView.getScrollHandle();
//    if (ps != null && !pdfView.documentFitsView()) {
//      if (!ps.shown()) {
//        ps.show();
//      } else {
//        ps.hide();
//      }
//    }
    pdfView.performClick();
    return true;
  }

  @Override
  public boolean onDoubleTap(MotionEvent e) {

    Timber.tag("gestureDetector").i("filename: %s", e.toString());

    DocumentLinkAdapter _adapter = adapter;

    if (spinner!= null && spinner.getAdapter() != null ) {
      _adapter = (DocumentLinkAdapter) spinner.getAdapter();
    }

    if (adapter.getCount() > 0){

      Type listType = new TypeToken<ArrayList<Image>>() {}.getType();

      Intent intent = new Intent( context, DocumentImageFullScreenActivity.class);
      intent.putExtra( "files", new Gson().toJson( adapter.getItems(), listType ) );
      intent.putExtra( "index", 0 );
      context.startActivity(intent);
    }

    return false;
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

    return true;
  }

  public void onScrollEnd(MotionEvent event) {
    pdfView.loadPages();
  }

  @Override
  public void onLongPress(MotionEvent e) {

  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    int xOffset = (int) pdfView.getCurrentXOffset();
    int yOffset = (int) pdfView.getCurrentYOffset();
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
    return true;
  }

  @Override
  public void onScaleEnd(ScaleGestureDetector detector) {
    pdfView.loadPages();
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

}

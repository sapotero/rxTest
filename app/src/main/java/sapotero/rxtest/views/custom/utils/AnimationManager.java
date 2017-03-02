package sapotero.rxtest.views.custom.utils;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import sapotero.rxtest.views.custom.pdf.PDFView;

public class AnimationManager {

  private PDFView pdfView;

  private ValueAnimator animation;

  private Scroller scroller;

  private ValueAnimator flingAnimation;

  public AnimationManager(PDFView pdfView) {
    this.pdfView = pdfView;
    scroller = new Scroller(pdfView.getContext(), null, true);
  }

  public void startXAnimation(float xFrom, float xTo) {
    stopAll();
    animation = ValueAnimator.ofFloat(xFrom, xTo);
    animation.setInterpolator(new DecelerateInterpolator());
    animation.addUpdateListener(new XAnimation());
    animation.setDuration(400);
    animation.start();
  }

  public void startYAnimation(float yFrom, float yTo) {
    stopAll();
    animation = ValueAnimator.ofFloat(yFrom, yTo);
    animation.setInterpolator(new DecelerateInterpolator());
    animation.addUpdateListener(new YAnimation());
    animation.setDuration(400);
    animation.start();
  }

  public void startZoomAnimation(float centerX, float centerY, float zoomFrom, float zoomTo) {
    stopAll();
    animation = ValueAnimator.ofFloat(zoomFrom, zoomTo);
    animation.setInterpolator(new DecelerateInterpolator());
    ZoomAnimation zoomAnim = new ZoomAnimation(centerX, centerY);
    animation.addUpdateListener(zoomAnim);
    animation.addListener(zoomAnim);
    animation.setDuration(400);
    animation.start();
  }

  public void startFlingAnimation(int startX, int startY, int velocityX, int velocityY, int minX, int maxX, int minY, int maxY) {
    stopAll();
    flingAnimation = ValueAnimator.ofFloat(0, 1);
    scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
    flingAnimation.setDuration(scroller.getDuration());
    flingAnimation.start();
  }

  public void stopAll() {
    if (animation != null) {
      animation.cancel();
      animation = null;
    }
    stopFling();
  }

  public void stopFling() {
    if (flingAnimation != null) {
      scroller.forceFinished(true);
      flingAnimation.cancel();
      flingAnimation = null;
    }
  }

  class XAnimation implements ValueAnimator.AnimatorUpdateListener {

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
      float offset = (Float) animation.getAnimatedValue();
      pdfView.moveTo(offset, pdfView.getCurrentYOffset());
    }

  }

  class YAnimation implements ValueAnimator.AnimatorUpdateListener {

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
      float offset = (Float) animation.getAnimatedValue();
      pdfView.moveTo(pdfView.getCurrentXOffset(), offset);
    }

  }

  class ZoomAnimation implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    private final float centerX;
    private final float centerY;

    public ZoomAnimation(float centerX, float centerY) {
      this.centerX = centerX;
      this.centerY = centerY;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
      float zoom = (Float) animation.getAnimatedValue();
      pdfView.zoomCenteredTo(zoom, new PointF(centerX, centerY));
    }

    @Override
    public void onAnimationCancel(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
      pdfView.loadPages();
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

  }


}
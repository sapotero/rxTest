package sapotero.rxtest.views.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import sapotero.rxtest.R;

public class CircleLeftArrow extends View {

  private Context mContext;

  private int viewColor;
  private int backgroundColor;

  private int normalColor = 0;
  private int normalBackground = 0;
  private int touchBackground = 0;
  private int touchColor = 0;
  private int disabledBackground = 0;
  private int disabledColor = 0;

  private int measuredSize;
  private int strokeWidth;
  private boolean touched = false;
  private float startX, startY, endX, endY;
  private OnClickListener listener;

  private Paint mCirclePiant, mArrowPaint;

  public CircleLeftArrow(Context context) {
    super(context);
    mContext = context;
    init(context, null, 0);
    setInitialColor();
  }

  public CircleLeftArrow(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, 0);
    setInitialColor();
  }

  public CircleLeftArrow(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs, defStyleAttr);
    setInitialColor();
  }

  private void init(Context context, AttributeSet attributeSet, int defStyle) {
    mCirclePiant = new Paint(Paint.ANTI_ALIAS_FLAG);
    mCirclePiant.setColor(viewColor);
    mCirclePiant.setStyle(Paint.Style.STROKE);

    mArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mArrowPaint.setColor(viewColor);
    mArrowPaint.setStyle(Paint.Style.STROKE);
    mArrowPaint.setStrokeCap(Paint.Cap.ROUND);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int measuredHeight = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
    int measuredWidth = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);

    measuredSize = Math.min(measuredHeight, measuredWidth);
    strokeWidth = Math.round(measuredSize * 0.025f);
    mCirclePiant.setStrokeWidth(strokeWidth);
    mArrowPaint.setStrokeWidth(strokeWidth);
    // Make a square
    setMeasuredDimension(measuredSize + strokeWidth, measuredSize + strokeWidth);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (measuredSize <= 0) {
      // Not much we can draw, can we
      return;
    }

    float center = (measuredSize + strokeWidth) * 0.5f;
    canvas.drawCircle(center, center, measuredSize * 0.5f, mCirclePiant);

    canvas.drawLine(
      center - 0.2f * measuredSize,
      center,
      center + 0.1f * measuredSize,
      center + 0.2f * measuredSize, mArrowPaint);
    canvas.drawLine(
      center - 0.2f * measuredSize,
      center,
      center + 0.1f * measuredSize,
      center - 0.2f * measuredSize, mArrowPaint);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    boolean result = super.onTouchEvent(event);
    switch (event.getAction()){
      case MotionEvent.ACTION_DOWN :
        touched = true;
        startX = event.getX();
        startY = event.getY();
        togglePaintColor(event);
        postInvalidate();
        return true;
      case MotionEvent.ACTION_UP :
        endX = event.getX();
        endY = event.getY();
        togglePaintColor(event);
        postInvalidate();
        float diffX = Math.abs(startX - endX);
        float diffY = Math.abs(startY - endY);

        if (diffX <= 5 && diffY <= 5 && touched ) {
          dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,1));
        }
        return true;
      default:
        return false;
    }
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if(event.getAction() == KeyEvent.ACTION_UP) {
      touched = false;
      if(listener != null) listener.onClick(this);
    }
    return super.dispatchKeyEvent(event);
  }

  public void setListener(OnClickListener listener) {
    this.listener = listener;
  }

  public void setInitialColor(){
    if (isClickable()){
      viewColor = normalColor;
      backgroundColor = normalBackground;
    }
    else {
      viewColor = disabledColor;
      backgroundColor = disabledBackground;
      this.listener = null;
    }

    normalColor = ContextCompat.getColor(getContext(), R.color.md_blue_600);
    normalBackground = ContextCompat.getColor(getContext(), R.color.md_grey_50);
    touchBackground = ContextCompat.getColor(getContext(), R.color.md_blue_A100);
    touchColor = ContextCompat.getColor(getContext(), R.color.md_blue_800);
    disabledBackground =  ContextCompat.getColor(getContext(), R.color.md_grey_200);
    disabledColor = ContextCompat.getColor(getContext(), R.color.md_grey_600);

    mCirclePiant.setColor(normalColor);
    mCirclePiant.setStyle(Paint.Style.STROKE);
    mArrowPaint.setColor(normalColor);
  }

  @Override
  public void setClickable(boolean isClickable){
    super.setClickable(isClickable);
    if (isClickable){
      viewColor = normalColor;
      backgroundColor = normalBackground;
    }
    else {
      viewColor = disabledColor;
      backgroundColor = disabledBackground;
      this.listener = null;
    }
    mCirclePiant.setColor(viewColor);
    mCirclePiant.setStyle(Paint.Style.STROKE);
    mArrowPaint.setColor(viewColor);
    postInvalidate();
  }

  private void togglePaintColor(MotionEvent event) {
    if (isClickable()) {
      if (event.getAction() == MotionEvent.ACTION_DOWN) {
        viewColor = touchColor;
        backgroundColor = touchBackground;
        mCirclePiant.setColor(backgroundColor);
        mCirclePiant.setStyle(Paint.Style.FILL);
        mArrowPaint.setColor(viewColor);

      } else if (event.getAction() == MotionEvent.ACTION_UP) {
        viewColor = normalColor;
        backgroundColor = normalBackground;
        mCirclePiant.setColor(viewColor);
        mCirclePiant.setStyle(Paint.Style.STROKE);
        mArrowPaint.setColor(viewColor);
      }
    }
  }
}
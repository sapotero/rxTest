package sapotero.rxtest.utils.rxbinding;

import android.view.View;

import com.jakewharton.rxbinding.view.RxView;

import java.util.concurrent.TimeUnit;

import rx.Subscription;

public class Bind {
  // Time period during which multiple clicks will be ignored
  private static final int MULTIPLE_CLICK_IGNORE_WINDOW = 1000;

  public interface OnClickListener {
    void onClick();
  }

  public static Subscription click(View view, OnClickListener onClickListener) {
    return RxView
      .clicks( view )
      .throttleFirst( MULTIPLE_CLICK_IGNORE_WINDOW, TimeUnit.MILLISECONDS )
      .subscribe(click -> {
        if ( onClickListener != null ) {
          onClickListener.onClick();
        }
      });
  }
}

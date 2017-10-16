package sapotero.rxtest.utils.rxbinding;

import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;

import java.util.concurrent.TimeUnit;

import rx.Subscription;

public class Bind {
  // Time period during which multiple clicks will be ignored
  private static final int MULTIPLE_CLICK_IGNORE_WINDOW = 1000;

  public interface OnClickListener {
    void onClick();
  }

  public interface OnMenuItemClickListener {
    void onClick(MenuItem item);
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

  public static Subscription menuItemClick(Toolbar toolbar, OnMenuItemClickListener onMenuItemClickListener) {
    return RxToolbar
      .itemClicks( toolbar )
      .throttleFirst( MULTIPLE_CLICK_IGNORE_WINDOW, TimeUnit.MILLISECONDS )
      .subscribe(item -> {
        if ( onMenuItemClickListener != null ) {
          onMenuItemClickListener.onClick( item );
        }
      });
  }

  public static Subscription menuNavigationClick(Toolbar toolbar, OnClickListener onClickListener) {
    return RxToolbar
      .navigationClicks( toolbar )
      .throttleFirst( MULTIPLE_CLICK_IGNORE_WINDOW, TimeUnit.MILLISECONDS )
      .subscribe(click -> {
        if ( onClickListener != null ) {
          onClickListener.onClick();
        }
      });
  }
}

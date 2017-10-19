package sapotero.rxtest.utils.click;

import android.support.v7.widget.Toolbar;
import android.view.View;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.support.v7.widget.RxToolbar;

import java.util.concurrent.TimeUnit;

import rx.Subscription;

// Prevents multiple clicks on one view
public class Bind extends Click {
  public static Subscription click(View view, OnClickListener onClickListener) {
    return RxView
      .clicks( view )
      .throttleFirst( MIN_TIME_BETWEEN_CLICKS, TimeUnit.MILLISECONDS )
      .subscribe(click -> {
        if ( onClickListener != null ) {
          onClickListener.onClick();
        }
      });
  }

  public static Subscription menuItemClick(Toolbar toolbar, OnMenuItemClickListener onMenuItemClickListener) {
    return RxToolbar
      .itemClicks( toolbar )
      .throttleFirst( MIN_TIME_BETWEEN_CLICKS, TimeUnit.MILLISECONDS )
      .subscribe(item -> {
        if ( onMenuItemClickListener != null ) {
          onMenuItemClickListener.onClick( item );
        }
      });
  }

  public static Subscription menuNavigationClick(Toolbar toolbar, OnClickListener onClickListener) {
    return RxToolbar
      .navigationClicks( toolbar )
      .throttleFirst( MIN_TIME_BETWEEN_CLICKS, TimeUnit.MILLISECONDS )
      .subscribe(click -> {
        if ( onClickListener != null ) {
          onClickListener.onClick();
        }
      });
  }
}

package sapotero.rxtest.views.custom;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

import sapotero.rxtest.views.adapters.OshsAutoCompleteAdapter;

public class DelayAutoCompleteTextView extends AutoCompleteTextView {

  private static final int MESSAGE_TEXT_CHANGED = 100;
  private static final int DEFAULT_AUTOCOMPLETE_DELAY = 1500;

  private int mAutoCompleteDelay = DEFAULT_AUTOCOMPLETE_DELAY;
  private ProgressBar mLoadingIndicator;

  private final Handler mHandler = new Handler(){
    @Override
    public void handleMessage(Message msg) {
      OshsAutoCompleteAdapter adapter = (OshsAutoCompleteAdapter) DelayAutoCompleteTextView.super.getAdapter();

      if (adapter != null && adapter.getFilter() != null){
        DelayAutoCompleteTextView.super.performFiltering((CharSequence) msg.obj, msg.arg1);
      } else {
        if (mLoadingIndicator != null) {
          mLoadingIndicator.setVisibility(View.GONE);
        }
      }
    }
  };

  public DelayAutoCompleteTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void setLoadingIndicator(ProgressBar progressBar) {
    mLoadingIndicator = progressBar;
  }

  public void setAutoCompleteDelay(int autoCompleteDelay) {
    mAutoCompleteDelay = autoCompleteDelay;
  }

  public void filter(CharSequence text) {
    performFiltering(text, 0);
  }

  @Override
  protected void performFiltering(CharSequence text, int keyCode) {
    if (isFocused()) {
      if (mLoadingIndicator != null) {
        mLoadingIndicator.setVisibility(View.VISIBLE);
      }
      mHandler.removeMessages(MESSAGE_TEXT_CHANGED);
      mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_TEXT_CHANGED, text), mAutoCompleteDelay);
    }
  }

  @Override
  public void onFilterComplete(int count) {
    if (mLoadingIndicator != null) {
      mLoadingIndicator.setVisibility(View.GONE);
    }
    super.onFilterComplete(count);
  }

  @Override
  protected void replaceText(CharSequence text) {
    Editable currentText = getText();
    super.replaceText(currentText);
  }

  public void hideIndicator() {
    if (mLoadingIndicator != null) {
      mLoadingIndicator.setVisibility(View.GONE);
    }
  }
}
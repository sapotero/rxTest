package sapotero.rxtest.views.custom.spinner;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import sapotero.rxtest.R;
import sapotero.rxtest.views.adapters.spinner.JournalSelectorAdapter;
import timber.log.Timber;

public class JournalSelectorView extends AppCompatTextView implements View.OnClickListener {
  private String TAG = this.getClass().getSimpleName();

  public JournalSelectorView(Context context) {
    super(context);
    setOnClickListener(this);
  }

  public JournalSelectorView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setOnClickListener(this);
  }

  public JournalSelectorView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    Timber.tag(TAG).w("clicked!");

    JournalSelectorAdapter adapter = new JournalSelectorAdapter();

    new MaterialDialog.Builder( getContext() )
      .title(R.string.container_title)
      .adapter(adapter, null)
      .itemsCallbackSingleChoice(-1, (dialog, view, which, text) -> {
        updateView( String.valueOf(text) );
        return true;
      })
      .alwaysCallSingleChoiceCallback()
      .show();

  }

  private void updateView(String text){
    setText(text);
  }


}
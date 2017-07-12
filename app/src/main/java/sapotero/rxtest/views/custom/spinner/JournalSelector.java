package sapotero.rxtest.views.custom.spinner;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

import timber.log.Timber;


public class JournalSelector extends AppCompatTextView implements View.OnClickListener {

  public JournalSelector(Context context) {
    super(context);
    setOnClickListener(this);
  }


  @Override
  public void onClick(View view) {
    Timber.e("OnClickListener");
  }
}

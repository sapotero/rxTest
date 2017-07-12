package sapotero.rxtest.views.custom.spinner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import sapotero.rxtest.R;
import timber.log.Timber;

import static com.googlecode.totallylazy.Sequences.sequence;

/**
 * Created by ANKIT
 */
@SuppressLint("AppCompatCustomView")
public class JournalSelectorView extends TextView implements View.OnClickListener {

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

  public JournalSelectorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    setOnClickListener(this);
  }


  @Override
  public void onClick(View v) {
    Timber.tag(TAG).w("clicked!");


    List<String> journal = sequence(1, 2, 3, 4, 5, 6)
      .map(i -> "Название журнала: " + i)
      .toList();

    new MaterialDialog.Builder( getContext() )
      .title(R.string.container_title)
      .items(journal)
      .itemsCallbackSingleChoice(-1, (dialog, view, which, text) -> {
        updateView(String.valueOf(text));
        return true;
      })
      .show();

  }

  private void updateView(String text){
    setText(text);
  }
}
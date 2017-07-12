package sapotero.rxtest.views.custom.spinner;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.views.menu.fields.MainMenuItem;
import timber.log.Timber;

import static com.googlecode.totallylazy.Sequences.sequence;

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

//    JournalSelectorAdapter adapter = new JournalSelectorAdapter(getContext(), Arrays.asList(MainMenuItem.values()));
//
//    new MaterialDialog.Builder( getContext() )
//      .title(R.string.container_title)
//      .adapter(adapter, null)
//      .itemsCallbackSingleChoice(-1, (dialog, view, which, text) -> {
//        updateView( String.valueOf(text) );
//        return true;
//      })
//      .alwaysCallSingleChoiceCallback()
//      .show();

    List<String> plain_list = sequence( MainMenuItem.values() ).map(MainMenuItem::getName).toList();
    new MaterialDialog.Builder( getContext() )
      .title(R.string.container_title)
      .items(plain_list)
      .itemsCallbackSingleChoice(-1, (dialog, view, which, text) -> {
        updateView( String.valueOf(text) );
        return true;
      })
      .show();


  }

  private void updateView(String text){
    setText(text);
  }
}
package sapotero.rxtest.views.custom.spinner;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;

import org.greenrobot.eventbus.EventBus;

import sapotero.rxtest.events.adapter.JournalSelectorIndexEvent;
import sapotero.rxtest.views.adapters.spinner.JournalSelectorAdapter;
import timber.log.Timber;

public class JournalSelectorView extends AppCompatTextView implements View.OnClickListener {
  private String TAG = this.getClass().getSimpleName();

  private JournalSelectorAdapter adapter;
  private MaterialDialog dialog;
  private int position = -1;

  public JournalSelectorView(Context context) {
    super(context);
    build();
  }

  public JournalSelectorView(Context context, AttributeSet attrs) {
    super(context, attrs);
    build();
  }

  public JournalSelectorView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    build();
  }

  private void build(){
    setOnClickListener(this);
    adapter = new JournalSelectorAdapter();
    setText( adapter.setDefault() );
  }


  @Override
  public void onClick(View v) {
    adapter = new JournalSelectorAdapter();
    adapter.setCallback(this::updateView);

    dialog = new MaterialDialog.Builder( getContext() )
      .adapter(adapter, null)
      .alwaysCallSingleChoiceCallback()
      .autoDismiss(true)
      .build();
    dialog.show();

    Window window = dialog.getWindow();
    assert window != null;
    WindowManager.LayoutParams layoutParams = window.getAttributes();
    layoutParams.gravity = Gravity.START | Gravity.TOP;
    layoutParams.x = 30;
    layoutParams.y = 172;
    layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
    window.setAttributes(layoutParams);
  }

  private void updateView(int position){
    dialog.dismiss();

    try {
      this.position = position;
      setText( adapter.getItem(position) );
    } catch (Exception e) {
      Timber.e(e);
      Timber.e("position: %s", position);
    }

    EventBus.getDefault().post( new JournalSelectorIndexEvent( adapter.getItemPosition(position) ) );
  }

  public void selectJournal(int position) {
    try {
      Timber.e("selectJournal  %s - %s", position, adapter.getItem(position));
      this.position = position;
      setText( adapter.getItem(position) );
    } catch (Exception e) {
      Timber.e(e);
    }
  }

  public void updateCounter() {
    Timber.e("updateCounter %s", position);
    if (position != -1) {
      selectJournal(position);
    }
  }
}
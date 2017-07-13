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

public class JournalSelectorView extends AppCompatTextView implements View.OnClickListener {
  private String TAG = this.getClass().getSimpleName();

  private JournalSelectorAdapter adapter;
  private MaterialDialog dialog;

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
    adapter.setCallback(this::updateView);

    setText( adapter.setDefault() );
  }


  @Override
  public void onClick(View v) {
    dialog = new MaterialDialog.Builder( getContext() )
      .adapter(adapter, null)
      .alwaysCallSingleChoiceCallback()
      .autoDismiss(true)
      .build();
    dialog.show();

    Window window = dialog.getWindow();
    WindowManager.LayoutParams layoutParams = window.getAttributes();
    layoutParams.gravity = Gravity.START | Gravity.CENTER_HORIZONTAL;
    layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
    window.setAttributes(layoutParams);
  }

  private void updateView(int position){
    dialog.dismiss();
    setText( adapter.getItem(position) );
    EventBus.getDefault().post( new JournalSelectorIndexEvent(position) );
  }

}
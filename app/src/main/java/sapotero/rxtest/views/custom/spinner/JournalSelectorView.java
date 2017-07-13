package sapotero.rxtest.views.custom.spinner;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import sapotero.rxtest.R;
import sapotero.rxtest.events.adapter.JournalSelectorIndexEvent;
import sapotero.rxtest.views.adapters.spinner.JournalSelectorAdapter;
import timber.log.Timber;

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


    Observable
      .just(true)
      .delay(1500, TimeUnit.MILLISECONDS)
      .subscribe(
        data -> {
          Timber.e("+++!!");
          JournalSelectorAdapter adapter = new JournalSelectorAdapter();
          setText( adapter.setDefault() );
        }, Timber::e
      );
  }


  @Override
  public void onClick(View v) {
    Timber.tag(TAG).w("clicked!");

    adapter = new JournalSelectorAdapter();
    adapter.setCallback(this::updateView);

    dialog = new MaterialDialog.Builder( getContext() )
      .title(R.string.container_title)
      .adapter(adapter, null)
      .alwaysCallSingleChoiceCallback()
      .autoDismiss(true)
      .build();
    dialog.show();


  }

  private void updateView(int position){
    dialog.dismiss();
    setText( adapter.getItem(position) );
    EventBus.getDefault().post( new JournalSelectorIndexEvent(position) );
  }

}
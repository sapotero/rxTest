package sapotero.rxtest.views.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sapotero.rxtest.R;
import timber.log.Timber;

public class RejectDecisionFragment extends DialogFragment implements View.OnClickListener {

  private String TAG = this.getClass().getSimpleName();

  @BindView(R.id.dialog_reject_decision_button_cancel)  Button button_cancel;
  @BindView(R.id.dialog_reject_decision_button_yes) Button button_ok;

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    getDialog().setTitle("Title!");
    View view = inflater.inflate(R.layout.dialog_reject_decision, null);

    getDialog().getWindow().setBackgroundDrawableResource(R.drawable.corner_background);

    ButterKnife.bind(this, view);

    return view;
  }

  @OnClick(R.id.dialog_reject_decision_button_cancel)
  public void _cancel(View view) {
    Timber.tag(TAG).i( "_cancel");
  }

  @OnClick(R.id.dialog_reject_decision_button_yes)
  public void _yes(View view) {
    Timber.tag(TAG).i( "_yes");
  }

  public void onClick(DialogInterface dialog, int which) {
    int i = 0;

    switch (which) {
      case Dialog.BUTTON_POSITIVE:
        i = R.string.dialog_oshs_add;
        break;
      case Dialog.BUTTON_NEGATIVE:
        i = R.string.dialog_oshs_cancel;
        break;
      default:
        i = R.string.dialog_oshs_cancel;
        break;
    }

    Timber.tag(TAG).i(String.valueOf(which));
  }

  public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    Timber.tag(TAG).i( "onDismiss");
  }

  public void onCancel(DialogInterface dialog) {
    super.onCancel(dialog);
    Timber.tag(TAG).i( "onCancel");
  }

  @Override
  public void onClick(View v) {

  }
}

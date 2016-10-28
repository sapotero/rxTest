package sapotero.rxtest.views.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.views.adapters.OshsAutoCompleteAdapter;
import sapotero.rxtest.views.views.DelayAutoCompleteTextView;
import timber.log.Timber;

public class SelectOshsDialogFragment extends DialogFragment implements View.OnClickListener {
  
  private String TAG = this.getClass().getSimpleName();

  @BindView(R.id.et_book_title) DelayAutoCompleteTextView bookTitle;
  @BindView(R.id.pb_loading_indicator) ProgressBar indicator;

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    getDialog().setTitle("Title!");
    View view = inflater.inflate(R.layout.dialog_choose_oshs, null);
    view.findViewById(R.id.dialog_oshs_add).setOnClickListener(this);
    view.findViewById(R.id.dialog_oshs_cancel).setOnClickListener(this);

    ButterKnife.bind(this, view);

    bookTitle.setThreshold(2);
    bookTitle.setAdapter(new OshsAutoCompleteAdapter(getActivity()));
    bookTitle.setLoadingIndicator( indicator );
    bookTitle.setOnItemClickListener(
      (adapterView, view1, position, id) -> {
        Oshs book = (Oshs) adapterView.getItemAtPosition(position);
        bookTitle.setText(book.getFirstName());
      }
    );
    return view;
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

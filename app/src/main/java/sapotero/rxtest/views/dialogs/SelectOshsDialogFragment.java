package sapotero.rxtest.views.dialogs;

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

  @BindView(R.id.user_autocomplete_field) DelayAutoCompleteTextView title;
  @BindView(R.id.pb_loading_indicator) ProgressBar indicator;

  SelectOshsDialogFragment.Callback callback;

  public interface Callback {
    void onSearchSuccess(Oshs user);
    void onSearchError(Throwable error);
  }
  public void registerCallBack(SelectOshsDialogFragment.Callback callback){
    this.callback = callback;
  }


  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    getDialog().setTitle("Title!");

    View view = inflater.inflate(R.layout.dialog_choose_oshs, null);
    view.findViewById(R.id.dialog_oshs_add).setOnClickListener(this);
    view.findViewById(R.id.dialog_oshs_cancel).setOnClickListener(this);


    ButterKnife.bind(this, view);

    title.setText("");

    title.setThreshold(2);
    title.setAdapter( new OshsAutoCompleteAdapter(getActivity()) );
    title.setLoadingIndicator( indicator );



    title.setOnItemClickListener(
      (adapterView, view1, position, id) -> {
        Oshs user = (Oshs) adapterView.getItemAtPosition(position);
        title.setText("");
        callback.onSearchSuccess( user );
        dismiss();
      }
    );
    return view;
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

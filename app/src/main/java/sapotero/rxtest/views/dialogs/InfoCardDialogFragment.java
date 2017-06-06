package sapotero.rxtest.views.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.utils.Settings;
import timber.log.Timber;

public class InfoCardDialogFragment extends DialogFragment implements View.OnClickListener {

  private String TAG = this.getClass().getSimpleName();

  @Inject Settings settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  @BindView(R.id.fragment_preview_main_infocard) WebView infocard;
  private String uid;

  @RequiresApi(api = Build.VERSION_CODES.M)
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_preview_main_infocard, null);
    EsdApplication.getDataComponent().inject(this);
    ButterKnife.bind(view);

    loadSettings();
    return view;
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  private void loadSettings() {
    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq( uid == null? settings.getUid() : uid  ))
      .get()
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(document -> {
        try {
          String htmlData = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + new String(Base64.decode( document.getInfoCard(), Base64.DEFAULT));

          infocard = (WebView) getView().findViewById(R.id.fragment_preview_main_infocard);
          infocard.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null);
          infocard.getSettings().setBuiltInZoomControls(true);
          infocard.getSettings().setDisplayZoomControls(false);
        } catch (Exception e) {
          e.printStackTrace();
        }

      }, Timber::e);

  }

  @OnClick(R.id.dialog_reject_decision_button_cancel)
  public void _cancel(View view) {
    Timber.tag(TAG).i( "_cancel");
    dismiss();
  }

  @OnClick(R.id.dialog_reject_decision_button_yes)
  public void _yes(View view) {
    Timber.tag(TAG).i( "_yes");
    dismiss();
  }

  public InfoCardDialogFragment withUid(String uid) {
    this.uid = uid;
    return this;
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

package sapotero.rxtest.views.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.views.adapters.TabPagerAdapter;
import sapotero.rxtest.views.custom.ViewPagerFixed;
import timber.log.Timber;

public class InfoCardDialogFragment extends DialogFragment {

  private String TAG = this.getClass().getSimpleName();

  @Inject ISettings settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  @BindView(R.id.fragment_preview_main_infocard_close) Button close;
  @BindView(R.id.fragment_preview_tab_main) ViewPagerFixed viewPager;
  @BindView(R.id.fragment_preview_tab_tabs) TabLayout tabLayout;

  //  @BindView(R.id.fragment_preview_main_infocard) WebView infocard;

  private DismissListener dismissListener;

  public interface DismissListener {
    void onDismiss();
  }

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_preview_main_infocard, container, false);
    ButterKnife.bind(this, view);

    EsdApplication.getDataComponent().inject(this);

//    infocard  = (WebView) view.findViewById(R.id.fragment_preview_main_infocard);
//    viewPager = (ViewPager) view.findViewById(R.id.fragment_preview_tab_main);

    loadSettings();
    return view;
  }

  @OnClick(R.id.fragment_preview_main_infocard_close)
  public void close(){
    dismiss();
  }

  private void loadSettings() {

//    RDocumentEntity document = dataStore
//      .select(RDocumentEntity.class)
//      .where(RDocumentEntity.UID.eq(settings.getUid()))
//      .get().firstOrNull();
//
//    String htmlData = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + new String(Base64.decode( document.getInfoCard(), Base64.DEFAULT));
//
//    infocard.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null);
//    infocard.getSettings().setBuiltInZoomControls(true);
//    infocard.getSettings().setDisplayZoomControls(false);

    if (viewPager != null) {

      TabPagerAdapter adapter = new TabPagerAdapter ( getChildFragmentManager() );
      adapter.withUid( settings.getUid()  );
      adapter.withoutZoom(true);

      viewPager.setAdapter(adapter);
      tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
      tabLayout.setupWithViewPager(viewPager);
    }


  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    Timber.tag(TAG).i( "onDismiss");

    if ( dismissListener != null ) {
      dismissListener.onDismiss();
    }
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    super.onCancel(dialog);
    Timber.tag(TAG).i( "onCancel");
  }

  public void dismissListener(DismissListener dismissListener) {
    this.dismissListener = dismissListener;
  }
}

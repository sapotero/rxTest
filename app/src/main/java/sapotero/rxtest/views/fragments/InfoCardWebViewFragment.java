package sapotero.rxtest.views.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.views.activities.DocumentInfocardFullScreenActivity;
import sapotero.rxtest.views.fragments.interfaces.PreviewFragment;
import timber.log.Timber;

public class InfoCardWebViewFragment extends PreviewFragment {

  @BindView(R.id.web_infocard) WebView infocard;
  @BindView(R.id.fragment_info_card_web_wrapper) RelativeLayout wrapper;

  @Inject ISettings settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  private String TAG = this.getClass().getSimpleName();
  private String uid;
  private boolean doubleTabEnabled = true;

  public InfoCardWebViewFragment() {
  }

  @Override
  public void update() {
    initWebView();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_info_card_web_view, container, false);
    ButterKnife.bind(this, view);
    EsdApplication.getDataComponent().inject( this );

    initEvents();
    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    initWebView();
  }

  private void initWebView() {
    final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
      @Override
      public boolean onDoubleTap(MotionEvent event) {


        Intent intent = new Intent(getContext(), DocumentInfocardFullScreenActivity.class);
        getContext().startActivity(intent);

        return true;
      }
    });

    if ( doubleTabEnabled ) {
      infocard.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    final WebSettings webSettings = infocard.getSettings();
    webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
    webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
    webSettings.setAppCacheEnabled(false);
    webSettings.setBlockNetworkImage(true);
    webSettings.setLoadsImagesAutomatically(true);
    webSettings.setGeolocationEnabled(false);
    webSettings.setNeedInitialFocus(false);
    webSettings.setSaveFormData(false);

    int fontsize = 16;
    try {
      fontsize = Integer.parseInt(settings.getInfocardFontSize());
    } catch (NumberFormatException e) {
      Timber.e(e);
    }

    webSettings.setDefaultFontSize( fontsize );

    setWebView();
  }

  private void initEvents() {
    Timber.tag(TAG).v("initEvents");
    unregisterEventBus();
    EventBus.getDefault().register(this);
  }

  private void unregisterEventBus() {
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
  }

  @Override
  public void onDestroy(){
    super.onDestroy();
    unregisterEventBus();
  }

  public void setWebView() {
    infocard.loadUrl("about:blank");

    Timber.tag(TAG).w("setWebView");
    RDocumentEntity doc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq( uid == null ? settings.getUid() : uid ))
      .get().firstOrNull();

    if (doc != null && doc.getInfoCard() != null) {

      Timber.tag(TAG).w("md5: %s %s", doc.getMd5(), doc.getUid());

      String htmlData = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + new String(Base64.decode( doc.getInfoCard(), Base64.DEFAULT) );
      infocard.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null);
      infocard.setWebViewClient(new WebViewClient(){
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
          return true;
        }
      });
    }

  }

  public InfoCardWebViewFragment withUid(String uid) {
    this.uid = uid;
    return this;
  }

  public PreviewFragment withEnableDoubleTap(boolean doubleTabEnabled) {
    this.doubleTabEnabled = doubleTabEnabled;
    return this;
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateCurrentDocumentEvent event) throws Exception {
    Timber.tag(TAG).w("UpdateCurrentDocumentEvent %s", event.uid);
    if (Objects.equals(event.uid, uid != null ? uid : settings.getUid())) {
      setWebView();
    }
  }
}

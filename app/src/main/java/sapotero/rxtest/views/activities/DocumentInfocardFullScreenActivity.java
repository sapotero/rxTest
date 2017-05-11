package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

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

public class DocumentInfocardFullScreenActivity extends AppCompatActivity {

  @BindView(R.id.document_image_toolbar) Toolbar toolbar;
  @BindView(R.id.fullscreen_web_infocard) WebView webview;


  @Inject RxSharedPreferences settings;
  @Inject Settings settings2;
  @Inject SingleEntityStore<Persistable> dataStore;

  private String TAG = this.getClass().getSimpleName();
  private WebSettings webSettings;
  private int FONT_SIZE = 16;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_document_infocard_full_screen);

    ButterKnife.bind(this);
    EsdApplication.getDataComponent().inject(this);

    toolbar.setContentInsetStartWithNavigation(250);
//    toolbar.setTitle("Просмотр инфокарточки");
    toolbar.setNavigationOnClickListener(v ->{
        finish();
      }
    );

    setDocument();

  }


  @OnClick(R.id.zoom_out)
  public void zoom_out(){
    if ( FONT_SIZE >= 16 ){
      FONT_SIZE-= 8;
      webSettings.setDefaultFontSize(FONT_SIZE);
    }
  }

  @OnClick(R.id.zoom_in)
  public void zoom_in(){
    if ( FONT_SIZE <= 40 ){
      FONT_SIZE+= 8;
      webSettings.setDefaultFontSize(FONT_SIZE);
    }
  }

  private void setDocument() {
    Preference<String> UID = settings.getString("activity_main_menu.uid");

    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq( UID.get() ))
      .get()
      .toObservable()
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        doc -> {

          if (doc != null) {

            String card = doc.getInfoCard();

            try {
              if ( card != null ){

                String htmlData = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + new String(Base64.decode( card, Base64.DEFAULT) );
                webview.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null);
                webview.getSettings().setBuiltInZoomControls(true);
                webview.getSettings().setDisplayZoomControls(true);
              }
            } catch (Exception e) {
              e.printStackTrace();
            }

          }
        },
        error -> {
          Timber.tag(TAG).e(error);
        }
      );


    webSettings = webview.getSettings();
    webSettings.setDefaultFontSize(FONT_SIZE);
    webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
    webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
    webSettings.setAppCacheEnabled(false);
    webSettings.setBlockNetworkImage(true);
    webSettings.setLoadsImagesAutomatically(true);
    webSettings.setGeolocationEnabled(false);
    webSettings.setNeedInitialFocus(false);
    webSettings.setSaveFormData(false);
    webview.setBackgroundColor(ContextCompat.getColor(this, R.color.md_white_1000));


  }

  @Override protected void onResume() {
    super.onResume();
    Timber.tag(TAG).i( " settings_username_host - " + settings2.getHost() );
  }

  @Override protected void onPause() {
    super.onPause();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }

}

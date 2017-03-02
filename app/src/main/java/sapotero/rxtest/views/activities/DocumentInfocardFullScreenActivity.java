package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import timber.log.Timber;

public class DocumentInfocardFullScreenActivity extends AppCompatActivity {

  @BindView(R.id.document_image_toolbar) Toolbar toolbar;
  @BindView(R.id.fullscreen_web_infocard) WebView webview;


  @Inject RxSharedPreferences settings;
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
    EsdApplication.getComponent(this).inject(this);

    toolbar.setContentInsetStartWithNavigation(250);
    toolbar.setTitle("Просмотр инфокарточки");
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
    Gson gson = new Gson();
    Preference<String> data = settings.getString("document.infoCard");
    String document = data.get();

    Preference<String> UID = settings.getString("activity_main_menu.uid");

    RDocumentEntity doc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq( UID.get() ))
      .get().first();
    document = doc.getInfoCard();

    try {
      if ( document != null ){

        String htmlData = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + new String(Base64.decode( document, Base64.DEFAULT) );
        webview.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null);
        webview.getSettings().setBuiltInZoomControls(false);
        webview.getSettings().setDisplayZoomControls(false);
        webview.setBackgroundColor( getResources().getColor(R.color.md_grey_50) );
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

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


  }

  @Override protected void onResume() {
    super.onResume();
    Timber.tag(TAG).i( " settings_username_host - " + settings.getString("settings_username_host").get() );
  }

  @Override protected void onPause() {
    super.onPause();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }

}

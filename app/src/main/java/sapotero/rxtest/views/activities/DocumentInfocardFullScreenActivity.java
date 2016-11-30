package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import timber.log.Timber;

public class DocumentInfocardFullScreenActivity extends AppCompatActivity {

  @BindView(R.id.document_image_toolbar) Toolbar toolbar;
  @BindView(R.id.fullscreen_web_infocard) WebView webview;


  @Inject RxSharedPreferences settings;

  private String TAG = this.getClass().getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_document_infocard_full_screen);

    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    toolbar.setTitle("Просмотр документа");
    toolbar.setNavigationOnClickListener(v ->{
        finish();
      }
    );

    setDocument();

  }

  private void setDocument() {
    Gson gson = new Gson();
    Preference<String> data = settings.getString("document.infoCard");
    String document = data.get();

    String htmlData = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + document;
    webview.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null);
    webview.getSettings().setBuiltInZoomControls(true);
    webview.getSettings().setDisplayZoomControls(true);

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

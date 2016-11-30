package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import timber.log.Timber;

public class DocumentImageFullScreenActivity extends AppCompatActivity {

  @BindView(R.id.document_image_toolbar) Toolbar toolbar;
  @BindView(R.id.document_image_fullscreen) PDFView pdfView;

  @Inject RxSharedPreferences settings;

  private String TAG = this.getClass().getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_document_image_full_screen);

    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    Intent intent = getIntent();

    if( intent.hasExtra("filename") ) {
      String filename = intent.getStringExtra("filename");
      File file = new File(getApplicationContext().getFilesDir(), filename);

      pdfView
        .fromFile(file)
        .enableSwipe(true)
        .enableDoubletap(true)
        .defaultPage(0)
        .swipeHorizontal(false)
        .onLoad(nbPages -> {
          Timber.tag(TAG).i(" onLoad");
        })
        .onError(t -> {
          Timber.tag(TAG).i(" onError");
        })
        .onPageChange((page, pageCount) -> {
          Timber.tag(TAG).i(" onPageChange");
        })
        .enableAnnotationRendering(false)
        .password(null)
        .scrollHandle(null)
        .load();

    }


    toolbar.setTitle("Просмотр документа");
    toolbar.setNavigationOnClickListener(v ->{
        finish();
      }
    );

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
//    mAttacher.cleanup();
  }

}

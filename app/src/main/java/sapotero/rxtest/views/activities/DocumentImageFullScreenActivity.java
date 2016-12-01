package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.views.views.CircleLeftArrow;
import sapotero.rxtest.views.views.CircleRightArrow;
import timber.log.Timber;

public class DocumentImageFullScreenActivity extends AppCompatActivity {

  @BindView(R.id.document_image_toolbar) Toolbar toolbar;
  @BindView(R.id.document_image_fullscreen) PDFView pdfView;

  @BindView(R.id.pdf_fullscreen_prev_document) CircleLeftArrow prev_document;
  @BindView(R.id.pdf_fullscreen_next_document) CircleRightArrow next_document;

  @BindView(R.id.pdf_fullscreen_document_counter) TextView document_counter;
  @BindView(R.id.pdf_fullscreen_page_counter)     TextView page_counter;


  @Inject RxSharedPreferences settings;

  private String TAG = this.getClass().getSimpleName();

  private ArrayList<Image> files = new ArrayList<Image>();
  private Integer index;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_document_image_full_screen);

    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    Intent intent = getIntent();

    if( intent.hasExtra("index") ) {
//      index = intent.getIntExtra("index");
      index = intent.getIntExtra("index", 0);
    }

    if( intent.hasExtra("files") ) {

      TypeToken<ArrayList<Image>> token = new TypeToken<ArrayList<Image>>() {};
      files = new Gson().fromJson( intent.getStringExtra( "files" ), token.getType() );

      Timber.tag(TAG).d("FILES: %s", files.size() );
    }

    updateDocument();

    toolbar.setTitleTextColor(getResources().getColor(R.color.md_grey_100));
    toolbar.setSubtitleTextColor(getResources().getColor(R.color.md_grey_400));
    toolbar.setContentInsetStartWithNavigation(250);
    toolbar.setNavigationOnClickListener(v ->{
        finish();
      }
    );

  }

  public void updateDocument(){
    setPdfPreview();
    updateDocumentCount();
    updatePageCount();
    updateTitle();

  }

  private void setPdfPreview() {
    Image image = files.get(index);
    File file = new File(getApplicationContext().getFilesDir(), String.format( "%s_%s", image.getMd5(), image.getTitle() ));

    pdfView
      .fromFile( file )
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
        updatePageCount();
      })
      .enableAnnotationRendering(false)
      .password(null)
      .scrollHandle(null)
      .load();
  }

  public void updateDocumentCount(){
    document_counter.setText( String.format("%s из %s", index + 1, files.size()) );
  }

  public void updatePageCount(){
    page_counter.setText( String.format("%s из %s страниц", pdfView.getCurrentPage() + 1, pdfView.getPageCount()) );
  }

  public void updateTitle(){
    toolbar.setTitle("Просмотр электронного образа");
    toolbar.setSubtitle( files.get(index).getTitle() );
  }


  @OnClick(R.id.pdf_fullscreen_prev_document)
  public void setLeftArrowArrow() {
    Timber.tag(TAG).i( "BEFORE %s - %s", index, files.size() );
    if ( index <= 0 ){
      index = files.size()-1;
    } else {
      index--;
    }
    Timber.tag(TAG).i( "AFTER %s - %s", index, files.size() );

    updateDocument();
  }

  @OnClick(R.id.pdf_fullscreen_next_document)
  public void setRightArrow() {
    Timber.tag(TAG).i( "BEFORE %s - %s", index, files.size() );
    if ( index >= files.size()-1 ){
      index = 0;
    } else {
      index++;
    }
    Timber.tag(TAG).i( "AFTER %s - %s", index, files.size() );

    updateDocument();
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

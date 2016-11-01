package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.io.File;
import java.io.FileNotFoundException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import timber.log.Timber;
import uk.co.senab.photoview.PhotoViewAttacher;

public class DocumentImageFullScreenActivity extends AppCompatActivity {

  @BindView(R.id.document_image_toolbar) Toolbar toolbar;
  @BindView(R.id.document_image_fullscreen) ImageView image;


  @Inject RxSharedPreferences settings;

  private String TAG = this.getClass().getSimpleName();
  private PhotoViewAttacher mAttacher;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_document_image_full_screen);

    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    Intent intent = getIntent();

    if(intent.hasExtra("filename")) {
      String fileName = getIntent().getStringExtra("filename");
      boolean exist = true;

      Timber.tag(TAG).i( "++filename " + fileName );

      File file = getFileStreamPath(fileName);
      if(file == null || !file.exists()) {
        exist = false;
      }

      if (exist){
        try {
          image.setImageBitmap( BitmapFactory.decodeStream( openFileInput(fileName)) );
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }
      } else {
        Timber.tag(TAG).i( "++filename not found" + fileName );
      }
    }

    mAttacher = new PhotoViewAttacher(image);

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
    mAttacher.cleanup();
  }

}

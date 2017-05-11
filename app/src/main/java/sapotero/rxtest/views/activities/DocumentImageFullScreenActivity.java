package sapotero.rxtest.views.activities;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.views.adapters.DocumentLinkAdapter;
import sapotero.rxtest.views.fragments.InfoCardDocumentsFragment;
import timber.log.Timber;

public class DocumentImageFullScreenActivity extends AppCompatActivity implements InfoCardDocumentsFragment.OnFragmentInteractionListener {

  @BindView(R.id.document_image_toolbar) Toolbar toolbar;
  @BindView(R.id.activity_document_image_full_screen_wrapper) FrameLayout wrapper;
//  @BindView(R.id.document_image_fullscreen)  PDFView pdfView;
//  @BindView(R.id.document_image_urgency_title) TextView urgency;
//
//  @BindView(R.id.pdf_fullscreen_prev_document)    CircleLeftArrow prev_document;
//  @BindView(R.id.pdf_fullscreen_next_document)    CircleRightArrow next_document;
//  @BindView(R.id.pdf_fullscreen_document_counter) TextView document_counter;
//  @BindView(R.id.pdf_fullscreen_page_counter)     TextView page_counter;


  @Inject RxSharedPreferences settings;

  private String TAG = this.getClass().getSimpleName();

  private ArrayList<Image> files = new ArrayList<Image>();
  private DocumentLinkAdapter adapter;
  private Integer index;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_document_image_full_screen);

    ButterKnife.bind(this);
    EsdApplication.getDataComponent().inject(this);

    InfoCardDocumentsFragment fragment = new InfoCardDocumentsFragment();
    fragment.withOutZoom(true);
    fragment.withUid(settings.getString("activity_main_menu.uid").get() );

    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.add(R.id.activity_document_image_full_screen_wrapper, fragment);
    fragmentTransaction.commit();


    toolbar.setTitleTextColor(getResources().getColor(R.color.md_black_1000));
    toolbar.setNavigationOnClickListener(v ->{
        finish();
      }
    );

    Drawable drawable = toolbar.getNavigationIcon();
    assert drawable != null;
    drawable.setColorFilter(ContextCompat.getColor(this, R.color.md_black_1000), PorterDuff.Mode.SRC_ATOP);

    toolbar.setTitle("Назад");

  }


  @Override
  public void onFragmentInteraction(Uri uri) {
    Timber.tag(TAG).v(uri.toString());
  }
}

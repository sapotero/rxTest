package sapotero.rxtest.views.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.views.adapters.DocumentLinkAdapter;
import sapotero.rxtest.views.fragments.InfoCardDocumentsFragment;
import timber.log.Timber;

public class DocumentImageFullScreenActivity extends AppCompatActivity implements InfoCardDocumentsFragment.OnFragmentInteractionListener {

  @BindView(R.id.document_image_toolbar) Toolbar toolbar;
  @BindView(R.id.activity_document_image_full_screen_wrapper) FrameLayout wrapper;

  @Inject ISettings settings;

  public static final String EXTRA_FILES_KEY = "files";
  public static final String EXTRA_INDEX_KEY = "index";

  private String TAG = this.getClass().getSimpleName();

  private ArrayList<Image> files = new ArrayList<Image>();
  private DocumentLinkAdapter adapter;
  private Integer index;

  private InfoCardDocumentsFragment fragment;

  public static Intent newIntent(Context context, ArrayList<Image> images, int index) {
    Type listType = new TypeToken<ArrayList<Image>>() {}.getType();

    Intent intent = new Intent( context, DocumentImageFullScreenActivity.class);
    intent.putExtra( EXTRA_FILES_KEY, new Gson().toJson( images, listType ) );
    intent.putExtra( EXTRA_INDEX_KEY, index );

    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_document_image_full_screen);

    ButterKnife.bind(this);
    EsdApplication.getDataComponent().inject(this);

    fragment = new InfoCardDocumentsFragment();
    fragment.withOutZoom(true);
    fragment.withUid( settings.getUid() );

    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.add(R.id.activity_document_image_full_screen_wrapper, fragment);
    fragmentTransaction.commit();


    toolbar.setTitleTextColor(getResources().getColor(R.color.md_black_1000));
    toolbar.setNavigationOnClickListener(v ->{
        closeActivity();
      }
    );
    toolbar.setOnClickListener(view -> {
      closeActivity();
    });

    Drawable drawable = toolbar.getNavigationIcon();
    assert drawable != null;
    drawable.setColorFilter(ContextCompat.getColor(this, R.color.md_black_1000), PorterDuff.Mode.SRC_ATOP);

    toolbar.setTitle("Назад");

  }

  private void closeActivity() {
    setResult(RESULT_OK);
    finish();
  }

  @Override
  public void onFragmentInteraction(Uri uri) {
    Timber.tag(TAG).v(uri.toString());
  }
}

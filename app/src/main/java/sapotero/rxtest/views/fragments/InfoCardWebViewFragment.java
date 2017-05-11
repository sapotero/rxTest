package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.views.activities.DocumentInfocardFullScreenActivity;
import sapotero.rxtest.views.adapters.utils.OnSwipeTouchListener;

public class InfoCardWebViewFragment extends Fragment {

  @BindView(R.id.web_infocard) WebView infocard;
  @BindView(R.id.fragment_info_card_web_wrapper) RelativeLayout wrapper;


  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  private OnFragmentInteractionListener mListener;
  private Context mContext;
  private String document;
  private String TAG = this.getClass().getSimpleName();
  private String uid;
  private Preference<String> UID;

  public InfoCardWebViewFragment() {
  }

  public static InfoCardWebViewFragment newInstance(String param1, String param2) {
    InfoCardWebViewFragment fragment = new InfoCardWebViewFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
    }
  }

  private void loadSettings() {
    UID  = settings.getString("activity_main_menu.uid");
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_info_card_web_view, container, false);
    ButterKnife.bind(this, view);
    EsdApplication.getDataComponent().inject( this );
    loadSettings();

    final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
      @Override
      public boolean onDoubleTap(MotionEvent event) {


        Intent intent = new Intent(getContext(), DocumentInfocardFullScreenActivity.class);
        getContext().startActivity(intent);

        return true;
      }
    });

    infocard.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

    wrapper.setOnTouchListener( new OnSwipeTouchListener( getContext() ) );

    setWebView();

    return view;
  }

  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }

  public void setWebView() {

//    Gson gson = new Gson();
//    Preference<String> data = settings.getString("document.infoCard");

      RDocumentEntity doc = dataStore
        .select(RDocumentEntity.class)
        .where(RDocumentEntity.UID.eq( uid == null ? UID.get() : uid ))
        .get().first();
      document = doc.getInfoCard();
//    } else {
//      document = data.get();
//    }



    try {
      if ( document != null ){

        String htmlData = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + new String(Base64.decode( document, Base64.DEFAULT) );
        infocard.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null);
        infocard.getSettings().setBuiltInZoomControls(false);
        infocard.getSettings().setDisplayZoomControls(true);
      }
    } catch (Exception e) {
      e.printStackTrace();
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

  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
      mContext = context;
    } else {
      throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public Fragment withUid(String uid) {
    this.uid = uid;
    return this;
  }

  public interface OnFragmentInteractionListener {
    void onFragmentInteraction(Uri uri);
  }
}

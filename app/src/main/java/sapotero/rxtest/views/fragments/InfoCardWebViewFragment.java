package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;

public class InfoCardWebViewFragment extends Fragment {

  @BindView(R.id.web_infocard) WebView infocard;

  @Inject RxSharedPreferences settings;

  private OnFragmentInteractionListener mListener;
  private Context mContext;
  private String document;
  private String TAG = this.getClass().getSimpleName();

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

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_info_card_web_view, container, false);
    ButterKnife.bind(this, view);
    EsdApplication.getComponent(mContext).inject( this );

    setWebView();

    return view;
  }

  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }

  public void setWebView() {

    Gson gson = new Gson();
    Preference<String> data = settings.getString("document.infoCard");
    document = data.get();

    try {
      if ( document != null ){
        String htmlData = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + document;
        infocard.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
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
  public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    void onFragmentInteraction(Uri uri);
  }
}

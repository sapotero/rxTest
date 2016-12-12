package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;

public class InfoCardFieldsFragment extends Fragment {

  @BindView(R.id.web_infocard) WebView infocard;

  @Inject RxSharedPreferences settings;

  private OnFragmentInteractionListener mListener;
  private Context mContext;
  private String document;
  private String TAG = this.getClass().getSimpleName();

  public InfoCardFieldsFragment() {
  }

  public static InfoCardFieldsFragment newInstance(String param1, String param2) {
    InfoCardFieldsFragment fragment = new InfoCardFieldsFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_info_card_fields, container, false);
    ButterKnife.bind(this, view);
    EsdApplication.getComponent(mContext).inject( this );


    return view;
  }

  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
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
    void onFragmentInteraction(Uri uri);
  }
}

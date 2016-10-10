package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

import sapotero.rxtest.R;

public class InfoCardFragment extends Fragment {

  public  static final String ARG_PAGE = "ARG_PAGE";
  private int mPage;

  private OnFragmentInteractionListener mListener;

  public InfoCardFragment() {
  }

  public static InfoCardFragment newInstance(int page) {
    InfoCardFragment fragment = new InfoCardFragment();
    Bundle args = new Bundle();

    args.putString(ARG_PAGE, String.valueOf(page));
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mPage = getArguments().getInt(ARG_PAGE);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    String page = String.valueOf( getArguments().getString(ARG_PAGE) );
    Log.d( "PAGE" , page);

    View rootView;

    if ( Objects.equals(page, "1") ){
      rootView = inflater.inflate(R.layout.fragment_info_document, container, false);
    } else {
      rootView = inflater.inflate(R.layout.fragment_info_card, container, false);
    }
    return rootView;
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

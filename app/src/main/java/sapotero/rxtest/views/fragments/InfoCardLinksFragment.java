package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import timber.log.Timber;

public class InfoCardLinksFragment extends Fragment {


  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  @BindView(R.id.fragment_info_card_link_wrapper) Spinner wrapper;

  private String TAG = this.getClass().getSimpleName();

  private OnFragmentInteractionListener mListener;
  private Context mContext;
  private String document;

  public InfoCardLinksFragment() {
  }

  public static InfoCardLinksFragment newInstance(String param1, String param2) {
    InfoCardLinksFragment fragment = new InfoCardLinksFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Timber.tag(TAG).w("onCreate" );

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_info_card_links, container, false);



    EsdApplication.getComponent(mContext).inject( this );
    ButterKnife.bind(this, view);

    loadSettings();

    return view;
  }

  @Override
  public void onResume(){
    super.onResume();

  }

  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }


  private void loadSettings() {
      Preference<String> DOCUMENT_UID = settings.getString("main_menu.uid");

      RDocumentEntity doc = dataStore
        .select(RDocumentEntity.class)
        .where(RDocumentEntity.UID.eq(DOCUMENT_UID.get()))
        .get().first();

      if ( doc.getLinks() != null ){

        Timber.tag(TAG).w("LINKS: %s", doc.getLinks() );

        String[] links = new Gson().fromJson( doc.getLinks(), String[].class );

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, links);
        wrapper.setAdapter( adapter );

    }


//      .toObservable()
//      .subscribeOn(Schedulers.io())
//      .observeOn(AndroidSchedulers.mainThread())
//      .subscribe(doc -> {
//        if ( doc.getLinks() != null ){
//
//          Timber.tag(TAG).w("LINKS: %s", doc.getLinks() );
//
//          String[] links = new Gson().fromJson( doc.getLinks(), String[].class );
//          wrapper.removeAllViews();
//
//          for(String link: links){
//            TextView text = new TextView(getContext());
//            text.setText( String.format( "* %s",link ) );
//            wrapper.addView(text);
//          }
//
//        }
//      });
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

package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
  @BindView(R.id.fragment_info_card_link_webview) WebView webview;
  @BindView(R.id.go) Button goButton;
  @BindView(R.id.fragment_info_card_link_disable) TextView disable;


  private String TAG = this.getClass().getSimpleName();

  private OnFragmentInteractionListener mListener;
  private Context mContext;
  private String document;
  private String uid;

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
    Timber.e( " loadSettings");

      Preference<String> DOCUMENT_UID = settings.getString("main_menu.uid");

      RDocumentEntity doc = dataStore
        .select(RDocumentEntity.class)
        .where(RDocumentEntity.UID.eq(uid == null ? DOCUMENT_UID.get() : uid ))
        .get().first();

      if ( doc.getLinks() != null ){


        String[] links = new Gson().fromJson( doc.getLinks(), String[].class );

        Timber.tag(TAG).w("LINKS: %s %s", doc.getLinks(), links.length );

        if (links.length == 0){
          links = new String[]{"Нет связанных документов"};
          goButton.setEnabled(false);
          disable.setVisibility(View.VISIBLE);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, links);


        wrapper.setAdapter( adapter );

        wrapper.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            adapter.getItem(position);
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {
            goButton.setEnabled(false);
          }
        });


      } else {
        Timber.e( " loadSettings empty");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.add("Нет связанных документов");
        wrapper.setAdapter( adapter );
      }

  }

  @OnClick( R.id.go )
  public void go(){
    try {
    RDocumentEntity document = dataStore
      .select( RDocumentEntity.class )
      .where( RDocumentEntity.UID.eq(uid) )
      .get()
      .first();

      if ( document != null ){
        String htmlData = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + document;
        webview.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setDisplayZoomControls(false);


      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public void isExist(String uid){
    int count = dataStore
      .count( RDocumentEntity.class )
      .where( RDocumentEntity.UID.eq(uid) )
      .get().value();

    Timber.e( " isExist - %s", count );
    if (count > 0){
      goButton.setEnabled(true);
      disable.setVisibility(View.GONE);
    } else {
      goButton.setEnabled(false);
      disable.setVisibility(View.VISIBLE);
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

  public Fragment withUid(String uid) {
    this.uid = uid;
    return this;
  }

  public interface OnFragmentInteractionListener {
    void onFragmentInteraction(Uri uri);
  }
}

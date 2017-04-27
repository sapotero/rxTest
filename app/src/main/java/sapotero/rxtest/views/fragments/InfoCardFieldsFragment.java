package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.views.adapters.utils.OnSwipeTouchListener;

public class InfoCardFieldsFragment extends Fragment {


  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  private String TAG = this.getClass().getSimpleName();

  private OnFragmentInteractionListener mListener;
  private Context mContext;
  private String document;

  @BindView(R.id.wrapper_field_created_at) LinearLayout wrapper_field_created_at;
  @BindView(R.id.wrapper_field_short_description) LinearLayout wrapper_field_short_description;
  @BindView(R.id.wrapper_field_urgency) LinearLayout wrapper_field_urgency;
  @BindView(R.id.wrapper_field_comment) LinearLayout wrapper_field_comment;
  @BindView(R.id.wrapper_field_external_number) LinearLayout wrapper_field_external_number;

  @BindView(R.id.field_created_at) TextView field_created_at;
  @BindView(R.id.field_short_description) TextView field_short_description;
  @BindView(R.id.field_urgency) TextView field_urgency;
  @BindView(R.id.field_comment) TextView field_comment;
  @BindView(R.id.field_external_number) TextView field_external_number;
  private String uid;

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
    EsdApplication.getComponent().inject( this );

    view.setOnTouchListener( new OnSwipeTouchListener( getContext() ) );

    loadSettings();

    return view;
  }

  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }


  private void loadSettings() {
    Preference<String> DOCUMENT_UID = settings.getString("activity_main_menu.uid");

    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(uid == null ? DOCUMENT_UID.get() : uid ))
      .get()
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(doc -> {
        if( doc.getRegistrationDate() != null ){
          field_created_at.setText( doc.getRegistrationDate() );
        } else {
          wrapper_field_created_at.setVisibility( View.GONE );
        }
        if( doc.getShortDescription() != null ){
          field_short_description.setText( doc.getShortDescription() );
        } else {
          wrapper_field_short_description.setVisibility( View.GONE );
        }
        if( doc.getUrgency() != null ){
          field_urgency.setText( doc.getUrgency() );
        } else {
          wrapper_field_urgency.setVisibility( View.GONE );
        }
        if( doc.getComment() != null ){
          field_comment.setText( doc.getComment() );
        } else {
          wrapper_field_comment.setVisibility( View.GONE );
        }
        if( doc.getExternalDocumentNumber() != null ){
          field_external_number.setText( doc.getExternalDocumentNumber() );
        } else {
          wrapper_field_external_number.setVisibility( View.GONE );
        }
      });
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

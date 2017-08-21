package sapotero.rxtest.views.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.views.adapters.utils.OnSwipeTouchListener;
import timber.log.Timber;

public class InfoCardFieldsFragment extends Fragment {

  @Inject ISettings settings;
  @Inject SingleEntityStore<Persistable> dataStore;

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

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_info_card_fields, container, false);
    ButterKnife.bind(this, view);
    EsdApplication.getDataComponent().inject( this );

    view.setOnTouchListener( new OnSwipeTouchListener( getContext() ) );

    loadSettings();

    return view;
  }

  private void loadSettings() {
    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(uid == null ? settings.getUid() : uid ))
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
      }, Timber::e);
  }

  public Fragment withUid(String uid) {
    this.uid = uid;
    return this;
  }
}

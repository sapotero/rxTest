package sapotero.rxtest.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

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
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.views.fragments.interfaces.PreviewFragment;
import timber.log.Timber;

public class InfoCardFieldsFragment extends PreviewFragment {

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
  private String TAG = this.getClass().getSimpleName();

  public InfoCardFieldsFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_info_card_fields, container, false);
    ButterKnife.bind(this, view);
    EsdApplication.getDataComponent().inject( this );

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

    initEvents();
    loadSettings();

  }

  @Override
  public void update() {

    initEvents();
    loadSettings();

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

  public PreviewFragment withUid(String uid) {
    this.uid = uid;
    return this;
  }

  private void initEvents() {
    Timber.tag(TAG).v("initEvents");
    unregisterEventBus();
    EventBus.getDefault().register(this);
  }

  private void unregisterEventBus() {
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
  }

  @Override
  public void onDestroy(){
    super.onDestroy();
    unregisterEventBus();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateCurrentDocumentEvent event) throws Exception {
    Timber.tag(TAG).w("UpdateCurrentDocumentEvent %s", event.uid);
    if (Objects.equals(event.uid, uid != null ? uid : settings.getUid())) {
      loadSettings();
    }
  }
}

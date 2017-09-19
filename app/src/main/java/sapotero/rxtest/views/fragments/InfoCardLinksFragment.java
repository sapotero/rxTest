package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RLinks;
import sapotero.rxtest.db.requery.models.RLinksEntity;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.views.activities.InfoNoMenuActivity;
import sapotero.rxtest.views.adapters.LinkAdapter;
import sapotero.rxtest.views.adapters.models.Link;
import sapotero.rxtest.views.fragments.interfaces.PreviewFragment;
import timber.log.Timber;

public class InfoCardLinksFragment extends PreviewFragment {

  @Inject ISettings settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  @BindView(R.id.fragment_info_card_link_wrapper) Spinner wrapper;
  @BindView(R.id.fragment_info_card_link_webview) WebView webview;
  @BindView(R.id.go) Button goButton;
  @BindView(R.id.fragment_info_card_link_disable) TextView disable;

  private String TAG = this.getClass().getSimpleName();

  private Context mContext;
  private String uid;
  private LinkAdapter adapter;

  public InfoCardLinksFragment() {
  }

  @Override
  public void update() {
    initLinks();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_info_card_links, container, false);

    EsdApplication.getDataComponent().inject( this );
    ButterKnife.bind(this, view);

    initEvents();

    return view;
  }

  @Override
  public void onResume(){
    super.onResume();

    initLinks();

  }

  private void initLinks() {
    adapter = new LinkAdapter(getContext(), new ArrayList<Link>());

    loadSettings();

    wrapper.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        show();
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });
  }

  private void loadSettings() {
    Timber.e( " loadSettings");

    adapter.clear();
    webview.loadUrl("about:blank");

    adapter.add( new Link( "0", "Нет связанных документов" ) );

    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(uid == null ? settings.getUid() : uid ))
      .get()
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        doc -> {

          if ( doc.getLinks() != null && doc.getLinks().size() > 0 ) {

            adapter.clear();

            Set<RLinks> links = doc.getLinks();

            for (RLinks _l : links) {


              RLinksEntity _tmp = (RLinksEntity) _l;
              Timber.tag("LOAD").e("links %s", _tmp.getUid() );

              RDocumentEntity _doc = dataStore
                .select(RDocumentEntity.class)
                .where(RDocumentEntity.UID.eq(_tmp.getUid()))
                .get().firstOrNull();

              if ( _doc != null ) {
                adapter.add(new Link(_doc.getUid(), _doc.getTitle()));
              } else {
                adapter.add(new Link( "0", "" ) );
              }
            }

            show();
          }
        }, error -> {
          Timber.tag(TAG).e(error);
        });



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

  }

  private void show(){
    try {

      disable.setVisibility(View.GONE);

      Timber.tag("GO").e( "Selected item: %s | %s", wrapper.getSelectedItemPosition(), adapter.getItem( wrapper.getSelectedItemPosition() ) );
      Link _link = adapter.getItem( wrapper.getSelectedItemPosition() );

      RDocumentEntity document = dataStore
        .select( RDocumentEntity.class )
        .where( RDocumentEntity.UID.eq( _link.getUid() ) )
        .get()
        .firstOrNull();

      if ( document != null ){
        String htmlData = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + new String(Base64.decode( document.getInfoCard(), Base64.DEFAULT) ) ;
        webview.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "UTF-8", null);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setDisplayZoomControls(false);
        webview.setWebViewClient(new WebViewClient(){
          @Override
          public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return true;
          }
        });
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @OnClick( R.id.go )
  public void go(){
    Link _link = adapter.getItem( wrapper.getSelectedItemPosition() );
    try {
      if (!Objects.equals(_link.getUid(), "0")){
        settings.setImageIndex(0);
        Intent intent = new Intent(mContext, InfoNoMenuActivity.class);
        intent.putExtra( "UID", _link.getUid() );
        startActivity(intent);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

 @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    mContext = context;
  }

  public InfoCardLinksFragment withUid(String uid) {
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

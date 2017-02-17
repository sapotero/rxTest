package sapotero.rxtest.views.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RFolderEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.bus.MassInsertDoneEvent;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.views.adapters.TabPagerAdapter;
import sapotero.rxtest.views.adapters.TabSigningPagerAdapter;
import sapotero.rxtest.views.dialogs.SelectOshsDialogFragment;
import sapotero.rxtest.views.fragments.DecisionPreviewFragment;
import sapotero.rxtest.views.fragments.InfoActivityDecisionPreviewFragment;
import sapotero.rxtest.views.fragments.InfoCardDocumentsFragment;
import sapotero.rxtest.views.fragments.InfoCardFieldsFragment;
import sapotero.rxtest.views.fragments.InfoCardLinksFragment;
import sapotero.rxtest.views.fragments.InfoCardWebViewFragment;
import sapotero.rxtest.views.fragments.RoutePreviewFragment;
import sapotero.rxtest.views.managers.menu.factories.CommandFactory;
import timber.log.Timber;

public class InfoNoMenuActivity extends AppCompatActivity implements InfoActivityDecisionPreviewFragment.OnFragmentInteractionListener, DecisionPreviewFragment.OnFragmentInteractionListener, RoutePreviewFragment.OnFragmentInteractionListener, InfoCardDocumentsFragment.OnFragmentInteractionListener, InfoCardWebViewFragment.OnFragmentInteractionListener, InfoCardLinksFragment.OnFragmentInteractionListener, InfoCardFieldsFragment.OnFragmentInteractionListener, /*CurrentDocumentManager.Callback,*/ SelectOshsDialogFragment.Callback {


  @BindView(R.id.activity_info_preview_container) LinearLayout preview_container;

  @BindView(R.id.tab_main) ViewPager viewPager;
  @BindView(R.id.tabs) TabLayout tabLayout;


  @Inject JobManager jobManager;
  @Inject CompositeSubscription subscriptions;
  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  private byte[] CARD;

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> PASSWORD;
  private Preference<String> DOCUMENT_UID;
  private Preference<String> STATUS_CODE;
  private Preference<Integer> POSITION;


//  private CurrentDocumentManager documentManager;
  private String TAG = this.getClass().getSimpleName();

  @BindView(R.id.toolbar) Toolbar toolbar;
  private Preference<String> HOST;
  //  private Preview preview;
  private Fields.Status status;
  private Fields.Journal journal;
  private SelectOshsDialogFragment oshs;

  private Menu menu;
  private String UID;
  private RDocumentEntity doc;
  private boolean showInfoCard = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_info);
    ButterKnife.bind(this);

    EsdApplication.getComponent(this).inject(this);

//    documentManager = new CurrentDocumentManager(this);
//    documentManager.registerCallBack(this);

    Intent intent = getIntent();
    if ( intent != null ){
      UID = getIntent().getExtras().getString("UID");

      try {
        showInfoCard = getIntent().getExtras().getBoolean("CARD");
      } catch (Exception e) {
        e.printStackTrace();
      }

      doc = dataStore
        .select(RDocumentEntity.class)
        .where(RDocumentEntity.UID.eq(UID)).get().first();
    }


    loadSettings();
    setToolbar();

    setPreview();
    setTabContent();
  }

  private void setPreview() {

    android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

    // FIX всегда отображаем резолюции
//    if ( status == Fields.Status.SIGNING || status == Fields.Status.APPROVAL ){
//      fragmentTransaction.add( R.id.activity_info_preview_container, new RoutePreviewFragment().withUid(UID) );
//    } else {
      fragmentTransaction.add( R.id.activity_info_preview_container, new InfoActivityDecisionPreviewFragment().withUid(UID) );
//    }

    fragmentTransaction.commit();
  }

  private void setToolbar() {

    toolbar.setTitleTextColor( getResources().getColor( R.color.md_grey_100 ) );
    toolbar.setSubtitleTextColor( getResources().getColor( R.color.md_grey_400 ) );

    toolbar.setContentInsetStartWithNavigation(250);

    toolbar.setNavigationOnClickListener(v ->{
      finish();
      }
    );



    status  = Fields.Status.findStatus(STATUS_CODE.get());
    journal = Fields.getJournalByUid( UID );

    toolbar.setTitle( String.format("%s от %s", doc.getRegistrationNumber(), doc.getRegistrationDate() ) );

    Timber.tag("MENU").e( "STATUS CODE: %s", STATUS_CODE.get() );

  }
  private void setTabContent() {

    if (viewPager.getAdapter() == null) {

      Timber.tag(TAG).e("setTabContent %s", "%" + Fields.Journal.CITIZEN_REQUESTS.getValue() );

      dataStore
        .select(RFolderEntity.class)
        .get().toObservable()
        .observeOn(Schedulers.io())
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe( folder -> {
          Timber.e( "%s - %s ", folder.getType(), folder.getTitle() );
        });

      if ( showInfoCard ){

        // для перехода по ссылкам из блока Согласование письмами
        TabPagerAdapter adapter = new TabPagerAdapter ( getSupportFragmentManager() );
        adapter.withUid(UID);
        viewPager.setAdapter(adapter);

      } else{
        if ( status == Fields.Status.SIGNING || status == Fields.Status.APPROVAL ){
          TabSigningPagerAdapter adapter = new TabSigningPagerAdapter( getSupportFragmentManager() );
          adapter.withUid(UID);
          viewPager.setAdapter(adapter);
        } else {
          TabPagerAdapter adapter = new TabPagerAdapter ( getSupportFragmentManager() );
          adapter.withUid(UID);
          viewPager.setAdapter(adapter);
        }
      }


      viewPager.setOffscreenPageLimit(10);

      tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
          viewPager.setCurrentItem( tab.getPosition() );
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
        }
      });
    }

    tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
    tabLayout.setupWithViewPager(viewPager);

  }


  private void loadSettings() {
    LOGIN    = settings.getString("login");
    PASSWORD = settings.getString("password");
    TOKEN    = settings.getString("token");
    POSITION = settings.getInteger("position");
    DOCUMENT_UID = settings.getString("document.uid");
    STATUS_CODE = settings.getString("activity_main_menu.start");

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    Timber.tag(TAG).i(String.valueOf(menu));
    return false;
  }



  @Override
  public void onStart() {
    super.onStart();

    if ( !EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().register(this);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if ( EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().unregister(this);
    }

    if ( subscriptions != null && subscriptions.hasSubscriptions() ){
      subscriptions.unsubscribe();
    }

    finish();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(MassInsertDoneEvent event) {
    Toast.makeText( getApplicationContext(), event.message, Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onFragmentInteraction(Uri uri) {
  }

  @Override
  protected void onResume() {
    super.onResume();
  }


//  /* CurrentDocumentManager.Callback */
//  @Override
//  public void onGetStateSuccess() {
//    Timber.tag("DocumentManagerCallback").i("onGetStateSuccess");
//  }
//
//  @Override
//  public void onGetStateError() {
//    Timber.tag("DocumentManagerCallback").i("onGetStateError");
//  }


  @Override
  public void onSearchSuccess(Oshs user, CommandFactory.Operation operation) {

  }

  @Override
  public void onSearchError(Throwable error) {

  }


  public boolean isOnline() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();
    return netInfo != null && netInfo.isConnectedOrConnecting();
  }
}

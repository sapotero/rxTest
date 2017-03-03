package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RFolderEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.bus.MassInsertDoneEvent;
import sapotero.rxtest.events.crypto.SignDataResultEvent;
import sapotero.rxtest.events.crypto.SignDataWrongPinEvent;
import sapotero.rxtest.events.decision.HasNoActiveDecisionConstructor;
import sapotero.rxtest.events.decision.ShowDecisionConstructor;
import sapotero.rxtest.events.utils.NoDocumentsEvent;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.events.view.ShowPrevDocumentEvent;
import sapotero.rxtest.events.view.ShowSnackEvent;
import sapotero.rxtest.events.view.UpdateCurrentInfoActivityEvent;
import sapotero.rxtest.jobs.bus.SyncDocumentsJob;
import sapotero.rxtest.utils.queue.QueueManager;
import sapotero.rxtest.views.adapters.TabPagerAdapter;
import sapotero.rxtest.views.adapters.TabSigningPagerAdapter;
import sapotero.rxtest.views.fragments.DecisionPreviewFragment;
import sapotero.rxtest.views.fragments.InfoActivityDecisionPreviewFragment;
import sapotero.rxtest.views.fragments.InfoCardDocumentsFragment;
import sapotero.rxtest.views.fragments.InfoCardFieldsFragment;
import sapotero.rxtest.views.fragments.InfoCardLinksFragment;
import sapotero.rxtest.views.fragments.InfoCardWebViewFragment;
import sapotero.rxtest.views.fragments.RoutePreviewFragment;
import sapotero.rxtest.views.managers.menu.OperationManager;
import sapotero.rxtest.views.managers.toolbar.ToolbarManager;
import timber.log.Timber;

public class InfoActivity extends AppCompatActivity implements InfoActivityDecisionPreviewFragment.OnFragmentInteractionListener, DecisionPreviewFragment.OnFragmentInteractionListener, RoutePreviewFragment.OnFragmentInteractionListener, InfoCardDocumentsFragment.OnFragmentInteractionListener, InfoCardWebViewFragment.OnFragmentInteractionListener, InfoCardLinksFragment.OnFragmentInteractionListener, InfoCardFieldsFragment.OnFragmentInteractionListener, OperationManager.Callback{


  @BindView(R.id.activity_info_preview_container) LinearLayout preview_container;

  @BindView(R.id.tab_main) ViewPager viewPager;
  @BindView(R.id.tabs) TabLayout tabLayout;
  @BindView(R.id.activity_info_wrapper) View wrapper;


  @Inject JobManager jobManager;
  @Inject CompositeSubscription subscriptions;
  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  // test
  @Inject QueueManager queue;
  @Inject OperationManager operationManager;

  private static final int SWIPE_MIN_DISTANCE = 120;
  private static final int SWIPE_MAX_OFF_PATH = 250;
  private static final int SWIPE_THRESHOLD_VELOCITY = 200;

  private byte[] CARD;

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> PASSWORD;
  private Preference<String> UID;
  private Preference<String> DOCUMENT_UID;
  private Preference<String> STATUS_CODE;
  private Preference<Integer> POSITION;
  private Preference<String> REG_NUMBER;
  private Preference<String> REG_DATE;

  private String TAG = this.getClass().getSimpleName();
  private CompositeSubscription subscription;

  @BindView(R.id.toolbar) Toolbar toolbar;

  private ToolbarManager toolbarManager;
  private Fields.Journal journal;
  private Fields.Status  status;
  private MaterialDialog.Builder loadingDialog;
  private Preference<Boolean> IS_PROCESSED;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_info);
    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    operationManager.registerCallBack(this);

    toolbarManager = new ToolbarManager( this, toolbar);
    toolbarManager.init();

    loadSettings();

    status  = Fields.Status.findStatus( STATUS_CODE.get() );
    journal = Fields.getJournalByUid( UID.get() );

    setTabContent();
    setPreview();

    loadingDialog = new MaterialDialog.Builder(this)
      .title("Обновление данных...")
      .progress(true, 0);

  }


  private void setPreview() {
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

    Timber.tag("INFO").v( "JOURNAL: %s | STATUS: %s", journal.getName(), status.getName() );

    if ( status == Fields.Status.SIGNING || status == Fields.Status.APPROVAL || IS_PROCESSED.get()  ){
      fragmentTransaction.add( R.id.activity_info_preview_container, new RoutePreviewFragment() );
    } else {
      fragmentTransaction.add( R.id.activity_info_preview_container, new InfoActivityDecisionPreviewFragment(toolbarManager) );
    }

    fragmentTransaction.commit();
  }

  private void setTabContent() {

    if (viewPager.getAdapter() == null) {

      Timber.tag(TAG).e("IS_PROCESSED.get() %s | %s -> %s", status, STATUS_CODE.get(), IS_PROCESSED.get() );

      dataStore
        .select(RFolderEntity.class)
        .get().toObservable()
        .observeOn(Schedulers.io())
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe( folder -> {
          Timber.e( "%s - %s ", folder.getType(), folder.getTitle() );
        });

      if ( status == Fields.Status.SIGNING || status == Fields.Status.APPROVAL || IS_PROCESSED.get()  ){
        TabSigningPagerAdapter adapter = new TabSigningPagerAdapter( getSupportFragmentManager() );
        viewPager.setAdapter(adapter);
      } else {
        TabPagerAdapter adapter = new TabPagerAdapter ( getSupportFragmentManager() );
        viewPager.setAdapter(adapter);
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
    UID      = settings.getString("activity_main_menu.uid");
    PASSWORD = settings.getString("password");
    TOKEN    = settings.getString("token");
    POSITION = settings.getInteger("position");
    DOCUMENT_UID = settings.getString("document.uid");
    STATUS_CODE = settings.getString("activity_main_menu.star");
    IS_PROCESSED = settings.getBoolean("activity_main_menu.from_sign");
    REG_NUMBER = settings.getString("activity_main_menu.regnumber");
    REG_DATE = settings.getString("activity_main_menu.date");

  }

  @OnClick(R.id.activity_info_left_button)
  public void prev(){
    Timber.tag(TAG).v("prev document");
  }

  @OnClick(R.id.activity_info_right_button)
  public void next(){
    Timber.tag(TAG).v("next document");
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

    unsubscribe();

    finish();
  }
//
//  @Override
//  protected void onSaveInstanceState(Bundle outState) {
//    super.onSaveInstanceState(outState);
//
//
//    try {
//      removeFragments();
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }
//
//  private void removeFragments() {
//    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//
//    for (Fragment fragment : getSupportFragmentManager().getFragments()){
//      ft.remove(fragment);
//    }
//
//    ft.commit();
//  }

  @Override
  public void onFragmentInteraction(Uri uri) {
  }


  @Override
  protected void onResume() {
    super.onResume();

    operationManager.registerCallBack(null);
    operationManager.registerCallBack(this);

    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);

    updateCurrent();

  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(MassInsertDoneEvent event) {
    Toast.makeText( getApplicationContext(), event.message, Toast.LENGTH_SHORT).show();
  }

  /* OperationManager.Callback */
  @Override
  public void onExecuteSuccess(String command) {
    Timber.tag(TAG).i("OperationManager.onExecuteSuccess %s", command);
    toolbarManager.update(command);
  }

  @Override
  public void onExecuteError() {
    Timber.tag(TAG).i("OperationManager.onExecuteError");
  }






  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(SignDataResultEvent event) throws Exception {
    Timber.d("SignDataResultEvent %s", event.sign);

    if (event.sign != null) {
      Toast.makeText( getApplicationContext(), event.sign, Toast.LENGTH_SHORT ).show();
    }
    toolbarManager.hideDialog();

  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(SignDataWrongPinEvent event) throws Exception {
    Timber.d("SignDataWrongPinEvent %s", event.data);

    if (event.data != null) {
      Toast.makeText( getApplicationContext(), event.data, Toast.LENGTH_SHORT ).show();

    }

    toolbarManager.hideDialog();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(ShowSnackEvent event) throws Exception {
    Snackbar.make( wrapper, event.message, Snackbar.LENGTH_LONG ).show();
  }


  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(ShowDecisionConstructor event) throws Exception {
    Intent intent = new Intent( this, DecisionConstructorActivity.class);
    InfoActivity activity = (InfoActivity) this;
    activity.startActivity(intent);
//    activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(HasNoActiveDecisionConstructor event) throws Exception {
    toolbarManager.showCreateDecisionButton();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateCurrentInfoActivityEvent event) throws Exception {

    Timber.d("UpdateCurrentInfoActivityEvent");
    loadingDialog.show();

    new Handler().postDelayed(this::restart, 5000);

  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(NoDocumentsEvent event) throws Exception {
    finish();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(ShowPrevDocumentEvent event) throws Exception {
    showPrevDocument();
  }

  private void showPrevDocument() {
    try {
      InfoActivity activity = (InfoActivity) this;
      Intent intent = new Intent(this, InfoActivity.class);

      MainActivity.RAdapter.getPrevFromPosition(settings.getInteger("activity_main_menu.position").get());

      activity.startActivity(intent);
      activity.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
      finish();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(ShowNextDocumentEvent event){
    showNextDocument();
  }

  private void showNextDocument() {
    try {
      InfoActivity activity = (InfoActivity) this;
      Intent intent = new Intent( this, InfoActivity.class);

      MainActivity.RAdapter.getNextFromPosition( settings.getInteger("activity_main_menu.position").get() );
      activity.startActivity(intent);
      activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
      finish();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  private void restart() {
    Intent intent = getIntent();
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
    overridePendingTransition(0, 0);
    finish();
    startActivity(intent);
  }

  public void updateCurrent(){
    unsubscribe();

    subscription.add(
      Observable
        .interval( 10, TimeUnit.SECONDS )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(interval -> {
          jobManager.addJobInBackground(new SyncDocumentsJob( UID.get(), status ));
        })
    );
  }

  private void unsubscribe(){
    if ( subscription == null ){
      subscription = new CompositeSubscription();
    }

    if (subscription.hasSubscriptions()){
      subscription.unsubscribe();
    }
  }
}

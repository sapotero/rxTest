package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
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
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.events.view.UpdateCurrentInfoActivityEvent;
import sapotero.rxtest.jobs.bus.UpdateDocumentJob;
import sapotero.rxtest.managers.menu.OperationManager;
import sapotero.rxtest.managers.toolbar.ToolbarManager;
import sapotero.rxtest.services.task.UpdateCurrentDocumentTask;
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
import timber.log.Timber;

public class InfoActivity extends AppCompatActivity implements InfoActivityDecisionPreviewFragment.OnFragmentInteractionListener, DecisionPreviewFragment.OnFragmentInteractionListener, RoutePreviewFragment.OnFragmentInteractionListener, InfoCardDocumentsFragment.OnFragmentInteractionListener, InfoCardWebViewFragment.OnFragmentInteractionListener, InfoCardLinksFragment.OnFragmentInteractionListener, InfoCardFieldsFragment.OnFragmentInteractionListener{


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

  private Preference<String> TOKEN;
  private Preference<String> LAST_SEEN_UID;
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
  private ScheduledThreadPoolExecutor scheduller;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_info);
    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);


  }

  private void initInfoActivity() {
    loadSettings();

    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);

    toolbarManager = new ToolbarManager( this, toolbar);
    toolbarManager.init();

    setLastSeen();

    status  = Fields.Status.findStatus( STATUS_CODE.get() );
    journal = Fields.getJournalByUid( UID.get() );

    setTabContent();
    setPreview();
  }



  private void setTabContent() {

    try {
      tabLayout.removeAllTabs();
      viewPager.removeAllViews();
    } catch (Exception e) {
      e.printStackTrace();
    }


//    Timber.tag(TAG).e("IS_PROCESSED.get() %s | %s -> %s", status, STATUS_CODE.get(), IS_PROCESSED.get() );


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


    tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
    tabLayout.setupWithViewPager(viewPager);
  }

  private void setPreview() {

    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

//    Timber.tag("INFO").v( "JOURNAL: %s | STATUS: %s", journal.getName(), status.getName() );

    try {
      LinearLayout layout = (LinearLayout) findViewById(R.id.activity_info_preview_container);
      layout.removeAllViews();
    } catch (Exception e) {
      e.printStackTrace();
    }

    if ( status == Fields.Status.SIGNING || status == Fields.Status.APPROVAL || IS_PROCESSED.get()  ){
      fragmentTransaction.add( R.id.activity_info_preview_container, new RoutePreviewFragment() );
    } else {
      fragmentTransaction.add( R.id.activity_info_preview_container, new InfoActivityDecisionPreviewFragment(toolbarManager) );
    }

    fragmentTransaction.commit();
  }

  private void loadSettings() {
    LOGIN    = settings.getString("login");
    UID      = settings.getString("activity_main_menu.uid");
    LAST_SEEN_UID = settings.getString("activity_main_menu.last_seen_uid");
    PASSWORD = settings.getString("password");
    TOKEN    = settings.getString("token");
    POSITION = settings.getInteger("position");
    DOCUMENT_UID = settings.getString("document.uid");
    STATUS_CODE = settings.getString("activity_main_menu.star");
    IS_PROCESSED = settings.getBoolean("activity_main_menu.from_sign");
    REG_NUMBER = settings.getString("activity_main_menu.regnumber");
    REG_DATE = settings.getString("activity_main_menu.date");

  }




  @OnClick(R.id.activity_info_prev_document)
  public void prev_doc(){
    showNextDocument();
  }
  @OnClick(R.id.activity_info_next_document)
  public void next_doc(){
    showPrevDocument();
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

    if (scheduller != null){
      scheduller.shutdown();
    }

    unsubscribe();

    finish();
  }

  public void exitIfAlreadySeenThisFuckingDocument(){
    if (LAST_SEEN_UID.get() != null && UID.get() != null) {
      if (Objects.equals(LAST_SEEN_UID.get(), UID.get())){
        finish();
      }
    }
  }

  public void setLastSeen(){
    LAST_SEEN_UID.set( UID.get() );
  }


  @Override
  public void onFragmentInteraction(Uri uri) {
  }


  @Override
  protected void onResume() {
    super.onResume();

    settings.getBoolean("decision_with_assigment").set(false);

    initInfoActivity();
    updateCurrent();

    invalidateArrows();

//    startThreadedUpdate();
//    Keyboard.hide(this);

  }

  private void invalidateArrows() {
    // если пришли из поиска - дизейблим стрелки
    try {
      if ( settings.getBoolean("load_from_search").get() ){
        ImageButton prev = (ImageButton) findViewById(R.id.activity_info_prev_document);
        ImageButton next = (ImageButton) findViewById(R.id.activity_info_next_document);

        prev.setClickable(false);
        next.setClickable(false);
        prev.setAlpha(0.5f);
        next.setAlpha(0.5f);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void startThreadedUpdate() {
    scheduller = new ScheduledThreadPoolExecutor(1);
    scheduller.scheduleWithFixedDelay( new UpdateCurrentDocumentTask(UID.get()), 0 ,5, TimeUnit.SECONDS );
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(MassInsertDoneEvent event) {
    Toast.makeText( getApplicationContext(), event.message, Toast.LENGTH_SHORT).show();
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
    updateCurrent();

  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateCurrentDocumentEvent event) throws Exception {
    Timber.d("UpdateCurrentDocumentEvent");
    updateCurrent();

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

    Timber.tag("SHOW_PREV").e("info_act");


    MainActivity.RAdapter.getPrevFromPosition( settings.getInteger("activity_main_menu.position").get() );

    exitIfAlreadySeenThisFuckingDocument();

    initInfoActivity();
    updateCurrent();
//    finish();
//
//    InfoActivity activity = this;
//    Intent intent = new Intent(this, InfoActivity.class);
//
////    MainActivity.invalidate();
//
//    MainActivity.RAdapter.getPrevFromPosition(settings.getInteger("activity_main_menu.position").get());
//    activity.startActivity(intent);
//      activity.overridePendingTransition(R.anim.slide_to_right, R.anim.slide_from_left);
//      activity.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(ShowNextDocumentEvent event){
    showNextDocument();
  }

  private void showNextDocument() {

    Timber.tag("SHOW_NEXT").e("info_act");


    MainActivity.RAdapter.getNextFromPosition( settings.getInteger("activity_main_menu.position").get() );

    exitIfAlreadySeenThisFuckingDocument();

    initInfoActivity();
    updateCurrent();

//    finish();
//
//    InfoActivity activity = this;
//    Intent intent = new Intent( this, InfoActivity.class);
//
//    MainActivity.invalidate();
//
//    MainActivity.RAdapter.getNextFromPosition( settings.getInteger("activity_main_menu.position").get() );
//    activity.startActivity(intent);
//    activity.overridePendingTransition(R.anim.slide_to_left, R.anim.slide_from_right);
//    activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);



  }


  private void restart() {
    Intent intent = getIntent();
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
    overridePendingTransition(0, 0);
    finish();
    startActivity(intent);
  }

  public void updateCurrent(){

    jobManager.addJobInBackground(new UpdateDocumentJob( UID.get(), status ));

    unsubscribe();
    subscription.add(
      Observable
        .interval( 5, TimeUnit.SECONDS )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(interval -> {
          jobManager.addJobInBackground(new UpdateDocumentJob( UID.get(), status ));
        })
    );

    toolbarManager.invalidate();
  }

  private void unsubscribe(){
    if ( subscription == null ){
      subscription = new CompositeSubscription();
    }

    if (subscription.hasSubscriptions()){
      subscription.clear();
    }
  }
}

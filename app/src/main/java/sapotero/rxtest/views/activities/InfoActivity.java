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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;

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
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.bus.MassInsertDoneEvent;
import sapotero.rxtest.events.crypto.SignDataResultEvent;
import sapotero.rxtest.events.crypto.SignDataWrongPinEvent;
import sapotero.rxtest.events.decision.HasNoActiveDecisionConstructor;
import sapotero.rxtest.events.decision.ShowDecisionConstructor;
import sapotero.rxtest.events.document.DropControlEvent;
import sapotero.rxtest.events.utils.NoDocumentsEvent;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.events.view.ShowPrevDocumentEvent;
import sapotero.rxtest.events.view.ShowSnackEvent;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.events.view.UpdateCurrentInfoActivityEvent;
import sapotero.rxtest.jobs.bus.UpdateDocumentJob;
import sapotero.rxtest.managers.toolbar.ToolbarManager;
import sapotero.rxtest.services.task.UpdateCurrentDocumentTask;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.views.adapters.TabPagerAdapter;
import sapotero.rxtest.views.adapters.TabSigningPagerAdapter;
import sapotero.rxtest.views.fragments.InfoActivityDecisionPreviewFragment;
import sapotero.rxtest.views.fragments.InfoCardDocumentsFragment;
import sapotero.rxtest.views.fragments.InfoCardFieldsFragment;
import sapotero.rxtest.views.fragments.InfoCardLinksFragment;
import sapotero.rxtest.views.fragments.InfoCardWebViewFragment;
import sapotero.rxtest.views.fragments.RoutePreviewFragment;
import timber.log.Timber;

public class InfoActivity extends AppCompatActivity implements InfoActivityDecisionPreviewFragment.OnFragmentInteractionListener, RoutePreviewFragment.OnFragmentInteractionListener, InfoCardDocumentsFragment.OnFragmentInteractionListener, InfoCardWebViewFragment.OnFragmentInteractionListener, InfoCardLinksFragment.OnFragmentInteractionListener, InfoCardFieldsFragment.OnFragmentInteractionListener{

  @BindView(R.id.activity_info_preview_container) LinearLayout preview_container;
  @BindView(R.id.frame_preview_decision) FrameLayout frame;

  @BindView(R.id.tab_main) ViewPager viewPager;
  @BindView(R.id.activity_info_wrapper) View wrapper;
  @BindView(R.id.tabs) TabLayout tabLayout;

  @Inject JobManager jobManager;
  @Inject ISettings settings;
  @Inject MemoryStore store;

  @Inject SingleEntityStore<Persistable> dataStore;

  @BindView(R.id.toolbar) Toolbar toolbar;

  private String TAG = this.getClass().getSimpleName();
  private CompositeSubscription subscription;
  private ToolbarManager toolbarManager;
  private Fields.Journal journal;
  private Fields.Status  status;
  private ScheduledThreadPoolExecutor scheduller;
  private Subscription loggerSubscription;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_info);
    ButterKnife.bind(this);
    EsdApplication.getManagerComponent().inject(this);

    clearImageIndex();

  }







  private void initInfoActivity() {
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);


    toolbarManager = new ToolbarManager( this, toolbar);
    toolbarManager.init();

    setLastSeen();

    status  = Fields.Status.findStatus( settings.getStatusCode() );
    journal = Fields.getJournalByUid( settings.getUid() );

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


    if ( status == Fields.Status.SIGNING || status == Fields.Status.APPROVAL ){
      TabSigningPagerAdapter adapter = new TabSigningPagerAdapter( getSupportFragmentManager() );
      viewPager.setAdapter(adapter);
    } else {
      TabPagerAdapter adapter = new TabPagerAdapter ( getSupportFragmentManager() );
      viewPager.setAdapter(adapter);
    }
    viewPager.setOffscreenPageLimit(4);

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
    addLoader();

    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.addToBackStack("PREVIEW");

    if ( status == Fields.Status.SIGNING || status == Fields.Status.APPROVAL ){
      fragmentTransaction.replace( R.id.activity_info_preview_container, new RoutePreviewFragment(), "PREVIEW" );
    } else {
      fragmentTransaction.replace( R.id.activity_info_preview_container, new InfoActivityDecisionPreviewFragment(toolbarManager), "PREVIEW" );
    }

    fragmentTransaction.commit();
  }

  private void addLoader() {
    preview_container.removeAllViews();

    frame.setVisibility(View.VISIBLE);

    int durationMillis = 300;

    Animation fadeIn = new AlphaAnimation(0, 1);
    fadeIn.setInterpolator(new DecelerateInterpolator());
    fadeIn.setDuration(durationMillis);

    Animation fadeOut = new AlphaAnimation(1, 0);
    fadeOut.setInterpolator(new AccelerateInterpolator());
    fadeOut.setStartOffset(durationMillis);
    fadeOut.setDuration(durationMillis);

    AnimationSet animation = new AnimationSet(true);
    AnimationSet wrapperAnimation = new AnimationSet(true);

    wrapperAnimation.addAnimation(fadeIn);
    animation.addAnimation(fadeOut);

    frame.setAnimation(animation);
    preview_container.setAnimation(wrapperAnimation);

    Observable.just("")
      .delay(durationMillis, TimeUnit.MILLISECONDS)
      .subscribeOn( Schedulers.newThread() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data  -> {
          frame.setVisibility(View.GONE);
        },
        Timber::e
      );

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

    if (loggerSubscription != null) {
      loggerSubscription.unsubscribe();
    }

    unsubscribe();

    finish();
  }

  public void exitIfAlreadySeenThisFuckingDocument(){
    if (Objects.equals(settings.getLastSeenUid(), settings.getUid())){
      Timber.tag(TAG).e("exitIfAlreadySeenThisDocument");
      finish();
    }
  }

  public void setLastSeen(){
    settings.setLastSeenUid( settings.getUid() );
  }


  @Override
  public void onFragmentInteraction(Uri uri) {
  }


  @Override
  protected void onResume() {
    super.onResume();

    settings.setDecisionWithAssignment(false);

    initInfoActivity();
    updateCurrent();

    invalidateArrows();

    chechPrimaryConsiderationDialog();
  }

  private void chechPrimaryConsiderationDialog() {
    if (settings.isShowPrimaryConsideration()){
      settings.setShowPrimaryConsideration(false);
      toolbarManager.showPrimaryConsiderationDialog(this);
    }
  }

  private void invalidateArrows() {
    // если пришли из поиска - дизейблим стрелки
    try {
      if ( settings.isLoadFromSearch() ){
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
    scheduller.scheduleWithFixedDelay( new UpdateCurrentDocumentTask(settings.getUid()), 0 ,5, TimeUnit.SECONDS );
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
  public void onMessageEvent(DropControlEvent event) throws Exception {
    Timber.tag(TAG).w("event: %s", event.control);
    toolbarManager.dropControlLabel( event.control );
  }


  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(ShowDecisionConstructor event) throws Exception {


    RDecisionEntity decision = dataStore
      .select(RDecisionEntity.class)
      .where(RDecisionEntity.ID.eq( settings.getDecisionActiveId() ) )
      .get().firstOrNull();

    if (decision != null) {

      Timber.tag(TAG).i("[%s] %s : %s|%s", decision.getId(), decision.getUid(), decision.isChanged(), decision.isTemporary() );

        if ( settings.isOnline() ){
          if ( decision.isChanged() != null && decision.isChanged() ){
            Toast.makeText( this, "3апрещено редактировать резолюцию. Дождитесь выполнения операции.", Toast.LENGTH_SHORT).show();
          } else {
            showDecisionEditor();
          }
        } else {
          showDecisionEditor();
        }
    }
//    activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
  }

  private void showDecisionEditor() {
    Intent intent = new Intent( this, DecisionConstructorActivity.class);
    InfoActivity activity = (InfoActivity) this;
    activity.startActivity(intent);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(HasNoActiveDecisionConstructor event) throws Exception {
//    toolbarManager.showCreateDecisionButton();
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
    Timber.tag(TAG).e("NoDocumentsEvent");
    finish();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(ShowPrevDocumentEvent event) throws Exception {
    Timber.tag(TAG).e("ShowPrevDocumentEvent");
    showPrevDocument();
  }

  private void showPrevDocument() {

    Timber.tag("SHOW_PREV").e("info_act");


    MainActivity.RAdapter.getPrevFromPosition( settings.getMainMenuPosition() );

    exitIfAlreadySeenThisFuckingDocument();

    clearImageIndex();
    initInfoActivity();
    updateCurrent();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(ShowNextDocumentEvent event){
    Timber.tag(TAG).e("ShowNextDocumentEvent");
    showNextDocument();
  }

  private void showNextDocument() {

    Timber.tag("SHOW_NEXT").e("info_act");


    MainActivity.RAdapter.getNextFromPosition( settings.getMainMenuPosition() );

    exitIfAlreadySeenThisFuckingDocument();

    clearImageIndex();
    initInfoActivity();
    updateCurrent();

  }

  private void clearImageIndex() {
    settings.setImageIndex(0);
  }

  public void updateCurrent(){

    updateDocument();

    unsubscribe();
    subscription.add(
      Observable
        .interval( 5, TimeUnit.SECONDS )
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(interval -> {
          updateDocument();
        }, Timber::e)
    );

    toolbarManager.invalidate();
  }

  private void updateDocument() {
    jobManager.addJobInBackground( new UpdateDocumentJob( settings.getUid() ) );
  }

  private void unsubscribe(){
    if ( subscription == null ){
      subscription = new CompositeSubscription();
    }

    if (subscription.hasSubscriptions()){
      subscription.clear();
    }
  }

  @Override
  public void onBackPressed() {
    finish();
  }
}

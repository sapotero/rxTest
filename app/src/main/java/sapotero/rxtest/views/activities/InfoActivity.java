package sapotero.rxtest.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.utils.JournalStatus;
import sapotero.rxtest.events.decision.HasNoActiveDecisionConstructor;
import sapotero.rxtest.events.decision.HideTemporaryEvent;
import sapotero.rxtest.events.decision.ShowDecisionConstructor;
import sapotero.rxtest.events.document.DropControlEvent;
import sapotero.rxtest.events.document.UpdateDocumentEvent;
import sapotero.rxtest.events.notification.RemoveAllNotificationEvent;
import sapotero.rxtest.events.view.ShowNextDocumentEvent;
import sapotero.rxtest.events.view.ShowSnackEvent;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.jobs.bus.UpdateDocumentJob;
import sapotero.rxtest.managers.menu.utils.DateUtil;
import sapotero.rxtest.managers.toolbar.ToolbarManager;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.views.adapters.TabPagerAdapter;
import sapotero.rxtest.views.adapters.TabSigningPagerAdapter;
import sapotero.rxtest.views.adapters.utils.FragmentAdapter;
import sapotero.rxtest.views.fragments.DecisionPreviewFragment;
import sapotero.rxtest.views.fragments.RoutePreviewFragment;
import sapotero.rxtest.views.fragments.interfaces.PreviewFragment;
import timber.log.Timber;

public class InfoActivity extends AppCompatActivity {

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

  public static final String EXTRA_DOCUMENTUIDS_KEY = "document_uids";

  private String TAG = this.getClass().getSimpleName();
  private CompositeSubscription subscription;
  private ToolbarManager toolbarManager;
  private JournalStatus status;

  private List<String> documentUids;

  /*ключи EXTRA. Для передачи в PendingIntent в NotifyManager*/
  private static final String EXTRA_DOCUMENTUID_KEY              = "document_uid";
  private static final String EXTRA_IS_PROJECT_KEY               = "is_project";
  private static final String EXTRA_REGISTRATION_NUMBER_KEY      = "registration_number";
  private static final String EXTRA_STATUS_CODE_KEY              = "status_code";
  private static final String EXTRA_IS_LOAD_FROM_SEARCHE_KEY     = "is_load_from_search";
  private static final String EXTRA_REGISTRATION_DATE_KEY        = "registration_date";
  private static final String EXTRA_IS_FROM_NOTIFICATION_BAR_KEY = "is_from_notification";
  private static final String EXTRA_NOTIFICATION_ID              = "notification_id";

  private FragmentAdapter viewPagerAdapter;


  public static Intent newIntent(Context context, ArrayList<String> documentUids) {
    Intent intent = new Intent(context, InfoActivity.class);
    intent.putStringArrayListExtra(EXTRA_DOCUMENTUIDS_KEY, documentUids);
    return intent;
  }

  public static Intent newIntent(Context context, Document document, String filter, int notificationId ) {
    Intent intent = new Intent(context, InfoActivity.class);
    intent.putExtra(EXTRA_DOCUMENTUID_KEY, document.getUid());
    intent.putExtra(EXTRA_IS_PROJECT_KEY,document.isProject());
    intent.putExtra(EXTRA_REGISTRATION_NUMBER_KEY, document.getRegistrationNumber());
    intent.putExtra(EXTRA_STATUS_CODE_KEY, filter);
    intent.putExtra(EXTRA_IS_LOAD_FROM_SEARCHE_KEY, true);
    intent.putExtra(EXTRA_REGISTRATION_DATE_KEY, document.getRegistrationDate() );
    intent.putExtra(EXTRA_IS_FROM_NOTIFICATION_BAR_KEY, true);
    intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
    return intent;
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_info);
    ButterKnife.bind(this);
    EsdApplication.getManagerComponent().inject(this);
    /*если intent "прилетел" из NotifyManager -> значение будет true*/
    boolean isIntentFromNotificationBar = false;

    if (getIntent().getExtras() != null){
      isIntentFromNotificationBar =  getIntent().getExtras().getBoolean(EXTRA_IS_FROM_NOTIFICATION_BAR_KEY, false);
    }

    if ( isIntentFromNotificationBar ) {
      settings.setUid(getIntent().getStringExtra(EXTRA_DOCUMENTUID_KEY));
      settings.setIsProject(getIntent().getBooleanExtra(EXTRA_IS_PROJECT_KEY,true)) ;
      settings.setMainMenuPosition( 0 );
      settings.setRegNumber(getIntent().getStringExtra(EXTRA_REGISTRATION_NUMBER_KEY));
      settings.setStatusCode(getIntent().getStringExtra(EXTRA_STATUS_CODE_KEY));
      settings.setLoadFromSearch(getIntent().getBooleanExtra(EXTRA_IS_LOAD_FROM_SEARCHE_KEY,true));
      settings.setRegDate(getIntent().getStringExtra(EXTRA_REGISTRATION_DATE_KEY));
      EventBus.getDefault().postSticky( new RemoveAllNotificationEvent());
    }

    clearImageIndex();

    documentUids = getIntent().getStringArrayListExtra(EXTRA_DOCUMENTUIDS_KEY);

    if ( documentUids == null ) {
      documentUids = new ArrayList<>();
    }
  }

  private void initInfoActivity() {
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);

    if ( toolbarManager == null ) {
      toolbarManager = new ToolbarManager(toolbar, this);
    }
    toolbarManager.init();

    setLastSeen();

    status  = JournalStatus.getByName( settings.getStatusCode() );

    setTabContent();
    setPreview();
  }

  private void setTabContent() {

//    try {
//      tabLayout.removeAllTabs();
//      viewPager.removeAllViews();
//    } catch (Exception e) {
//      e.printStackTrace();
//    }

    String type = "TabPagerAdapter";
    if ( status == JournalStatus.SIGNING || status == JournalStatus.APPROVAL || settings.isProject() ){
      type = "TabSigningPagerAdapter";
    }

    FragmentManager fm = getSupportFragmentManager();


    if (viewPagerAdapter != null) {
      Timber.tag(TAG).e("adapter type: %s | type: %s", viewPagerAdapter.getLabel(), type );
//      Timber.tag(TAG).e("adapter type: %s | type: %s", viewPagerAdapter.getLabel(), type );
    }

    if ( viewPagerAdapter != null && Objects.equals(viewPagerAdapter.getLabel(), type)){

      viewPagerAdapter.update();
    } else {
      viewPagerAdapter = Objects.equals(type, "TabPagerAdapter") ? new TabPagerAdapter(fm) : new TabSigningPagerAdapter(fm);
      viewPager.setAdapter(viewPagerAdapter);
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

    String TAG = "DECISION";

    if ( status == JournalStatus.SIGNING || status == JournalStatus.APPROVAL || settings.isProject() ){
      TAG = "ROUTE";
    }

    Timber.tag(TAG).e("TAG: %s", TAG);

    FragmentManager fm = getSupportFragmentManager();

    if ( fm.findFragmentByTag(TAG) == null ){
      Timber.tag(TAG).d("fm.findFragmentByTag(TAG) == null");

      removeAllFragments(fm);

      FragmentTransaction fragmentTransaction = fm.beginTransaction();
      Fragment fragment = Objects.equals(TAG, "DECISION") ? new DecisionPreviewFragment() : new RoutePreviewFragment();
      fragmentTransaction.addToBackStack(TAG);
      fragmentTransaction.replace( R.id.activity_info_preview_container, fragment, TAG );
      fragmentTransaction.commit();
    } else {
      Timber.tag(TAG).d("fm.findFragmentByTag(TAG) != null");
      Fragment preview = fm.findFragmentByTag(TAG);
      ((PreviewFragment) preview).update();
    }


  }

  private void removeAllFragments(FragmentManager fm) {
    while (fm.getBackStackEntryCount() > 0) {
      fm.popBackStackImmediate();
    }
  }

  private void addLoader() {
//    preview_container.removeAllViews();
//
    frame.setVisibility(View.GONE);
//
//    int durationMillis = 300;
//
//    Animation fadeIn = new AlphaAnimation(0, 1);
//    fadeIn.setInterpolator(new DecelerateInterpolator());
//    fadeIn.setDuration(durationMillis);
//
//    Animation fadeOut = new AlphaAnimation(1, 0);
//    fadeOut.setInterpolator(new AccelerateInterpolator());
//    fadeOut.setStartOffset(durationMillis);
//    fadeOut.setDuration(durationMillis);
//
//    AnimationSet animation = new AnimationSet(true);
//    AnimationSet wrapperAnimation = new AnimationSet(true);
//
//    wrapperAnimation.addAnimation(fadeIn);
//    animation.addAnimation(fadeOut);
//
//    frame.setAnimation(animation);
//    preview_container.setAnimation(wrapperAnimation);
//
//    new Handler().postDelayed(() -> frame.setVisibility(View.GONE), durationMillis);

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

    unsubscribe();

    finish();
  }

  public void exitIfAlreadySeenThisDocument() {
    if (Objects.equals(settings.getLastSeenUid(), settings.getUid())){
      Timber.tag(TAG).e("exitIfAlreadySeenThisDocument");
      finish();
    }
  }

  public void setLastSeen(){
    settings.setLastSeenUid( settings.getUid() );
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
      .where(RDecisionEntity.UID.eq( settings.getDecisionActiveUid() ) )
      .get().firstOrNull();

    if (decision != null) {

      Timber.tag(TAG).i("[%s] %s : %s", decision.getId(), decision.getUid(), decision.isChanged() );

        if ( settings.isOnline() ){
          if ( decision.isChanged() != null && decision.isChanged() ){
            Toast.makeText( this, R.string.decision_on_sync_edit_denied, Toast.LENGTH_SHORT).show();
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
//    setPreview();
    EventBus.getDefault().post( new HideTemporaryEvent() );

  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateDocumentEvent event) throws Exception {
    Timber.d("UpdateDocumentEvent");
    updateCurrent();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateCurrentDocumentEvent event) throws Exception {
    Timber.d("UpdateCurrentDocumentEvent");
    updateCurrent();
  }

  private void showPrevDocument() {
    Timber.tag("SHOW_PREV").e("info_act");

    getPrevFromCurrentPosition();
    exitIfAlreadySeenThisDocument();
    clearImageIndex();

    initInfoActivity();
    updateCurrent();
  }

  private void getPrevFromCurrentPosition() {
    getPrevNextDoc(false);
  }

  private void getPrevNextDoc(boolean next) {
    if ( documentUids.size() == 0 ) {
      finish();
    } else {
      int position = settings.getMainMenuPosition();
      position = next ? position + 1 : position - 1;
      position = checkBounds( position );
      setNewDocument( position );
    }
  }

  private int checkBounds(int position) {
    int result = position;

    if ( position < 0 ) {
      result = documentUids.size() - 1;
    }

    if ( position >= documentUids.size() ) {
      result = 0;
    }

    return result;
  }

  private void setNewDocument(int position) {
    InMemoryDocument item = store.getDocuments().get( documentUids.get(position) );

    if ( item != null ) {
      settings.setMainMenuPosition(position);
      settings.setUid(item.getUid());
      settings.setIsProject(item.isProject());
      settings.setRegNumber(item.getDocument().getRegistrationNumber());
      settings.setStatusCode(item.getFilter());
      settings.setRegDate(item.getDocument().getRegistrationDate());
      settings.setIsProject(item.isProject());
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(ShowNextDocumentEvent event){
    Timber.tag(TAG).e("ShowNextDocumentEvent: showing next document");
    showNextDocument();

    Timber.tag(TAG).e("ShowNextDocumentEvent: removing uid from list");
    int index = documentUids.indexOf( event.getUid() );

    if ( index > -1 ) {
      documentUids.remove( index );

      int mainMenuPosition = settings.getMainMenuPosition();
      if ( index < mainMenuPosition ) {
        settings.setMainMenuPosition( mainMenuPosition - 1 );
      }
    }
  }

  private void showNextDocument() {
    Timber.tag("SHOW_NEXT").e("info_act");

    getNextFromCurrentPosition();

    exitIfAlreadySeenThisDocument();

    clearImageIndex();
    initInfoActivity();
    updateCurrent();
  }

  private void getNextFromCurrentPosition() {
    getPrevNextDoc(true);
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
    InMemoryDocument doc = store.getDocuments().get(settings.getUid());

    int time = 600;
    try {
      time = Integer.parseInt(settings.getUpdateTime());
    } catch (NumberFormatException e) {
      Timber.e(e);
    }

    if ( doc != null
        // если док не обработан
        && !doc.isProcessed()
        // или он обработан и время последней команды старше 5 мин
        || ( doc != null && doc.isProcessed() && doc.getUpdatedAt() != null
        && DateUtil.isSomeTimePassed( doc.getUpdatedAt(), time ) )
        // или он из папки Обработанное
        || ( doc != null && doc.getDocument() != null && doc.getDocument().isFromProcessedFolder() )
    ){
      jobManager.addJobInBackground( new UpdateDocumentJob( settings.getUid(), settings.getLogin(), settings.getCurrentUserId() ) );
    }
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

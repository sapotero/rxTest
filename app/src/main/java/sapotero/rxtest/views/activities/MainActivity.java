package sapotero.rxtest.views.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
import sapotero.rxtest.BuildConfig;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.query.DBQueryBuilder;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.adapter.UpdateDocumentAdapterEvent;
import sapotero.rxtest.events.bus.GetDocumentInfoEvent;
import sapotero.rxtest.events.rx.UpdateCountEvent;
import sapotero.rxtest.events.service.SuperVisorUpdateEvent;
import sapotero.rxtest.events.service.UpdateAllDocumentsEvent;
import sapotero.rxtest.events.stepper.auth.StepperDcCheckEvent;
import sapotero.rxtest.events.stepper.auth.StepperDcCheckFailEvent;
import sapotero.rxtest.events.stepper.load.StepperLoadDocumentEvent;
import sapotero.rxtest.events.view.RemoveDocumentFromAdapterEvent;
import sapotero.rxtest.jobs.bus.UpdateAuthTokenJob;
import sapotero.rxtest.managers.DataLoaderManager;
import sapotero.rxtest.services.MainService;
import sapotero.rxtest.utils.FirstRun;
import sapotero.rxtest.utils.queue.QueueManager;
import sapotero.rxtest.views.adapters.DocumentsAdapter;
import sapotero.rxtest.views.adapters.OrganizationAdapter;
import sapotero.rxtest.views.adapters.SearchResultAdapter;
import sapotero.rxtest.views.adapters.decorators.GridSpacingItemDecoration;
import sapotero.rxtest.views.custom.CircleLeftArrow;
import sapotero.rxtest.views.custom.CircleRightArrow;
import sapotero.rxtest.views.custom.OrganizationSpinner;
import sapotero.rxtest.views.custom.SearchView.SearchView;
import sapotero.rxtest.views.menu.MenuBuilder;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements MenuBuilder.Callback, SearchView.OnVisibilityChangeListener {

  @Inject JobManager jobManager;
  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  @Inject QueueManager queue;

  @BindView(R.id.toolbar) Toolbar toolbar;

  @BindView(R.id.documentsRecycleView) RecyclerView rv;
  @BindView(R.id.progressBar) ProgressBar progressBar;
  @BindView(R.id.activity_main_update_progressbar) ProgressBar update_progressbar;

  @BindView(R.id.activity_main_menu) LinearLayout activity_main_menu;

  @BindView(R.id.activity_main_menu_builder_organization) LinearLayout menu_builder_organization;
  @BindView(R.id.activity_main_menu_builder_buttons) FrameLayout menu_builder_buttons;




  @BindView(R.id.DOCUMENT_TYPE) Spinner DOCUMENT_TYPE_SELECTOR;
  @BindView(R.id.ORGANIZATION) OrganizationSpinner ORGANIZATION_SELECTOR;

  @BindView(R.id.activity_main_right_button) CircleRightArrow rightArrow;

  @BindView(R.id.activity_main_left_button) CircleLeftArrow leftArrow;
  @BindView(R.id.favorites_button) CheckBox favorites_button;

  @BindView(R.id.documents_empty_list) TextView documents_empty_list;

  private DataLoaderManager dataLoaderInterface;



  private String TAG = MainActivity.class.getSimpleName();

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> HOST;
  private Preference<String> PASSWORD;
  private Preference<Integer> COUNT;

  private int loaded = 0;

  private OrganizationAdapter organization_adapter;

  private int total = 0;
  private DrawerBuilder drawer;

  private CompositeSubscription subscriptions;
  //  private String total;
  private final int ALL                = 0;
  private final int INCOMING_DOCUMENTS = 1;
  private final int CITIZEN_REQUESTS   = 2;
  private final int APPROVE_ASSIGN     = 3;
  private final int INCOMING_ORDERS    = 4;
  private final int ORDERS             = 5;
  private final int ORDERS_DDO         = 6;
  private final int IN_DOCUMENTS       = 7;
  private final int ON_CONTROL         = 8;
  private final int PROCESSED          = 9;

  private final int FAVORITES          = 10;
  private final int SETTINGS_VIEW_TYPE_APPROVE = 18;
  private final int SETTINGS_VIEW = 20;
  private final int SETTINGS_DECISION_TEMPLATES = 21;
  private final int SETTINGS_LOG = 99;
  private final int SETTINGS_SIGN = 98;

  private final int SETTINGS_REJECTION_TEMPLATES = 22;

  @SuppressLint("StaticFieldLeak")
  public static DocumentsAdapter RAdapter;

  @SuppressLint("StaticFieldLeak")
  public static DBQueryBuilder dbQueryBuilder;

  public  MenuBuilder menuBuilder;
  private DataLoaderManager dataLoader;
  private SearchView searchView;

  private MainActivity context;
  private CompositeSubscription subscription;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);
    loadSettings();
    context = this;

    initAdapters();

    menuBuilder = new MenuBuilder(this);
    menuBuilder
      .withButtonsLayout( menu_builder_buttons )
      .withOrganizationLayout( menu_builder_organization )
      .withOrganizationSelector( ORGANIZATION_SELECTOR )
      .withFavoritesButton( favorites_button )
      .withJournalSelector( DOCUMENT_TYPE_SELECTOR )
      .withUser( LOGIN.get() )
      .registerCallBack(this);
    menuBuilder.build();

    dbQueryBuilder = new DBQueryBuilder(this)
      .withAdapter( RAdapter )
      .withItem(menuBuilder)
      .withOrganizationsAdapter( organization_adapter )
      .withOrganizationSelector( ORGANIZATION_SELECTOR )
      .withEmptyView( documents_empty_list )
      .withRecycleView(rv)
      .withProgressBar( progressBar );

    dataLoader = new DataLoaderManager(this);

    progressBar.setVisibility(ProgressBar.GONE);

    initToolbar();

    initEvents();

    rxSettings();

    initSearch();

    isConnected();

    setFirstRunFalse();

    showLoginScreen();

    updateToken();
  }

  private void setFirstRunFalse() {
    FirstRun firstRun = new FirstRun(settings);

    boolean isFirstRun = firstRun.isFirstRun();
    boolean isSignedWithDc = firstRun.getBooleanFromSettings("SIGN_WITH_DC");

    // If signed with login and password, do not set first run flag to false
    if ( isFirstRun && isSignedWithDc ) {
      firstRun.setFirstRun(false);
    }

    EventBus.getDefault().post( new UpdateAllDocumentsEvent());
  }

  private void updateToken() {
    String sign = settings.getString("START_UP_SIGN").get();
    if (sign == null) {
      sign = "";
    }
    dataLoader.updateAuth(sign);
  }

  private boolean isSkippedSignIn() {
    boolean result;

    String pin = settings.getString("PIN").get();
    if (pin == null) {
      pin = "";
    }

    String password = settings.getString("password").get();
    if (password == null) {
      password = "";
    }

    result = pin.equals("") && password.equals("");

    return result;
  }

  private void showLoginScreen() {
    if ( isSkippedSignIn() ) {
      setFirstRunTrue();
      Intent intent = new Intent(this, LoginActivity.class);
      startActivity(intent);
      finish();
    }
  }

  private void setFirstRunTrue() {
    FirstRun firstRun = new FirstRun(settings);
    firstRun.setFirstRun(true);
  }

  public void isConnected(){
    ReactiveNetwork.observeInternetConnectivity()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(isConnectedToInternet -> {
        try {
          toolbar.getMenu().findItem(R.id.online).setTitle( isConnectedToInternet ? "В сети" : "Не в сети" );
          toolbar.getMenu().findItem(R.id.online).setIcon( isConnectedToInternet  ? R.drawable.icon_online : R.drawable.icon_offline );
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
  }


  private void initSearch() {
    searchView = SearchView.getInstance(this);
    searchView.setOnVisibilityChangeListener(this);
    searchView.setQuery("", false);
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(@NonNull String query) {
        Timber.v("onQueryTextSubmit %s", query);
        return false;
      }

      @Override
      public void onQueryTextChanged(@NonNull String newText) {
        if (newText != null && newText.length() >= 1){
          Timber.v("onQueryTextChanged %s | %s", newText, searchView.getSelected() );

          ArrayList<List<RDocumentEntity>> result = new ArrayList<>();

          // TEST
          //requery не умеет делать OR( u=1 and a=2 ) OR ( u=1 and a=3 )
          if ( searchView.getSelected() != null ){

            boolean[] selected = searchView.getSelected();

            for (int i = 0; i < selected.length; i++) {
              Timber.v("onQueryTextChanged %s | %s", i, selected[i] );
              if ( selected[i] ) {
                switch (i) {
                  case 0:
                    result.add(
                      dataStore
                        .select(RDocumentEntity.class)
                        .where( RDocumentEntity.USER.eq( settings.getString("login").get() ) )
                        .and(RDocumentEntity.REGISTRATION_NUMBER.like("%" + newText + "%"))
                        .and(RDocumentEntity.FROM_PROCESSED_FOLDER.eq(false) )
                        .get().toList()


                    );
                    // .and(RDocumentEntity.REGISTRATION_NUMBER.like("%" + newText + "%"));
                    break;
                  case 1:
                    result.add(
                      dataStore
                        .select(RDocumentEntity.class)
                        .where( RDocumentEntity.USER.eq( settings.getString("login").get() ) )
                        .and(RDocumentEntity.SHORT_DESCRIPTION.like("%" + newText + "%"))
                        .and(RDocumentEntity.FROM_PROCESSED_FOLDER.eq(false) )
                        .get().toList()
                    );
                    // query = query.and(RDocumentEntity.SHORT_DESCRIPTION.like("%" + newText + "%"));
                    break;
                  default:
                    break;
                }
              }
            }
          }

          ArrayList<RDocumentEntity> docs = new ArrayList<>();


          for (List<RDocumentEntity> list: result) {
            Timber.tag(TAG).v("count: %s", list.size());
            for ( RDocumentEntity doc : list ){
              docs.add( doc );
            }
          }

          SearchResultAdapter adapter = new SearchResultAdapter( context, docs );
          searchView.setSuggestionAdapter( adapter );

        }

      }
    });
  }

  private void initAdapters() {
    int columnCount = 2;
    int spacing = 32;

    GridLayoutManager gridLayoutManager = new GridLayoutManager(this, columnCount, GridLayoutManager.VERTICAL, false);

    RAdapter = new DocumentsAdapter(this, new ArrayList<>());

    rv.addItemDecoration(new GridSpacingItemDecoration(columnCount, spacing, true));
    rv.setLayoutManager(gridLayoutManager);
    rv.setAdapter(RAdapter);


    organization_adapter = new OrganizationAdapter(this, new ArrayList<>());
    ORGANIZATION_SELECTOR.setAdapter(organization_adapter, true, selected -> {
      dbQueryBuilder.execute(false);
    });
  }

  private void initToolbar() {


    toolbar.setTitle("СЕРВИС ЭЛЕКТРОННОГО ДОКУМЕНТООБОРОТА");
    toolbar.setSubtitle("МВД России");
    toolbar.setTitleTextColor(getResources().getColor(R.color.md_white_1000));
    toolbar.setSubtitleTextColor(getResources().getColor(R.color.md_white_1000));


    toolbar.setContentInsetStartWithNavigation(250);

    toolbar.inflateMenu(R.menu.activity_main_menu);
    toolbar.setOnMenuItemClickListener(item -> {
      switch (item.getItemId()) {

        case R.id.removeQueue:
          queue.removeAll();
          break;
        case R.id.checkQueue:
          queue.getUncompleteTasks();
          break;

        case R.id.reload:

          dataLoader.updateAuth(null);
          updateByStatus();

//          if (menuBuilder.getItem() != MainMenuItem.PROCESSED || menuBuilder.getItem() != MainMenuItem.FAVORITES ){
//            updateProgressBar();
//          }
          break;
        case R.id.action_search:
          searchView.onOptionsItemSelected(getFragmentManager(), item);
          break;
        default:
          jobManager.addJobInBackground(new UpdateAuthTokenJob());
          break;
      }
      return false;
    });

    if (!settings.getBoolean("debug_enabled").get()){
      toolbar.getMenu().findItem(R.id.removeQueue).setVisible(false);
      toolbar.getMenu().findItem(R.id.checkQueue).setVisible(false);
    }

  }

  private void updateProgressBar() {
    dropLoadProgress(true);

    subscription.add(
    Observable
      .interval( 1, TimeUnit.SECONDS)
      .subscribeOn(AndroidSchedulers.mainThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe( data-> {
        int value = update_progressbar.getProgress();
        update_progressbar.setProgress( value + 1 );

        Timber.tag(TAG).w("TICK %s > %s = %s", getLoadedDocumentsPercent() , value, getLoadedDocumentsPercent() > value);
        if ( getLoadedDocumentsPercent() >= value  && getLoadedDocumentsPercent() > 0){
          subscription.unsubscribe();
        }


      })
    );

  }

  private void dropLoadProgress(Boolean visible) {
    loaded = 0;

    if (subscription != null) {
      subscription.clear();
    }

    if ( update_progressbar != null){
      update_progressbar.setProgress(0);
      update_progressbar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
  }

  private void initEvents() {
    Timber.tag(TAG).v("initEvents");
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);


    Intent serviceIntent = new Intent(this, MainService.class);
    if(startService(serviceIntent) != null) {
//      Toast.makeText(getBaseContext(), "Service is already running", Toast.LENGTH_SHORT).show();
    } else {
      EventBus.getDefault().post(new SuperVisorUpdateEvent());
    }

    if (subscription == null){
      subscription = new CompositeSubscription();
    }
  }

  private void updateByStatus() {
    dataLoader.updateByCurrentStatus( menuBuilder.getItem(), null );

    Toast.makeText(this, "Обновление данных...", Toast.LENGTH_SHORT).show();

    menuBuilder.build();

  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();

    initEvents();

    Timber.tag(TAG).v("onResume");
    invalidate();

    menuBuilder.getItem().recalcuate();
    dropLoadProgress(false);

//    EventBus.getDefault().post( new UpdateAllDocumentsEvent());

  }

  public static void invalidate(){
    RAdapter.clear();
    dbQueryBuilder.execute(true);
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (subscriptions != null) {
      subscriptions.unsubscribe();
    }
  }

  @Override
  public void onStop() {
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    super.onStop();
  }

  private void setJournalType(int type) {
    menuBuilder.selectJournal( type );
  }

  private void drawer_build_bottom() {
    String version = BuildConfig.VERSION_NAME;

    drawer
      .addDrawerItems(

        new SectionDrawerItem().withName(R.string.drawer_item_settings),

        new SecondaryDrawerItem()
          .withName(R.string.drawer_item_settings_account)
          .withIcon(MaterialDesignIconic.Icon.gmi_accounts)
          .withIdentifier(SETTINGS_VIEW),
        new SecondaryDrawerItem()
          .withName(R.string.drawer_item_settings_templates)
          .withIcon(MaterialDesignIconic.Icon.gmi_comment_edit)
          .withIdentifier(SETTINGS_DECISION_TEMPLATES)
      );

    if (settings.getBoolean("debug_enabled").get()){
      drawer
        .addDrawerItems(
          new SectionDrawerItem().withName(R.string.drawer_item_debug),
          new SecondaryDrawerItem()
            .withIdentifier(SETTINGS_LOG)
            .withIcon(MaterialDesignIconic.Icon.gmi_assignment)
            .withName("Лог"),
          new SecondaryDrawerItem()
            .withIdentifier(SETTINGS_SIGN)
            .withIcon(MaterialDesignIconic.Icon.gmi_dns)
            .withName("Подписи ЭО"),
          new DividerDrawerItem()
        );
    }

    drawer
      .addDrawerItems(
        new SecondaryDrawerItem()
        .withName("Версия приложения: " + version )
        .withSelectable(false));

      drawer.withOnDrawerItemClickListener(
        (view, position, drawerItem) -> {

          Timber.tag(TAG).d("drawerItem.getIdentifier(): " + drawerItem.getIdentifier());

          Class<?> activity = null;

          switch ((int) drawerItem.getIdentifier()) {
            case ALL:
              setJournalType(ALL);
              break;
            case INCOMING_DOCUMENTS:
              setJournalType(INCOMING_DOCUMENTS);
              break;
            case CITIZEN_REQUESTS:
              setJournalType(CITIZEN_REQUESTS);
              break;
            case APPROVE_ASSIGN:
              setJournalType(APPROVE_ASSIGN);
              break;
            case INCOMING_ORDERS:
              setJournalType(INCOMING_ORDERS);
              break;
            case ORDERS:
              setJournalType(ORDERS);
              break;
            case ORDERS_DDO:
              setJournalType(ORDERS_DDO);
              break;
            case IN_DOCUMENTS:
              setJournalType(IN_DOCUMENTS);
              break;
            case ON_CONTROL:
              setJournalType(ON_CONTROL);
              break;
            case PROCESSED:
              setJournalType(PROCESSED);
              break;
            case FAVORITES:
              setJournalType(FAVORITES);
              break;
            case SETTINGS_VIEW_TYPE_APPROVE:
              setJournalType(8);
              break;

            case SETTINGS_VIEW:
              activity = SettingsActivity.class;
              break;
            case SETTINGS_DECISION_TEMPLATES:
              activity = SettingsTemplatesActivity.class;
              break;
            case SETTINGS_REJECTION_TEMPLATES:
              activity = SettingsTemplatesActivity.class;
              break;
            case SETTINGS_LOG:
              activity = LogActivity.class;
              break;
            case SETTINGS_SIGN:
              activity = FileSignActivity.class;
              break;
            default:
              activity = null;
              break;
          }


          Timber.tag(TAG).i(String.valueOf(view));
          Timber.tag(TAG).i(String.valueOf(position));
          Timber.tag(TAG).i(String.valueOf(drawerItem.getIdentifier()));

          if (activity != null) {
            Intent intent = new Intent(this, activity);
            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
            startActivity(intent, bundle);
          }

          return false;

        }
      )
      .build();
  }

  private void drawer_build_head() {


    AccountHeader headerResult = new AccountHeaderBuilder()
      .withActivity(this)
      .withHeaderBackground(R.drawable.header)
      .addProfiles(
        new ProfileDrawerItem()
          .withName(  settings.getString("current_user_organization").get() )
          .withEmail( settings.getString("current_user").get() )
          .withSetSelected(true)
          .withIcon(R.drawable.gerb)
      )
      .withOnAccountHeaderListener(
        (view, profile, currentProfile) -> false
      )
      .build();

    if (drawer == null) {
      drawer = new DrawerBuilder()
        .withActivity(this)
        .withToolbar(toolbar)
        .withActionBarDrawerToggle(true)
        .withHeader(R.layout.drawer_header)
//        .withShowDrawerOnFirstLaunch(true)
        .withAccountHeader(headerResult);
    }

    drawer.addDrawerItems(
      new SectionDrawerItem().withName(R.string.drawer_item_journals)
    );
  }

  private void loadSettings() {
    LOGIN = settings.getString("login");
    PASSWORD = settings.getString("password");
    TOKEN = settings.getString("token");
    HOST = settings.getString("settings_username_host");
    COUNT = settings.getInteger("documents.count");
  }

  private void drawer_add_item(int index, String title, Long identifier) {
    Timber.tag("drawer_add_item").v(" !index " + index + " " + title);

    drawer.addDrawerItems(
      new PrimaryDrawerItem()
        .withName(title)
        .withIdentifier(identifier)
    );
  }

  public void rxSettings() {
    drawer_build_head();

    Map<Integer, String> map = new HashMap<>();
    int index = 0;

    Preference<Set<String>> set = settings.getStringSet("settings_view_journals");
    for (String journal : set.get()) {
      index = Arrays.asList((getResources().getStringArray(R.array.settings_view_start_page_values))).indexOf(journal);
      map.put(index, journal);
    }

    Map<Integer, String> treeMap = new TreeMap<>(map);
    String[] identifier = (getResources().getStringArray(R.array.settings_view_start_page_identifier));
    String[] title = (getResources().getStringArray(R.array.settings_view_start_page));

    for(Fields.Menu menu: Fields.Menu.values() ){
      drawer_add_item( menu.getIndex() , menu.getTitle(), Long.valueOf( menu.getIndex()) );
    }

    drawer_build_bottom();
  }



  @OnClick(R.id.activity_main_left_button)
  public void setLeftArrowArrow() {
    menuBuilder.showPrev();
  }

  @OnClick(R.id.activity_main_right_button)
  public void setRightArrow() {
    menuBuilder.showNext();
  }



  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(GetDocumentInfoEvent event) {
//    Toast.makeText(getApplicationContext(), event.message, Toast.LENGTH_SHORT).show();
  }

//  @Subscribe(threadMode = ThreadMode.MAIN)
//  public void onMessageEvent(InsertRxDocumentsEvent event) {
////    Toast.makeText(getApplicationContext(), event.uis, Toast.LENGTH_SHORT).show();
//  }

//  @Subscribe(threadMode = ThreadMode.MAIN)
//  public void onMessageEvent(UpdateDocumentJobEvent event) {
//    int position = RAdapter.getPositionByUid(event.uid);
//    RecyclerView.ViewHolder a = rv.findViewHolderForAdapterPosition(position);
//
//    int visibility = event.value ? View.VISIBLE : View.GONE;
//    int field = Objects.equals(event.field, "control") ? R.id.control_label : R.id.favorite_label;
//
//    View view = a.itemView.findViewById(field);
//    view.setVisibility(visibility);
//  }


  @Subscribe( threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateCountEvent event) {
    Timber.tag(TAG).v("UpdateCountEvent");
    menuBuilder.getItem().recalcuate();
  }

  @Subscribe( threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateDocumentAdapterEvent event) {
    Timber.tag(TAG).v("UpdateDocumentAdapterEvent");
    dbQueryBuilder.invalidateDocumentEvent(event);
  }



  @RequiresApi(api = Build.VERSION_CODES.M)
  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onMessageEvent(RemoveDocumentFromAdapterEvent event) {
    Timber.tag(TAG).v("RemoveDocumentFromAdapterEvent %s", event.uid );

//    if ( !IS_HIDDEN.containsKey(event.uid) ){
//      IS_HIDDEN.put(event.uid, true);
//    }
//    RAdapter.removeItem(event.uid);

//    menuBuilder.update();

  }

  /* MenuBuilder.Callback */
  @Override
  public void onMenuBuilderUpdate(ArrayList<ConditionBuilder> conditions) {
    menuBuilder.setFavorites( dbQueryBuilder.getFavoritesCount() );
    dbQueryBuilder.executeWithConditions( conditions, menuBuilder.getItem().isVisible() && favorites_button.isChecked(), menuBuilder.getItem() );
  }


  @Override
  public void onUpdateError(Throwable error) {

  }



  @Override
  public void onShow() {
    Timber.v("onShow");
  }

  @Override
  public void onDismiss() {
    Timber.v("onDismiss");
  }


  /* DOCUMENT COUNT UPDATE */
  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(StepperLoadDocumentEvent event) throws Exception {
    loaded++;

    int perc = getLoadedDocumentsPercent();

    if (update_progressbar != null) {
      if ( update_progressbar.getProgress() < perc ){
        update_progressbar.setProgress( perc );
      }
    }

    if ( update_progressbar != null && perc == 100f ){
      update_progressbar.setVisibility(View.GONE);
    }
  }

  private int getLoadedDocumentsPercent() {
    if ( COUNT.get() == null ){
      COUNT.set(1);
    }

    float result = 100f * loaded / COUNT.get();
    if (result > 100 ){
      result = 100f;
    }

    return (int) Math.ceil(result);
  }

}

package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.BuildConfig;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RColleagueEntity;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.query.DBQueryBuilder;
import sapotero.rxtest.db.requery.utils.DocumentStateSaver;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.adapter.JournalSelectorIndexEvent;
import sapotero.rxtest.events.rx.UpdateCountEvent;
import sapotero.rxtest.events.service.CheckNetworkEvent;
import sapotero.rxtest.events.utils.ErrorReceiveTokenEvent;
import sapotero.rxtest.events.utils.LoadedFromDbEvent;
import sapotero.rxtest.events.utils.RecalculateMenuEvent;
import sapotero.rxtest.events.utils.ReceivedTokenEvent;
import sapotero.rxtest.events.view.UpdateDrawerEvent;
import sapotero.rxtest.jobs.bus.UpdateAuthTokenJob;
import sapotero.rxtest.managers.DataLoaderManager;
import sapotero.rxtest.retrofit.Api.AuthService;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import sapotero.rxtest.services.MainService;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.queue.QueueManager;
import sapotero.rxtest.views.adapters.DocumentsAdapter;
import sapotero.rxtest.views.adapters.OrganizationAdapter;
import sapotero.rxtest.views.adapters.SearchResultAdapter;
import sapotero.rxtest.views.adapters.decorators.GridSpacingItemDecoration;
import sapotero.rxtest.views.custom.CircleLeftArrow;
import sapotero.rxtest.views.custom.CircleRightArrow;
import sapotero.rxtest.views.custom.OrganizationSpinner;
import sapotero.rxtest.views.custom.SearchView.SearchView;
import sapotero.rxtest.views.custom.spinner.JournalSelectorView;
import sapotero.rxtest.views.menu.MenuBuilder;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements MenuBuilder.Callback, SearchView.OnVisibilityChangeListener {

  @Inject JobManager jobManager;
  @Inject ISettings settings;
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject QueueManager queue;
  @Inject MemoryStore store;
  @Inject OkHttpClient okHttpClient;

  @BindView (R.id.toolbar)                          Toolbar             toolbar;
  @BindView (R.id.documentsRecycleView)             RecyclerView        rv;
  @BindView (R.id.progressBar)                      ProgressBar         progressBar;
  @BindView (R.id.activity_main_update_progressbar) ProgressBar         update_progressbar;
  @BindView (R.id.activity_main_menu)               LinearLayout        activity_main_menu;
  @BindView (R.id.activity_main_wrapper)            RelativeLayout      wrapper;
  @BindView (R.id.activity_main_menu_organization)  LinearLayout        menu_builder_organization;
  @BindView (R.id.activity_main_menu_buttons)       FrameLayout         menu_builder_buttons;
  @BindView (R.id.DOCUMENT_TYPE)                    Spinner             DOCUMENT_TYPE_SELECTOR;
  @BindView (R.id.ORGANIZATION)                     OrganizationSpinner ORGANIZATION_SELECTOR;
  @BindView (R.id.activity_main_right_button)       CircleRightArrow    rightArrow;
  @BindView (R.id.activity_main_left_button)        CircleLeftArrow     leftArrow;
  @BindView (R.id.favorites_button)                 CheckBox            favorites_button;
  @BindView (R.id.documents_empty_list)             TextView            documents_empty_list;

  @BindView (R.id.activity_main_journal_selector)   JournalSelectorView journalSelector;

  private String TAG = MainActivity.class.getSimpleName();
  private OrganizationAdapter organization_adapter;
  private DrawerBuilder drawer;

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

  public DocumentsAdapter RAdapter;

  public  DBQueryBuilder dbQueryBuilder;
  public  MenuBuilder menuBuilder;
  private DataLoaderManager dataLoader;
  private SearchView searchView;
  private MainActivity context;
  private CompositeSubscription subscription;
  private CompositeSubscription subscriptionSubstituteMode;
  private PublishSubject<Integer> searchSubject = PublishSubject.create();
  private Subscription searchSubjectSubscription;

  private int menuIndex;

  private List<RColleagueEntity> colleagues;
  private MaterialDialog startSubstituteDialog;
  private MaterialDialog stopSubstituteDialog;

  private boolean switchToSubstituteModeStarted = false;
  private boolean exitFromSubstituteModeStarted = false;

  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.bind(this);
    EsdApplication.getManagerComponent().inject(this);
    context = this;
    searchSubject = PublishSubject.create();

    initAdapters();

    unregisterEventBus();
    EventBus.getDefault().register(this);

    menuBuilder = new MenuBuilder(this);
    menuBuilder
      .withButtonsLayout( menu_builder_buttons )
      .withOrganizationLayout( menu_builder_organization )
      .withOrganizationSelector( ORGANIZATION_SELECTOR )
      .withFavoritesButton( favorites_button )
      .withJournalSelector( DOCUMENT_TYPE_SELECTOR )
      .withUser( settings.getLogin() )
      .registerCallBack(this);
    menuBuilder.build();

    dbQueryBuilder = new DBQueryBuilder()
      .withAdapter( RAdapter )
      .withOrganizationsAdapter( organization_adapter )
      .withOrganizationSelector( ORGANIZATION_SELECTOR )
      .withEmptyView( documents_empty_list )
      .withProgressBar( progressBar );

    dataLoader = new DataLoaderManager(this);

    initToolbar();

    initSearchSub();
    initSearch();

    setFirstRunFalse();

    updateToken();
  }

  private void setFirstRunFalse() {
    boolean isFirstRun = settings.isFirstRun();
    boolean isSignedWithDc = settings.isSignedWithDc();

    // If signed with login and password, do not set first run flag to false
    if ( isFirstRun && isSignedWithDc ) {
      settings.setFirstRun(false);
    }

  }

  private void updateToken() {
    String sign = settings.getSign();
    if (sign == null) {
      sign = "";
    }
    dataLoader.updateAuth(sign, false);
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
        if (newText.length() >= 1){
          Timber.v("onQueryTextChanged %s | %s", newText, searchView.getSelected() );

          ArrayList<List<RDocumentEntity>> result = new ArrayList<>();

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
                        .where( RDocumentEntity.USER.eq( settings.getLogin() ) )
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
                        .where( RDocumentEntity.USER.eq( settings.getLogin() ) )
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
            Timber.tag(TAG).v("put: %s", list.size());
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
    if (settings.isFirstRun()){
      EventBus.getDefault().removeStickyEvent(LoadedFromDbEvent.class);
      store.clearAndLoadFromDb();
    }

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

  private void updateCount(){
    journalSelector.updateCounter();
    menuBuilder.getItemsBuilder().updateView();
  }

  private void initToolbar() {


    toolbar.setTitle("СЕРВИС ЭЛЕКТРОННОГО ДОКУМЕНТООБОРОТА");
    toolbar.setSubtitle("МВД России");
    toolbar.setTitleTextColor(getResources().getColor(R.color.md_white_1000));
    toolbar.setSubtitleTextColor(getResources().getColor(R.color.md_white_1000));


    toolbar.setContentInsetStartWithNavigation(250);

    toolbar.inflateMenu(R.menu.activity_main_menu);

    setToolbarClickListener();

    if (!settings.isDebugEnabled()){
      toolbar.getMenu().findItem(R.id.removeQueue).setVisible(false);
      toolbar.getMenu().findItem(R.id.checkQueue).setVisible(false);
    }

  }


  private void setEmptyToolbarClickListener() {
    toolbar.setOnMenuItemClickListener(null);
  }

  private void setToolbarClickListener() {
    toolbar.setOnMenuItemClickListener(item -> {
      switch (item.getItemId()) {

        case R.id.substituteModeLabel:
          // Do nothing, it's just a label
          break;

        case R.id.removeQueue:
          queue.removeAll();
          break;
        case R.id.checkQueue:
          queue.getUncompleteTasks();
          break;

        case R.id.reload:

          Toast.makeText(this, "Обновление данных...", Toast.LENGTH_SHORT).show();
          dataLoader.updateAuth(null, true);

//          if (menuBuilder.getItem() != MainMenuItem.PROCESSED || menuBuilder.getItem() != MainMenuItem.FAVORITES ){
//            updateProgressBar();
//          }
          break;
        case R.id.action_search:
          if (searchView == null || searchSubjectSubscription == null || searchSubject == null) {
            initSearchSub();
            initSearch();
          }

          searchSubject.onNext( item.getItemId() );
//            searchView.onOptionsItemSelected(getFragmentManager(), item);

//          searchView.onOptionsItemSelected(getFragmentManager(), item);
//          setEmptyToolbarClickListener();
          break;
        default:
          jobManager.addJobInBackground(new UpdateAuthTokenJob(settings.getLogin()));
          break;
      }
      return false;
    });
  }

  private void initSearchSub(){

    if (searchSubjectSubscription != null){
      searchSubjectSubscription.unsubscribe();
    }

    searchSubjectSubscription = searchSubject
      .buffer( 500, TimeUnit.MILLISECONDS )
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        data -> {
          if (data.size() > 0){
            searchView.onOptionsItemSelected(getFragmentManager(), data.get(0));
          }
        },
        Timber::e
      );
  }

  private void initService() {
    Timber.tag(TAG).v("initService");
    Intent serviceIntent = MainService.newIntent(this, false);
    startService(serviceIntent);
  }

  private void updateByStatus() {
    dataLoader.updateByCurrentStatus( menuBuilder.getItem(), null, settings.getLogin(), settings.getCurrentUserId());
  }


  @Override
  public void onResume() {
    super.onResume();
    initSearchSub();
    initSearch();


    initService();
    startNetworkCheck();
    subscribeToNetworkCheckResults();
    subscribeToSubstituteModeResults();

    update( true );

    initDrawer();


//    EventBus.getDefault().post( new RecalculateMenuEvent());

  }

  private void startNetworkCheck() {
    EventBus.getDefault().postSticky(new CheckNetworkEvent( true ));
  }

  private void stopNetworkCheck() {
    EventBus.getDefault().postSticky(new CheckNetworkEvent( false ));
  }

  // resolved https://tasks.n-core.ru/browse/MVDESD-13314
  // Обновление иконки В сети / Не в сети
  private void subscribeToNetworkCheckResults() {
    unsubscribe();
    subscription = new CompositeSubscription();

    subscription.add(
      settings.getOnlinePreference()
        .asObservable()
        .subscribe(
          isOnline -> {
            boolean isConnectedToInternet = isOnline != null ? isOnline : false;
            try {
              toolbar.getMenu().findItem(R.id.online).setTitle( isConnectedToInternet ? R.string.is_online : R.string.is_offline );
              toolbar.getMenu().findItem(R.id.online).setIcon(  isConnectedToInternet ? R.drawable.icon_online : R.drawable.icon_offline );
            } catch (Exception e) {
              Timber.tag(TAG).e(e);
            }
          },
          Timber::e
        )
    );
  }

  private void subscribeToSubstituteModeResults() {
    unsubscribeSubstituteMode();
    subscriptionSubstituteMode = new CompositeSubscription();

    subscriptionSubstituteMode.add(
      settings.getSubstituteModePreference()
        .asObservable()
        .subscribe(
          result -> {
            boolean isSubstituteMode = result != null ? result : false;
            try {
              toolbar.getMenu().findItem(R.id.substituteModeLabel).setVisible( isSubstituteMode );
            } catch (Exception e) {
              Timber.tag(TAG).e(e);
            }
          },
          Timber::e
        )
    );
  }

  private void updateOrganizationFilter() {
    if ( settings.isOrganizationFilterActive() ) {
      try {
        Set<String> oldFilterSelection = settings.getOrganizationFilterSelection();
        ORGANIZATION_SELECTOR.setSelected(oldFilterSelection);
      } catch (Exception e) {
        Timber.tag(TAG).e(e);
      }
    }
  }

  public void update(boolean reloadDocuments) {
    settings.setTabChanged( reloadDocuments );
    updateCount();
    updateOrganizationFilter();
  }

  private void unsubscribe() {
    if ( subscription != null && subscription.hasSubscriptions() ) {
      subscription.unsubscribe();
    }
  }

  private void unsubscribeSubstituteMode() {
    if ( subscriptionSubstituteMode != null && subscriptionSubstituteMode.hasSubscriptions() ) {
      subscriptionSubstituteMode.unsubscribe();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    stopNetworkCheck();
    unsubscribe();
    unsubscribeSubstituteMode();
  }

  @Override
  protected void onDestroy() {
    // Reset previous state of organization filter and set tab changed on application quit
    settings.setOrganizationFilterActive( false );
    settings.setTabChanged(true);

    unregisterEventBus();

    super.onDestroy();
  }

  private void unregisterEventBus() {
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
  }

  private void setJournalType(int type, boolean reloadDocuments) {
    // Reset previous state of organization filter and set tab changed
    settings.setOrganizationFilterActive(false);
    settings.setTabChanged(reloadDocuments);

    menuBuilder.selectJournal( type );
    journalSelector.selectJournal(type);
  }

  private void drawer_build_bottom() {
    String version = BuildConfig.VERSION_NAME;

    drawer
      .addDrawerItems(
        new DividerDrawerItem(),
        new PrimaryDrawerItem()
          .withName(R.string.drawer_item_settings)
          .withIdentifier(SETTINGS_VIEW),
        new PrimaryDrawerItem()
          .withName(R.string.drawer_item_settings_templates)
          .withIdentifier(SETTINGS_DECISION_TEMPLATES)
      );

    if (settings.isDebugEnabled()){
      drawer
        .addDrawerItems(
          new DividerDrawerItem(),
          new PrimaryDrawerItem()
            .withIdentifier(SETTINGS_LOG)
            .withName(R.string.drawer_item_settings_log),
          new PrimaryDrawerItem()
            .withIdentifier(SETTINGS_SIGN)
            .withName(R.string.drawer_item_settings_signatures),
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
              setJournalType(ALL, true);
              break;
            case INCOMING_DOCUMENTS:
              setJournalType(INCOMING_DOCUMENTS, true);
              break;
            case CITIZEN_REQUESTS:
              setJournalType(CITIZEN_REQUESTS, true);
              break;
            case APPROVE_ASSIGN:
              setJournalType(APPROVE_ASSIGN, true);
              break;
            case INCOMING_ORDERS:
              setJournalType(INCOMING_ORDERS, true);
              break;
            case ORDERS:
              setJournalType(ORDERS, true);
              break;
            case ORDERS_DDO:
              setJournalType(ORDERS_DDO, true);
              break;
            case IN_DOCUMENTS:
              setJournalType(IN_DOCUMENTS, true);
              break;
            case ON_CONTROL:
              setJournalType(ON_CONTROL, true);
              break;
            case PROCESSED:
              setJournalType(PROCESSED, true);
              break;
            case FAVORITES:
              setJournalType(FAVORITES, true);
              break;
            case SETTINGS_VIEW_TYPE_APPROVE:
              setJournalType(8, true);
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

    // resolved https://tasks.n-core.ru/browse/MVDESD-13752
    // Добавить в боковую панель список коллег
    List<RColleagueEntity> colleaguesFromDB = dataStore
      .select(RColleagueEntity.class)
      .where(RColleagueEntity.USER.eq( settings.getLogin() ))
      .and(RColleagueEntity.ACTIVED.eq(true))
      .get().toList();

    colleagues = new ArrayList<>();
    colleagues.addAll( colleaguesFromDB );

    Collections.sort(colleagues, (o1, o2) -> o1.getSortIndex() != null && o2.getSortIndex() != null ? o1.getSortIndex().compareTo( o2.getSortIndex() ) : 0 );

    IProfile[] profiles;

    // resolved https://tasks.n-core.ru/browse/MVDESD-12618
    // Режим замещения
    if ( !settings.isSubstituteMode() ) {
      // Не в режиме замещения в заголовке отображается основной пользователь,
      // а в списке аккаунтов - список коллег, которых можно замещать
      profiles = new ProfileDrawerItem[ colleagues.size() + 1 ];

      addMainProfile( profiles );

      int i = 1;

      for (int colleagueIndex = 0; colleagueIndex < colleagues.size(); colleagueIndex++) {
        String colleagueName = splitName( colleagues.get(colleagueIndex).getOfficialName() );

        profiles[i] = new ProfileDrawerItem()
          .withName( colleagueName )
          .withIdentifier( colleagueIndex )
          .withSelectable( false )
          .withSetSelected( false )
          .withOnDrawerItemClickListener((view, position, drawerItem) -> {
            int index = (int) drawerItem.getIdentifier();
            // Переход в режим замещения, если вошли по пину и не показвыать окно авторизации после рестарта
            // и не в режиме замещения и не ожидаем получения токена
            if ( settings.isSignedWithDc() && !settings.isFirstRun() ) {
              if ( !settings.isSubstituteMode() && !settings.isUpdateAuthStarted() ) {
                startSubstituteMode( index );
              } else {
                Toast.makeText(this, "Невозможно войти в режим замещения: дождитесь обновления данных", Toast.LENGTH_SHORT).show();
              }
            }
            return false;
          })
          .withIcon( R.drawable.gerb );

        i++;
      }

    } else {
      // В режиме замещения в заголовке отображается коллега, которого пользователь замещает,
      // а в списке аккаунтов - основной пользователь
      profiles = new ProfileDrawerItem[ 2 ];

      addMainProfile( profiles );

      Bitmap userImage = getUserImage( settings.getOldCurrentUserImage() );

      ProfileDrawerItem profileDrawerItem = new ProfileDrawerItem()
        .withName( settings.getOldCurrentUser() )
        .withSelectable( false )
        .withSetSelected( false )
        .withOnDrawerItemClickListener((view, position, drawerItem) -> {
          // Выход из режима замещения, если в режиме замещения и не ожидаем получения токена
          if ( settings.isSubstituteMode() && !settings.isUpdateAuthStarted() ) {
            stopSubstituteMode();
          } else {
            Toast.makeText(this, "Невозможно выйти из режима замещения: дождитесь обновления данных", Toast.LENGTH_SHORT).show();
          }
          return false;
        });

      if ( userImage != null ) {
        profileDrawerItem.withIcon( userImage );
      } else {
        profileDrawerItem.withIcon( R.drawable.gerb );
      }

      profiles[1] = profileDrawerItem;
    }

    AccountHeader headerResult = new AccountHeaderBuilder()
      .withActivity(this)
      .withHeaderBackground(R.drawable.header)
      .addProfiles( profiles )
      .withOnAccountHeaderListener(
        (view, profile, currentProfile) -> false
      )
      .build();

    drawer = new DrawerBuilder()
      .withActivity(this)
      .withToolbar(toolbar)
      .withActionBarDrawerToggle(true)
      .withHeader(R.layout.drawer_header)
//        .withShowDrawerOnFirstLaunch(true)
      .withAccountHeader(headerResult);

    drawer.addDrawerItems(
      new SectionDrawerItem().withName(R.string.drawer_item_journals)
    );
  }

  private void addMainProfile(IProfile[] profiles) {
    Bitmap userImage = getUserImage( settings.getCurrentUserImage() );

    ProfileDrawerItem profileDrawerItem = new ProfileDrawerItem()
      .withName( settings.getCurrentUserOrganization() )
      .withEmail( settings.getCurrentUser() )
      .withSetSelected( true );

    if ( userImage != null ) {
      profileDrawerItem.withIcon( userImage );
    } else {
      profileDrawerItem.withIcon( R.drawable.gerb );
    }

    profiles[0] = profileDrawerItem;
  }

  // resolved https://tasks.n-core.ru/browse/MVDESD-12618
  // Отображать фото пользователя
  private Bitmap getUserImage(String imageString) {
    Bitmap imageBitmap = null;

    if ( imageString != null && !Objects.equals(imageString, "" ) ) {
      try {
        String str = imageString.replaceAll("(\\n)", "");
        byte[] decodedString = Base64.decode(str.getBytes(), Base64.DEFAULT);
        imageBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
      } catch (Exception e) {
        Timber.tag(TAG).e(e);
      }
    }

    return imageBitmap;
  }

  private String splitName(String nameToSplit) {
    String name = nameToSplit;

    try {
      String[] split = name.split(" ");

      if ( split.length >= 2 ){
        String part1 = split[0];
        String part2 = split[1];

        if (part2 != null && part2.contains(".")) {
          name = String.format("%s %s", part1, part2);
        }
      }
    } catch (Exception error) {
      Timber.tag(TAG).d("Error splitting colleague name: %s", nameToSplit);
    }

    return name;
  }

  private String splitOrganization(String nameToSplit) {
    String name = nameToSplit;
    String organization = "";

    try {
      String[] split = name.split("\\(");

      if ( split.length >= 2 ){
        String part2 = split[1];

        if (part2 != null) {
          if ( part2.contains(")") ) {
            part2 = part2.substring( 0, part2.lastIndexOf(")") - 1 );
          }

          organization = part2;
        }
      }
    } catch (Exception error) {
      Timber.tag(TAG).d("Error splitting colleague organization: %s", nameToSplit);
    }

    return organization;
  }

  private void startSubstituteMode(int colleagueIndex) {
    if ( settings.isOnline() ) {
      if ( colleagues != null && colleagueIndex < colleagues.size() ) {
        if ( queue.isAllTasksComplete() ) {
          Timber.tag("Substitute").d("Starting substitute mode");

          showStartSubstituteDialog();

          RColleagueEntity colleagueEntity = colleagues.get( colleagueIndex );

          Retrofit retrofit = new RetrofitManager(context, settings.getHost(), okHttpClient).process();
          AuthService auth = retrofit.create(AuthService.class);

          auth.switchToColleague(colleagueEntity.getColleagueId(), settings.getLogin(), settings.getToken())
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( colleagueResponse -> {
              Timber.tag("Substitute").d("Received colleague token");

              jobManager.cancelJobsInBackground(null, TagConstraint.ANY, "DocJob");

              switchToSubstituteModeStarted = true;

              settings.setSubstituteMode( true );
              settings.setOldLogin( settings.getLogin() );
              settings.setOldCurrentUserId( settings.getCurrentUserId() );
              settings.setOldCurrentUser( settings.getCurrentUser() );
              settings.setOldCurrentUserOrganization( settings.getCurrentUserOrganization() );
              settings.setOldCurrentUserImage( settings.getCurrentUserImage() );
              settings.setLogin( colleagueResponse.getLogin() );
              settings.setToken( colleagueResponse.getAuthToken() );
              settings.setCurrentUserId( colleagueResponse.getOfficialId() );
              settings.setColleagueId( colleagueEntity.getColleagueId() );

              settings.setCurrentUser( splitName( colleagueEntity.getOfficialName() ) );
              settings.setCurrentUserOrganization( splitOrganization( colleagueEntity.getOfficialName() ) );
              settings.setCurrentUserImage("");
              initDrawer();

              dataLoader.unsubcribeAll();

              // Сначала сохраняем/восстанавливаем состояние тех документов, которые имеются у обоих пользователей,
              // затем перезагружаем MemoryStore
              switchDocuments();
              store.clearAndLoadFromDb();

            }, error -> {
              Timber.tag(TAG).e(error);
              dismissStartSubstituteDialog();
              Toast.makeText(this, "Ошибка входа в режим замещения", Toast.LENGTH_SHORT).show();
            });

        } else {
          Toast.makeText(this, "Невозможно войти в режим замещения: дождитесь обработки очереди запросов", Toast.LENGTH_SHORT).show();
        }
      }
    } else {
      Toast.makeText(this, "Невозможно войти в режим замещения в оффлайне", Toast.LENGTH_SHORT).show();
    }
  }

  private void switchDocuments() {
    new DocumentStateSaver().saveRestoreDocumentStates( settings.getLogin(), settings.getOldLogin(), TAG );
  }

  private void stopSubstituteMode() {
    if ( settings.isOnline() ) {
      if ( queue.isAllTasksComplete() ) {
        Timber.tag("Substitute").d("Stopping substitute mode");

        jobManager.cancelJobsInBackground(null, TagConstraint.ANY, "DocJob");

        exitFromSubstituteModeStarted = true;

        settings.setSubstituteMode( false );
        switchUser();

        dataLoader.unsubcribeAll();
        showStopSubstituteDialog();

        dataLoader.updateAuth(null, true);

      } else {
        Toast.makeText(this, "Невозможно выйти из режима замещения: дождитесь обработки очереди запросов", Toast.LENGTH_SHORT).show();
      }
    } else {
      Toast.makeText(this, "Невозможно выйти из режима замещения в оффлайне", Toast.LENGTH_SHORT).show();
    }
  }

  private void switchUser() {
    // Меняем логин и сразу сохраняем/восстанавливаем состояние тех документов, которые имеются у обоих пользователей,
    // и обновляем боковое меню
    swapLogin();
    switchDocuments();
    initDrawer();
  }

  private void swapLogin() {
    String temp = settings.getLogin();
    settings.setLogin( settings.getOldLogin() );
    settings.setOldLogin( temp );

    temp = settings.getCurrentUserId();
    settings.setCurrentUserId( settings.getOldCurrentUserId() );
    settings.setOldCurrentUserId( temp );

    temp = settings.getCurrentUser();
    settings.setCurrentUser( settings.getOldCurrentUser() );
    settings.setOldCurrentUser( temp );

    temp = settings.getCurrentUserOrganization();
    settings.setCurrentUserOrganization( settings.getOldCurrentUserOrganization() );
    settings.setOldCurrentUserOrganization( temp );

    temp = settings.getCurrentUserImage();
    settings.setCurrentUserImage( settings.getOldCurrentUserImage() );
    settings.setOldCurrentUserImage( temp );
  }

  private void showStartSubstituteDialog() {
    prepareStartSubstituteDialog();
    startSubstituteDialog.show();
  }

  private void dismissStartSubstituteDialog() {
    if ( startSubstituteDialog != null && startSubstituteDialog.isShowing() ) {
      startSubstituteDialog.dismiss();
    }
  }

  private void prepareStartSubstituteDialog() {
    if (startSubstituteDialog == null){
      startSubstituteDialog = new MaterialDialog.Builder( this )
        .title(R.string.app_name)
        .content(R.string.start_substitute)
        .cancelable(false)
        .progress(true, 0).build();
    }
  }

  private void showStopSubstituteDialog() {
    prepareStopSubstituteDialog();
    stopSubstituteDialog.show();
  }

  private void dismissStopSubstituteDialog() {
    if ( stopSubstituteDialog != null && stopSubstituteDialog.isShowing() ) {
      stopSubstituteDialog.dismiss();
    }
  }

  private void prepareStopSubstituteDialog() {
    if (stopSubstituteDialog == null){
      stopSubstituteDialog = new MaterialDialog.Builder( this )
        .title(R.string.app_name)
        .content(R.string.stop_substitute)
        .cancelable(false)
        .progress(true, 0).build();
    }
  }

  private void drawer_add_item(int index, String title, Long identifier) {
    Timber.tag("drawer_add_item").v(" !index " + index + " " + title);

    drawer.addDrawerItems(
      new PrimaryDrawerItem()
        .withName(title)
        .withIdentifier(identifier)
    );
  }

  public void initDrawer() {
    drawer_build_head();

    Fields.Menu menu = Fields.Menu.ALL;
    drawer_add_item( menu.getIndex() , menu.getTitle(), Long.valueOf( menu.getIndex()) );

    // resolved https://tasks.n-core.ru/browse/MVDESD-13752
    // Добавить в боковую панель разделы: Контроль, Обраб, Избр.
    List<String> menuItems = new ArrayList<>();
    menuItems.addAll( settings.getJournals() );
    menuItems.add( String.valueOf( ON_CONTROL ) );
    menuItems.add( String.valueOf( PROCESSED ) );
    menuItems.add( String.valueOf( FAVORITES ) );
    Collections.sort(menuItems, (o1, o2) -> {
      try {
        return Integer.valueOf(o1).compareTo( Integer.valueOf(o2) );
      } catch (NumberFormatException e) {
        return 0;
      }
    } );

    for(String uid : menuItems ){
      Fields.Menu m = Fields.Menu.getMenu(uid);
      if (m != null) {
        drawer_add_item( m.getIndex() , m.getTitle(), Long.valueOf( m.getIndex()) );
      }
    }

//    for(Fields.Menu menu: Fields.Menu.values() ){
//      drawer_add_item( menu.getIndex() , menu.getTitle(), Long.valueOf( menu.getIndex()) );
//    }

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

  /* MenuBuilder.Callback */
  @Override
  public void onMenuBuilderUpdate(ArrayList<ConditionBuilder> conditions) {
//    menuBuilder.setFavorites( dbQueryBuilder.getFavoritesCount() );
    dbQueryBuilder.executeWithConditions( conditions, menuBuilder.getItem().isVisible() && favorites_button.isChecked(), menuBuilder.getItem() );
  }

  @Override
  public void onUpdateError(Throwable error) {
  }

  @Override
  public void onShow() {
    setEmptyToolbarClickListener();
  }

  @Override
  public void onDismiss() {
    setToolbarClickListener();
  }


  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(RecalculateMenuEvent event) {
    if (menuBuilder != null) {
      Timber.tag(TAG).i("RecalculateMenuEvent");
//      menuBuilder.getItemsBuilder().updateView();
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(JournalSelectorIndexEvent event) {
    try {
      if ( menuIndex != event.index ) {
        // Reset previous state of organization filter on journal change and set tab changed
        settings.setOrganizationFilterActive( false );
        settings.setTabChanged( true );
      }
      menuIndex = event.index;
      DOCUMENT_TYPE_SELECTOR.setSelection(event.index);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateCountEvent event) {
    Timber.tag(TAG).i("UpdateCountEvent");
    update( false );
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onMessageEvent(LoadedFromDbEvent event) {
    Timber.tag("LoadFromDb").i("MainActivity: handle LoadedFromDbEvent");
    EventBus.getDefault().removeStickyEvent(event);

    if ( switchToSubstituteModeStarted ) {
      switchToSubstituteModeStarted = false;
      dataLoader.initV2( true );
      setJournalType(ALL, false);
      update( false );
      dismissStartSubstituteDialog();

    } else if ( exitFromSubstituteModeStarted ) {
      exitFromSubstituteModeStarted = false;
      setJournalType(ALL, false);
      update( true );
      dismissStopSubstituteDialog();

    } else {
      update( false );
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateDrawerEvent event) {
    Timber.tag(TAG).i("UpdateDrawerEvent");
    initDrawer();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(ReceivedTokenEvent event) {
    Timber.tag(TAG).i("ReceivedTokenEvent");

    if ( exitFromSubstituteModeStarted ) {
      store.clearAndLoadFromDb();
    } else {
      updateByStatus();
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(ErrorReceiveTokenEvent event) {
    Timber.tag(TAG).i("ErrorReceiveTokenEvent");

    if ( exitFromSubstituteModeStarted ) {
      exitFromSubstituteModeStarted = false;
      settings.setSubstituteMode( true );
      // В случае ошибки меняем логин и состояние документов и бокового меню обратно
      switchUser();
      dismissStopSubstituteDialog();
      Toast.makeText(this, "Ошибка выхода из режима замещения", Toast.LENGTH_SHORT).show();
    }
  }
}

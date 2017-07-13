package sapotero.rxtest.views.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
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
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
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
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.adapter.JournalSelectorIndexEvent;
import sapotero.rxtest.events.service.CheckNetworkEvent;
import sapotero.rxtest.events.utils.RecalculateMenuEvent;
import sapotero.rxtest.jobs.bus.UpdateAuthTokenJob;
import sapotero.rxtest.managers.DataLoaderManager;
import sapotero.rxtest.services.MainService;
import sapotero.rxtest.utils.Settings;
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
import sapotero.rxtest.views.menu.MenuBuilder;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements MenuBuilder.Callback, SearchView.OnVisibilityChangeListener {

  @Inject JobManager jobManager;
  @Inject Settings settings;
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject QueueManager queue;
  @Inject MemoryStore store;

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

  @SuppressLint("StaticFieldLeak")
  public static DocumentsAdapter RAdapter;

  public  DBQueryBuilder dbQueryBuilder;
  public  MenuBuilder menuBuilder;
  private DataLoaderManager dataLoader;
  private SearchView searchView;
  private MainActivity context;
  private CompositeSubscription subscription;
  private PublishSubject<Integer> searchSubject = PublishSubject.create();
  private int menuIndex;
  private int buttonIndex;

  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.bind(this);
    EsdApplication.getManagerComponent().inject(this);
    context = this;
    searchSubject = PublishSubject.create();

    initAdapters();

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


//    rxSettings();

    initSearch();

    setFirstRunFalse();

    updateToken();
    initSearchSub();

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
    dataLoader.updateAuth(sign);
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
      store.clear();
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

          searchSubject.onNext( item.getItemId() );

//          searchView.onOptionsItemSelected(getFragmentManager(), item);
//          setEmptyToolbarClickListener();
          break;
        default:
          jobManager.addJobInBackground(new UpdateAuthTokenJob());
          break;
      }
      return false;
    });
  }

  private void initSearchSub(){

    searchSubject
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

  private void initEvents() {
    Timber.tag(TAG).v("initEvents");
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);

    Intent serviceIntent = MainService.newIntent(this, false);
    startService(serviceIntent);
  }

  private void updateByStatus() {
    dataLoader.updateByCurrentStatus( menuBuilder.getItem(), null, false);
    Toast.makeText(this, "Обновление данных...", Toast.LENGTH_SHORT).show();
  }


  @Override
  public void onResume() {
    super.onResume();
    initEvents();
    startNetworkCheck();
    subscribeToNetworkCheckResults();

    rxSettings();

//    EventBus.getDefault().post( new RecalculateMenuEvent());

  }

  private void startNetworkCheck() {
    EventBus.getDefault().post(new CheckNetworkEvent( true ));
  }

  private void stopNetworkCheck() {
    EventBus.getDefault().post(new CheckNetworkEvent( false ));
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

  private void unsubscribe() {
    if ( subscription != null && subscription.hasSubscriptions() ) {
      subscription.unsubscribe();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    stopNetworkCheck();
    unsubscribe();
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
        new DividerDrawerItem(),
        new SecondaryDrawerItem()
          .withName(R.string.drawer_item_settings_account)
          .withIdentifier(SETTINGS_VIEW),
        new SecondaryDrawerItem()
          .withName(R.string.drawer_item_settings_templates)
          .withIdentifier(SETTINGS_DECISION_TEMPLATES)
      );

    if (settings.isDebugEnabled()){
      drawer
        .addDrawerItems(
          new DividerDrawerItem(),
          new SecondaryDrawerItem()
            .withIdentifier(SETTINGS_LOG)
            .withName("Очередь задач"),
          new SecondaryDrawerItem()
            .withIdentifier(SETTINGS_SIGN)
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

    List<RColleagueEntity> colleaguesFromDB = dataStore
      .select(RColleagueEntity.class)
      .where(RColleagueEntity.USER.eq( settings.getLogin() ))
      .and(RColleagueEntity.ACTIVED.eq(true))
      .get().toList();

    List<RColleagueEntity> colleagues = new ArrayList<>();
    colleagues.addAll( colleaguesFromDB );

    Collections.sort(colleagues, (o1, o2) -> o1.getSortIndex() != null && o2.getSortIndex() != null ? o1.getSortIndex().compareTo( o2.getSortIndex() ) : 0 );

    IProfile[] profiles = new ProfileDrawerItem[ colleagues.size() + 1 ];

    profiles[0] = new ProfileDrawerItem()
      .withName( settings.getCurrentUserOrganization() )
      .withEmail( settings.getCurrentUser() )
      .withSetSelected( true )
      .withIcon( R.drawable.gerb );

    int i = 1;

    for (RColleagueEntity colleague : colleagues) {
      String colleagueName = splitName( colleague.getOfficialName() );

      profiles[i] = new ProfileDrawerItem()
        .withName( colleagueName )
        .withIsExpanded( true )
        .withSelectable( false )
        .withSetSelected( false )
        .withIcon( R.drawable.gerb );
      i++;
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
    }

    return name;
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
      menuBuilder.getItemsBuilder().updateView();
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(JournalSelectorIndexEvent event) {
    menuIndex = event.index;
    DOCUMENT_TYPE_SELECTOR.setSelection(event.index);
  }


}

package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.os.Bundle;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.query.DBQueryBuilder;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.bus.GetDocumentInfoEvent;
import sapotero.rxtest.events.bus.MarkDocumentAsChangedJobEvent;
import sapotero.rxtest.events.bus.UpdateDocumentJobEvent;
import sapotero.rxtest.events.rx.InsertRxDocumentsEvent;
import sapotero.rxtest.jobs.bus.UpdateAuthTokenJob;
import sapotero.rxtest.views.adapters.DocumentsAdapter;
import sapotero.rxtest.views.adapters.OrganizationAdapter;
import sapotero.rxtest.views.adapters.utils.DocumentTypeAdapter;
import sapotero.rxtest.views.adapters.utils.StatusAdapter;
import sapotero.rxtest.views.interfaces.DataLoaderInterface;
import sapotero.rxtest.views.menu.MenuBuilder;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import sapotero.rxtest.views.views.CircleLeftArrow;
import sapotero.rxtest.views.views.CircleRightArrow;
import sapotero.rxtest.views.views.MultiOrganizationSpinner;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements MenuBuilder.Callback {

  @Inject JobManager jobManager;
  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;

  @BindView(R.id.toolbar) Toolbar toolbar;

  @BindView(R.id.documentsRecycleView) RecyclerView rv;
  @BindView(R.id.progressBar) ProgressBar progressBar;

  @BindView(R.id.activity_main_menu) LinearLayout activity_main_menu;

  @BindView(R.id.activity_main_menu_builder_organization) LinearLayout menu_builder_organization;
  @BindView(R.id.activity_main_menu_builder_buttons) FrameLayout menu_builder_buttons;




  @BindView(R.id.DOCUMENT_TYPE) Spinner DOCUMENT_TYPE_SELECTOR;
//  @BindView(R.id.JOURNAL_TYPE) Spinner FILTER_TYPE_SELECTOR;
  @BindView(R.id.ORGANIZATION) MultiOrganizationSpinner ORGANIZATION_SELECTOR;

  @BindView(R.id.activity_main_right_button) CircleRightArrow rightArrow;

  @BindView(R.id.activity_main_left_button) CircleLeftArrow leftArrow;
  @BindView(R.id.favorites_button) CheckBox favorites_button;

  @BindView(R.id.documents_empty_list) TextView documents_empty_list;



  private String TAG = MainActivity.class.getSimpleName();

  private Preference<String> TOKEN;

  private Preference<String> LOGIN;
  private Preference<String> HOST;
  private Preference<String> PASSWORD;

  private StatusAdapter filter_adapter;
  private OrganizationAdapter organization_adapter;
  private DocumentTypeAdapter document_type_adapter;

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
  private final int SETTINGS_REJECTION_TEMPLATES = 22;

  public DocumentsAdapter RAdapter;
  public  MenuBuilder menuBuilder;
  private DBQueryBuilder dbQueryBuilder;
  private DataLoaderInterface dataLoader;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);
    loadSettings();


    RAdapter = new DocumentsAdapter(this, new ArrayList<>());
    rv.setAdapter(RAdapter);
    GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
    rv.setLayoutManager(gridLayoutManager);

    organization_adapter = new OrganizationAdapter(this, new ArrayList<>());
    ORGANIZATION_SELECTOR.setAdapter(organization_adapter, false, selected -> {
      Timber.tag("ORGANIZATION_SELECTOR").i("selected");
    });

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
      .withOrganizationsAdapter( organization_adapter )
      .withEmptyView( documents_empty_list )
      .withProgressBar( progressBar );

    dbQueryBuilder.printFolders();
    dbQueryBuilder.printTemplates();



    dataLoader = new DataLoaderInterface(this);



    progressBar.setVisibility(ProgressBar.GONE);


    toolbar.setTitle("Все документы");
    toolbar.setTitleTextColor(getResources().getColor(R.color.md_grey_100));
    toolbar.setSubtitleTextColor(getResources().getColor(R.color.md_grey_400));

    toolbar.setContentInsetStartWithNavigation(250);

    toolbar.inflateMenu(R.menu.main_menu);
    toolbar.setOnMenuItemClickListener(item -> {
      switch (item.getItemId()) {
        case R.id.reload:
          updateByStatus();
//          document_favorite_button1.setVisibility( document_favorite_button1.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
          break;
        default:
          jobManager.addJobInBackground(new UpdateAuthTokenJob());
          break;
      }
      return false;
    });

    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);

    rxSettings();

  }

  private void updateByStatus() {
    dataLoader.updateByStatus( menuBuilder.getItem() );
//    dataLoader.updateByStatus( menuBuilder.getItem() );

//    ProgressDialog prog= new ProgressDialog(this);//Assuming that you are using fragments.
//    prog.setTitle("Обновление данных");
//    prog.setMessage("data is loading...");
//    prog.setCancelable(false);
//    prog.setIndeterminate(true);
//    prog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//    prog.show();
//
//    new Handler().postDelayed( () -> {
//      prog.dismiss();
//    }, 5000L);

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
    menuBuilder.build();
    menuBuilder.getItem().recalcuate();
    dbQueryBuilder.execute();
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
//    int index = Arrays.asList((getResources().getStringArray(R.array.settings_view_start_page_identifier))).indexOf(String.valueOf(type));
//    String value = Arrays.asList((getResources().getStringArray(R.array.settings_view_start_page_values))).get(index);
//
//    Integer adapter_index = document_type_adapter.findByValue(value);
//    Timber.tag(TAG).i("value selected int: " + adapter_index);
//    DOCUMENT_TYPE_SELECTOR.setSelection(adapter_index);
    menuBuilder.selectJournal( type );

  }

  private void drawer_build_bottom() {
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
          .withIdentifier(SETTINGS_DECISION_TEMPLATES),
        new SecondaryDrawerItem()
          .withName(R.string.drawer_item_settings_templates_off)
          .withIcon(MaterialDesignIconic.Icon.gmi_comment_list)
          .withIdentifier(SETTINGS_REJECTION_TEMPLATES),

        new DividerDrawerItem(),
        new SecondaryDrawerItem()
          .withName(R.string.drawer_item_debug)
          .withIcon(MaterialDesignIconic.Icon.gmi_developer_board)
          .withIdentifier(99)
      )
      .withOnDrawerItemClickListener(
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
            default:
              activity = SettingsActivity.class;
              break;
          }


          Timber.tag(TAG).i(String.valueOf(view));
          Timber.tag(TAG).i(String.valueOf(position));
          Timber.tag(TAG).i(String.valueOf(drawerItem.getIdentifier()));

          if (activity != null) {
            Intent intent = new Intent(this, activity);
            startActivity(intent);
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
          .withName( settings.getString("current_user").get() )
          .withEmail( settings.getString("current_user").get() )
          .withSetSelected(true)
//          .withEmail("admin_id")
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

//    for (Integer i : treeMap.keySet()) {
//      Timber.tag("drawer_add_item").v(" !index " + i + " " + treeMap.get(i));
//      drawer_add_item(i, title[i], Long.valueOf(identifier[i]));
//    }

    drawer_build_bottom();
  }

  private void drawer_add_item(int index, String title, Long identifier) {
    Timber.tag("drawer_add_item").v(" !index " + index + " " + title);

    drawer.addDrawerItems(
      new PrimaryDrawerItem()
        .withName(title)
        .withIdentifier(identifier)
    );
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
    Toast.makeText(getApplicationContext(), event.message, Toast.LENGTH_SHORT).show();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(InsertRxDocumentsEvent event) {
    Toast.makeText(getApplicationContext(), event.message, Toast.LENGTH_SHORT).show();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateDocumentJobEvent event) {
    int position = RAdapter.getPositionByUid(event.uid);
    RecyclerView.ViewHolder a = rv.findViewHolderForAdapterPosition(position);

    int visibility = event.value ? View.VISIBLE : View.GONE;
    int field = Objects.equals(event.field, "control") ? R.id.control_label : R.id.favorite_label;

    View view = a.itemView.findViewById(field);
    view.setVisibility(visibility);

  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(MarkDocumentAsChangedJobEvent event) {
    Timber.tag("JOBS").i( "MarkDocumentAsChangedJobEvent ++ "  );
  }



  /* MenuBuilder.Callback */
  @Override
  public void onMenuBuilderUpdate(ArrayList<ConditionBuilder> conditions) {

    for ( ConditionBuilder condition: conditions ) {
      Timber.tag(TAG).i( "++ %s", condition.toString() );
    }


    menuBuilder.setFavorites( dbQueryBuilder.getFavoritesCount() );


    Timber.tag(TAG).i( "visible: %s, pressed: %s", menuBuilder.getItem().isVisible(), favorites_button.isChecked() );

    dbQueryBuilder.executeWithConditions( conditions, menuBuilder.getItem().isVisible() && favorites_button.isChecked() );
  }

  @Override
  public void onUpdateError(Throwable error) {

  }

}

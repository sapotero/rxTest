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
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.query.DBQueryBuilder;
import sapotero.rxtest.events.bus.GetDocumentInfoEvent;
import sapotero.rxtest.events.bus.MarkDocumentAsChangedJobEvent;
import sapotero.rxtest.events.bus.UpdateDocumentJobEvent;
import sapotero.rxtest.events.rx.InsertRxDocumentsEvent;
import sapotero.rxtest.jobs.bus.UpdateAuthTokenJob;
import sapotero.rxtest.views.adapters.DocumentsAdapter;
import sapotero.rxtest.views.adapters.OrganizationAdapter;
import sapotero.rxtest.views.adapters.utils.DocumentTypeAdapter;
import sapotero.rxtest.views.adapters.utils.StatusAdapter;
import sapotero.rxtest.views.menu.MenuBuilder;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import sapotero.rxtest.views.views.CircleLeftArrow;
import sapotero.rxtest.views.views.CircleRightArrow;
import sapotero.rxtest.views.views.MultiOrganizationSpinner;
import timber.log.Timber;

import static android.widget.Toast.LENGTH_LONG;

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
  private final int SETTINGS_VIEW_TYPE_ALL = 10;
  private final int SETTINGS_VIEW_TYPE_INCOMING_DOCUMENTS = 11;
  private final int SETTINGS_VIEW_TYPE_CITIZEN_REQUESTS = 12;
  private final int SETTINGS_VIEW_TYPE_INCOMING_ORDERS = 13;
  private final int SETTINGS_VIEW_TYPE_INTERNAL = 14;
  private final int SETTINGS_VIEW_TYPE_ORDERS = 15;
  private final int SETTINGS_VIEW_TYPE_ORDERS_MVD = 16;
  private final int SETTINGS_VIEW_TYPE_ORDERS_DDO = 17;

  private final int SETTINGS_VIEW_TYPE_APPROVE = 18;
  private final int SETTINGS_VIEW = 20;
  private final int SETTINGS_DECISION_TEMPLATES = 21;
  private final int SETTINGS_REJECTION_TEMPLATES = 22;

  public DocumentsAdapter RAdapter;

  private Subscription documentQuery = null;
  private Subscription changedQuery = null;
  private int loaded = 0;
  private Toast mToast;
  private Subscription updateOrganizations;
  public MenuBuilder menuBuilder;
  private DBQueryBuilder dbQueryBuilder;

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
      .registerCallBack(this);
    menuBuilder.build();

    dbQueryBuilder = new DBQueryBuilder(this)
      .withAdapter( RAdapter )
      .withOrganizationsAdapter( organization_adapter )
      .withEmptyView( documents_empty_list )
      .withProgressBar( progressBar );

    dbQueryBuilder.printFolders();
    dbQueryBuilder.printTemplates();



    progressBar.setVisibility(ProgressBar.GONE);


    toolbar.setTitle("Все документы");
    toolbar.setTitleTextColor(getResources().getColor(R.color.md_grey_100));
    toolbar.setSubtitleTextColor(getResources().getColor(R.color.md_grey_400));

    toolbar.setContentInsetStartWithNavigation(250);

    toolbar.inflateMenu(R.menu.main_menu);
    toolbar.setOnMenuItemClickListener(item -> {
      switch (item.getItemId()) {
        case R.id.reload:
          tryToread();
//          document_favorite_button1.setVisibility( document_favorite_button1.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
          break;
        default:
          jobManager.addJobInBackground(new UpdateAuthTokenJob());
          break;
      }
      return false;
    });


  }

  private void tryToread() {
    Timber.tag("IO").i( "tryToread start" );

    try {
      Process process = Runtime.getRuntime().exec("/system/bin/ls -la /storage");

      BufferedReader reader = new BufferedReader( new InputStreamReader(process.getInputStream()) );
      int read;
      char[] buffer = new char[4096];

      StringBuilder output = new StringBuilder();
      while ((read = reader.read(buffer)) > 0) {
        output.append(buffer, 0, read);
      }
      reader.close();

      process.waitFor();

      Toast.makeText (this, output.toString(), LENGTH_LONG ).show();

    } catch (IOException | InterruptedException e) {
      Timber.tag("IO").w( e );

    }
    Timber.tag("IO").d( "tryToread end" );
  }

  @Override
  public void onStart() {
    super.onStart();


    if (!EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().register(this);
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    rxSettings();
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
    int index = Arrays.asList((getResources().getStringArray(R.array.settings_view_start_page_identifier))).indexOf(String.valueOf(type));
    String value = Arrays.asList((getResources().getStringArray(R.array.settings_view_start_page_values))).get(index);

    Integer adapter_index = document_type_adapter.findByValue(value);
    Timber.tag(TAG).i("value selected int: " + adapter_index);
    DOCUMENT_TYPE_SELECTOR.setSelection(adapter_index);
  }

  private void drawer_build_bottom() {
    Drawer dr = drawer
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
            case SETTINGS_VIEW_TYPE_ALL:
              setJournalType(SETTINGS_VIEW_TYPE_ALL);
              break;
            case SETTINGS_VIEW_TYPE_INCOMING_DOCUMENTS:
              setJournalType(SETTINGS_VIEW_TYPE_INCOMING_DOCUMENTS);
              break;
            case SETTINGS_VIEW_TYPE_CITIZEN_REQUESTS:
              setJournalType(SETTINGS_VIEW_TYPE_CITIZEN_REQUESTS);
              break;
            case SETTINGS_VIEW_TYPE_INCOMING_ORDERS:
              setJournalType(SETTINGS_VIEW_TYPE_INCOMING_ORDERS);
              break;
            case SETTINGS_VIEW_TYPE_INTERNAL:
              setJournalType(SETTINGS_VIEW_TYPE_INTERNAL);
              break;
            case SETTINGS_VIEW_TYPE_ORDERS:
              setJournalType(SETTINGS_VIEW_TYPE_ORDERS);
              break;
            case SETTINGS_VIEW_TYPE_ORDERS_MVD:
              setJournalType(SETTINGS_VIEW_TYPE_ORDERS_MVD);
              break;
            case SETTINGS_VIEW_TYPE_ORDERS_DDO:
              setJournalType(SETTINGS_VIEW_TYPE_ORDERS_DDO);
              break;
            case SETTINGS_VIEW_TYPE_APPROVE:
              setJournalType(SETTINGS_VIEW_TYPE_APPROVE);
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

    drawer = new DrawerBuilder()
      .withActivity(this)
      .withToolbar(toolbar)
      .withActionBarDrawerToggle(true)
      .withHeader(R.layout.drawer_header)
      .withAccountHeader(headerResult);

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


    for (Integer i : treeMap.keySet()) {
      Timber.tag("drawer_add_item").v(" !index " + i + " " + treeMap.get(i));
      drawer_add_item(i, title[i], Long.valueOf(identifier[i]));
    }

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

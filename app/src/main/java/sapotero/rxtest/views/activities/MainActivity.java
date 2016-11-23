package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
import com.mikepenz.materialdrawer.icons.MaterialDrawerFont;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.honorato.multistatetogglebutton.MultiStateToggleButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.requery.Persistable;
import io.requery.query.Expression;
import io.requery.query.LogicalCondition;
import io.requery.query.Result;
import io.requery.query.WhereAndOr;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.events.bus.GetDocumentInfoEvent;
import sapotero.rxtest.events.bus.MarkDocumentAsChangedJobEvent;
import sapotero.rxtest.events.bus.UpdateDocumentJobEvent;
import sapotero.rxtest.events.rx.InsertRxDocumentsEvent;
import sapotero.rxtest.events.rx.LoadAllDocumentsByStatusEvent;
import sapotero.rxtest.jobs.bus.UpdateAuthTokenJob;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.views.adapters.DocumentsAdapter;
import sapotero.rxtest.views.adapters.OrganizationAdapter;
import sapotero.rxtest.views.adapters.models.DocumentTypeItem;
import sapotero.rxtest.views.adapters.models.FilterItem;
import sapotero.rxtest.views.adapters.models.OrganizationItem;
import sapotero.rxtest.views.adapters.utils.DocumentTypeAdapter;
import sapotero.rxtest.views.adapters.utils.StatusAdapter;
import sapotero.rxtest.views.views.CircleLeftArrow;
import sapotero.rxtest.views.views.CircleRightArrow;
import sapotero.rxtest.views.views.MultiOrganizationSpinner;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

  @Inject JobManager jobManager;
  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  @BindView(R.id.toolbar) Toolbar toolbar;

  @BindView(R.id.documentsRecycleView) RecyclerView rv;
  @BindView(R.id.progressBar) View progressBar;

  @BindView(R.id.DOCUMENT_TYPE) Spinner DOCUMENT_TYPE_SELECTOR;
  @BindView(R.id.JOURNAL_TYPE) Spinner FILTER_TYPE_SELECTOR;
  @BindView(R.id.ORGANIZATION) MultiOrganizationSpinner ORGANIZATION_SELECTOR;

  @BindView(R.id.document_control_buttons) MultiStateToggleButton control_buttons;

  @BindView(R.id.activity_main_right_button) CircleRightArrow rightArrow;
  @BindView(R.id.activity_main_left_button) CircleLeftArrow leftArrow;

  @BindView(R.id.documents_empty_list) TextView documents_empty_list;

  @BindView(R.id.document_control_button) Button document_control_button;
  @BindView(R.id.document_favorite_button) Button document_favorite_button;

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

  private Subscription loader;
  public DocumentsAdapter RAdapter;
  private Document __document__;
  private Subscription timeoutSubcribe;
  private boolean[] old_selectd;
  private boolean needToFindOrganizations = true;
  private Subscription documentQuery = null;
  private Subscription changedQuery = null;
  private Subscription loadFromDbQuery = null;
  private int loaded = 0;
  private Toast mToast;
  private Subscription updateDocumentCount;
  private Subscription updateOrganizations;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    loadSettings();
    setAdapters();

    GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
    rv.setLayoutManager(gridLayoutManager);

    progressBar.setVisibility(ProgressBar.GONE);

    RAdapter = new DocumentsAdapter(this, new ArrayList<>());
    rv.setAdapter(RAdapter);


    toolbar.setTitle("Все документы");
    toolbar.setTitleTextColor(getResources().getColor(R.color.md_grey_100));
    toolbar.setSubtitleTextColor(getResources().getColor(R.color.md_grey_400));

    toolbar.setContentInsetStartWithNavigation(250);

    toolbar.inflateMenu(R.menu.info);
    toolbar.setOnMenuItemClickListener(item -> {
      switch (item.getItemId()) {
        case R.id.reload:
          System.gc();

          break;
        default:
          jobManager.addJobInBackground(new UpdateAuthTokenJob());
          break;
      }
      return false;
    });

    View[] buttons = new View[]{document_control_button, document_favorite_button};
    control_buttons.setButtons(buttons, new boolean[buttons.length]);

    control_buttons.setOnValueChangedListener(position -> {
      Timber.tag(TAG).d("Position: " + position);
      Timber.tag(TAG).d("Position: " + control_buttons.getStates()[0]);
      Timber.tag(TAG).d("Position: " + control_buttons.getStates()[1]);

      loadFromDB();
    });
    control_buttons.enableMultipleChoice(true);
    loadFromDB();
  }

  @Override
  public void onStart() {
    super.onStart();

    if (subscriptions == null) {
      subscriptions = new CompositeSubscription();
    }

    if (!EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().register(this);
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    if (documentQuery != null) {
      documentQuery.unsubscribe();
//      table_changes();
    }

    if (changedQuery != null) {
      changedQuery.unsubscribe();
      documentsModifiedListener();
    }

    rxSettings();
  }

  private void documentsModifiedListener() {

    if (changedQuery == null) {
      changedQuery = dataStore
        .select(RDocumentEntity.class)
        .where(RDocumentEntity.CHANGED.eq(true))
        .orderBy(RDocumentEntity.ID.desc())
        .get()
        .toSelfObservable()
        .subscribe(
          doc -> {
            Timber.tag("documentsModifiedListener").i( "data " + doc.first().getUid() );
          },
          error -> {
            Timber.tag("documentsModifiedListener").e("error " + error.toString());
            error.printStackTrace();
          }
        );
    }
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
//          .withEmail("admin_id")
          .withIcon( MaterialDrawerFont.Icon.mdf_person )
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

  private void setAdapters() {

    List<FilterItem> filters = new ArrayList<>();
    String[] filter_types = getResources().getStringArray(R.array.FILTER_TYPES_VALUE);
    String[] filter_names = getResources().getStringArray(R.array.FILTER_TYPES);
    for (int i = 0; i < filter_types.length; i++) {
      filters.add(new FilterItem(filter_names[i], filter_types[i], "0"));
    }

    filter_adapter = new StatusAdapter(this, filters);
    FILTER_TYPE_SELECTOR.setAdapter(filter_adapter);
    FILTER_TYPE_SELECTOR.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        loadFromDB();
        toolbar.setSubtitle(filter_adapter.getItem(position).getName());
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
      }
    });


    List<DocumentTypeItem> document_types = new ArrayList<>();
    String[] document_types_name = getResources().getStringArray(R.array.JOURNAL_TYPES);
    String[] document_types_value = getResources().getStringArray(R.array.JOURNAL_TYPES_VALUE);

    for (int i = 0; i < document_types_name.length; i++) {
      document_types.add(new DocumentTypeItem(document_types_name[i], document_types_value[i], 0));
    }

    document_type_adapter = new DocumentTypeAdapter(this, document_types);
    DOCUMENT_TYPE_SELECTOR.setAdapter(document_type_adapter);
    DOCUMENT_TYPE_SELECTOR.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        findOrganizations();
        loadFromDB();
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
      }
    });


    organization_adapter = new OrganizationAdapter(this, new ArrayList<>());
    ORGANIZATION_SELECTOR.setAdapter(organization_adapter, false, selected -> {
      loadFromDB();
    });

    updateDocumentCount();

//    Observable.from(filter_types).subscribe(
//      this::loadDocumentsCountByType
//    );
  }

  private void updateDocumentCount() {

    if (updateDocumentCount != null){
      updateDocumentCount.unsubscribe();
    }
    updateDocumentCount = dataStore
      .select(RDocumentEntity.UID)
      .get()
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(uid -> {
        document_type_adapter.updateCountByType( uid.get(0) );
      });

    updateOrganizations();
  }

  private void updateOrganizations(){

    if (updateOrganizations != null){
      updateOrganizations.unsubscribe();
    }
    updateOrganizations = dataStore
      .select(RSignerEntity.ORGANISATION)
      .distinct()
      .get()
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
//      .flatMap( org -> {
//
//      })
//      })
      .subscribe(org -> {

        Integer count = dataStore
          .count(RSignerEntity.class)
          .where(RSignerEntity.ORGANISATION.eq(org.get(0).toString()))
          .get()
          .value();

        organization_adapter.add(new OrganizationItem(org.get(0).toString(), count));

        Timber.tag(TAG).d("ORGANIZATION: " + org.get(0).toString());
        Timber.tag(TAG).d("ORGANIZATION COUNT: " + count);
      });
  }

  private void loadFromDB() {

    documents_empty_list.setVisibility(View.GONE);
    progressBar.setVisibility(ProgressBar.VISIBLE);

    //    findOrganizations();
    updateFilterAdapter();

    //    updateOrganizationAdapter();

    int spinner_pos = DOCUMENT_TYPE_SELECTOR.getSelectedItemPosition();

    String[] document_type = getResources().getStringArray(R.array.JOURNAL_TYPES_VALUE);
    String type = String.valueOf(document_type[spinner_pos]);

    String[] document_title = getResources().getStringArray(R.array.JOURNAL_TYPES);
    String title = String.valueOf(document_title[spinner_pos]);
    toolbar.setTitle(title);

    Timber.d("DOCUMENT_TYPE_SELECTOR " + spinner_pos);
    Timber.d("DOCUMENT_TYPE_SELECTOR " + type);
    RAdapter.clear();

    WhereAndOr<Result<RDocumentEntity>> query = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.like(type + "%"));


    // favorites && control
    LogicalCondition<? extends Expression<Boolean>, ?> favorites_condition = null;
    LogicalCondition<? extends Expression<Boolean>, ?> control_condition = null;

    boolean favorites_button_value = control_buttons.getStates()[0];
    boolean control_button_value = control_buttons.getStates()[1];

    favorites_condition = favorites_button_value ? RDocumentEntity.CONTROL.eq(true) : RDocumentEntity.CONTROL.in(Arrays.asList(true, false));
    control_condition = control_button_value ? RDocumentEntity.FAVORITES.eq(true) : RDocumentEntity.FAVORITES.in(Arrays.asList(true, false));


    if (favorites_button_value) {
      query = query.and(favorites_condition);
    }
    if (control_button_value) {
      query = query.and(control_condition);
    }
    // favorites && control


    // organizations
    boolean organization_filter = false;

    ArrayList<String> organizations = new ArrayList<String>();
    if (Arrays.asList(ORGANIZATION_SELECTOR.getSelected()).size() > 0) {

      for (int i = 0; i < ORGANIZATION_SELECTOR.getSelected().length; i++) {
        if (ORGANIZATION_SELECTOR.getSelected()[i]) {
          try {
            organizations.add(organization_adapter.getItem(i).getName());
            Timber.tag(TAG).i(String.format("%s - ++", organization_adapter.getItem(i).getName()));
            organization_filter = true;
          } catch (Exception e) {
            Timber.tag(TAG).e(e);
          }
        }
      }
    }

    if (organization_filter) {
      query = query.and(RDocumentEntity.ORGANIZATION.in(organizations));
    }

    // organizations


    // filter

    int filter_index = FILTER_TYPE_SELECTOR.getSelectedItemPosition();
    FilterItem filter_item = filter_adapter.getItem(filter_index);

    Timber.tag(TAG).i(String.format("filter_name %s - ++", filter_item.getName()));

    query = query.and(RDocumentEntity.FILTER.eq(filter_item.getValue()));


    // filter

    if (loadFromDbQuery != null) {
      loadFromDbQuery.unsubscribe();
    }


    loadFromDbQuery = query.get()
      .toObservable()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .toList()
      .subscribe(docs -> {
        addToAdapterList(docs);
      });

    if (Integer.valueOf(filter_item.getCount()) == 0) {
      Timber.e("EMPTY LIST count");
      progressBar.setVisibility(ProgressBar.GONE);
      documents_empty_list.setVisibility(View.VISIBLE);
      documents_empty_list.setText(getString(R.string.document_empty_list));
    }

  }

  private void addToAdapterList(List<RDocumentEntity> docs) {
    if (docs.size() > 0) {

      ArrayList<Document> list_dosc = new ArrayList<Document>();
      for (int i = 0; i < docs.size(); i++) {
        RDocumentEntity doc = docs.get(i);
        Timber.tag(TAG).v("addToAdapter ++ " + doc.getUid());

        Document document = new Document();
        document.setUid(doc.getUid());
        document.setMd5(doc.getMd5());
        document.setControl(doc.isControl());
        document.setFavorites(doc.isFavorites());
        document.setSortKey(doc.getSortKey());
        document.setTitle(doc.getTitle());
        document.setRegistrationNumber(doc.getRegistrationNumber());
        document.setRegistrationDate(doc.getRegistrationDate());
        document.setUrgency(doc.getUrgency());
        document.setShortDescription(doc.getShortDescription());
        document.setComment(doc.getComment());
        document.setExternalDocumentNumber(doc.getExternalDocumentNumber());
        document.setReceiptDate(doc.getReceiptDate());
        document.setOrganization(doc.getOrganization());

        list_dosc.add(document);

      }
      RAdapter.setDocuments(list_dosc);
      progressBar.setVisibility(ProgressBar.GONE);
    }


  }

  private void findOrganizations() {
    organization_adapter.clear();

    dataStore
      .select(RSignerEntity.ORGANISATION)
      .distinct()
      .get()
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(org -> {

        Integer count = dataStore
          .count(RSignerEntity.class)
          .where(RSignerEntity.ORGANISATION.eq(org.get(0).toString()))
          .get()
          .value();

        organization_adapter.add(new OrganizationItem(org.get(0).toString(), count));
      });
  }

//  private void loadDocumentsCountByType(String TYPE) {
//
//    Retrofit retrofit = new RetrofitManager(this, HOST.get() + "/v3/", okHttpClient).process();
//    DocumentsService documentsService = retrofit.create(DocumentsService.class);
//
//    Observable<Documents> documents = documentsService.getDocuments(LOGIN.get(), TOKEN.get(), TYPE, 0, 0);
//
//    documents.subscribeOn(Schedulers.io())
//      .observeOn(AndroidSchedulers.mainThread())
//      .subscribe(
//        data -> {
//          total += Integer.valueOf( data.getMeta().getTotal() );
//
//          notificationUpdate();
//
//          String total = data.getMeta().getTotal();
//
//          if (total != null && Integer.valueOf(total) > 0) {
//            String[] values = getResources().getStringArray(R.array.FILTER_TYPES_VALUE);
//
//            int index = -1;
//            for (int i = 0; i < values.length; i++) {
//              if (values[i].equals(TYPE)) {
//                index = i;
//                break;
//              }
//            }
//
//            Timber.tag(TAG).i(TYPE + " - " + total + " | " + index);
//
//            FilterItem filterItem = filter_adapter.getItem(index);
//
//            filterItem.setCount(total);
//
//            jobManager.addJobInBackground(new LoadAllDocumentsByStatusJob(index, total));
//
//            filter_adapter.notifyDataSetChanged();
//          }
//
//        },
//        error -> {
//          Timber.tag(TAG).d("loadDocumentsCountByType " + error.getMessage());
//        });
//
//  }
//
  public void showNextType(Boolean next) {
    unsubscribe();
    int position = next ? filter_adapter.next() : filter_adapter.prev();
    FILTER_TYPE_SELECTOR.setSelection(position);
  }

  public void unsubscribe() {
    if (loader != null && !loader.isUnsubscribed()) {
      loader.unsubscribe();
    }
  }

  private void table_changes() {

    documents_empty_list.setText(null);
    if (documentQuery == null) {
      documentQuery = dataStore
        .select(RDocumentEntity.class)
        .orderBy(RDocumentEntity.ID.desc())
        .get()
        .toSelfObservable()
        .subscribe(
          data -> {
            List<RDocumentEntity> docs = data.toList();

            for (RDocumentEntity doc:docs) {
              document_type_adapter.updateCountByType( doc.getUid() );
            }
          },
          error -> {
            Timber.tag("table_changes").e("error " + error.toString());
            error.printStackTrace();
          }
        );
    }


  }

  private void updateFilterAdapter() {

    int spinner_pos = DOCUMENT_TYPE_SELECTOR.getSelectedItemPosition();

    String[] document_type = getResources().getStringArray(R.array.JOURNAL_TYPES_VALUE);
    String type = String.valueOf(document_type[spinner_pos]);

    String[] filter_types = getResources().getStringArray(R.array.FILTER_TYPES_VALUE);
    for (int i = 0; i < filter_types.length; i++) {

      Integer count = dataStore
        .count(RDocumentEntity.class)
        .where(RDocumentEntity.UID.like(type + "%"))
        .and(RDocumentEntity.FILTER.eq(filter_types[i]))
        .get()
        .value();

      filter_adapter.updateByValue(filter_types[i], count);
    }

  }

  private void notificationUpdate(){

    if (mToast == null) {
      mToast = Toast.makeText(MainActivity.this, String.format(  "Загрузка документов: %s/%s ", loaded, total ), Toast.LENGTH_LONG);
    }

    mToast.setText( String.format(  "Загрузка документов: %s/%s ", loaded, total ) );
    mToast.show();
  }



  @OnClick(R.id.activity_main_left_button)
  public void setLeftArrowArrow() {
    showNextType(false);
  }

  @OnClick(R.id.activity_main_right_button)
  public void setRightArrow() {
    showNextType(true);
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
    document_type_adapter.updateCountByType( event.uid );

    loaded++;
    notificationUpdate();
  }




  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(LoadAllDocumentsByStatusEvent event) {
    //    table_changes();
  }

}

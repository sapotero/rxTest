package sapotero.rxtest.views.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.db.requery.CreateDoc;
import sapotero.rxtest.db.requery.Doc;
import sapotero.rxtest.events.bus.GetDocumentInfoEvent;
import sapotero.rxtest.events.rx.InsertRxDocumentsEvent;
import sapotero.rxtest.jobs.bus.GetDocumentInfoJob;
import sapotero.rxtest.retrofit.DocumentsService;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.retrofit.models.documents.Documents;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import sapotero.rxtest.views.adapters.DocumentsAdapter;
import sapotero.rxtest.views.adapters.models.FilterItem;
import sapotero.rxtest.views.adapters.utils.StatusAdapter;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

  private String TOKEN    = "";
  private String LOGIN    = "";
  private String PASSWORD = "";

  private String total;
  private String TAG = MainActivity.class.getSimpleName();

  @SuppressLint("StaticFieldLeak")
  private static MainActivity context;

  @SuppressLint("StaticFieldLeak")
  protected static RecyclerView rvv;

  @BindView(R.id.documentsRecycleView) RecyclerView rv;
  @BindView(R.id.progressBar) View progressBar;
  @BindView(R.id.DOCUMENT_TYPE) Spinner DOCUMENT_TYPE_SELECTOR;
  @BindView(R.id.JOURNAL_TYPE)  Spinner JOURNAL_TYPE_SELECTOR;
  @BindView(R.id.ORGANIZATION)  Spinner ORGANIZATION_SELECTOR;

  @BindView(R.id.toolbar) Toolbar toolbar;

  @Inject JobManager jobManager;
  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
//  @Inject public BriteDatabase db;
  @Inject public SingleEntityStore<Persistable> dataStore;

  private StatusAdapter filterAdapter;
  private List<Document> loaded_documents;
  private Drawer drawer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);


    context = this;
    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    loadSettings();

    progressBar.setVisibility(ProgressBar.INVISIBLE);
    setAdapters();

    GridLayoutManager gridLayoutManager = new GridLayoutManager( this, 2, GridLayoutManager.VERTICAL, false );
    rv.setLayoutManager(gridLayoutManager);


    AccountHeader headerResult = new AccountHeaderBuilder()
      .withActivity(this)
      .withHeaderBackground(R.drawable.header)
      .addProfiles(
        new ProfileDrawerItem()
          .withName("Admin")
          .withEmail("admin_id")
          .withIcon(MaterialDesignIconic.Icon.gmi_account)
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
      .withAccountHeader(headerResult)
      .addDrawerItems(
        new PrimaryDrawerItem().withName(R.string.drawer_item_home)
          .withIcon(MaterialDrawerFont.Icon.mdf_arrow_drop_down)
          .withBadge("99")
          .withIdentifier(1),

        new SectionDrawerItem().withName(R.string.drawer_item_settings),

        new SecondaryDrawerItem()
          .withName(R.string.drawer_item_settings_account)
          .withIcon(MaterialDesignIconic.Icon.gmi_accounts),
        new SecondaryDrawerItem()
          .withName(R.string.drawer_item_settings_config)
          .withIcon(MaterialDesignIconic.Icon.gmi_settings),
        new SecondaryDrawerItem()
          .withName(R.string.drawer_item_settings_templates)
          .withIcon(MaterialDesignIconic.Icon.gmi_comment_edit),

        new DividerDrawerItem(),
        new SecondaryDrawerItem()
          .withName(R.string.drawer_item_debug)
          .withIcon(MaterialDesignIconic.Icon.gmi_developer_board)
          .withIdentifier(99)
      )
      .withOnDrawerItemClickListener(
        (view, position, drawerItem) -> {

          Timber.tag(TAG).i( String.valueOf(view) );
          Timber.tag(TAG).i(String.valueOf(position));
          Timber.tag(TAG).i(String.valueOf( drawerItem.getIdentifier() ));

          Intent intent = new Intent(this, SettingsActivity.class);
          startActivity(intent);

          return false;

        }
      )
      .build();
    toolbar.setSubtitle("subtitle");
    toolbar.setTitle("TITLE");
    toolbar.setTitleTextColor( getResources().getColor( R.color.md_grey_100 ) );
    toolbar.setSubtitleTextColor( getResources().getColor( R.color.md_grey_400 ) );

    toolbar.inflateMenu(R.menu.info);
  }

  private void loadSettings() {
    Preference<String> username = settings.getString("login");
    LOGIN = username.get();

    Preference<String> password = settings.getString("password");
    PASSWORD = password.get();

    Preference<String> token = settings.getString("token");
    TOKEN = token.get();

    Timber.tag(TAG).v("LOGIN: "+ LOGIN );
    Timber.tag(TAG).v("PASSWORD: "+ PASSWORD );
    Timber.tag(TAG).v("TOKEN: "+ TOKEN );
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.info, menu);

    menu.add(0, 0, 0, "Загрузка документов")
      .setIcon(android.R.drawable.ic_menu_info_details)
      .setOnMenuItemClickListener(
        item -> {

          dataStore.count(Doc.class).get().toSingle()
            .subscribe(count -> {
              if (count == 0) {
                Observable.fromCallable(new CreateDoc(dataStore))
                  .flatMap((Func1<Observable<Iterable<Doc>>, Observable<?>>) o -> o)
                  .observeOn(Schedulers.computation())
                  .subscribe( o -> {
                     Timber.tag(TAG).e(String.valueOf(o));
                  });
              }
            });

//          loadSettings();
//
//          try {
//            jobManager.addJobInBackground( new UpdateAuthTokenJob(LOGIN, PASSWORD) );
//          } catch ( Exception e){
//            Timber.tag(TAG + " process error").v( e );
//          }

          return true;
        })
      .setShowAsAction( MenuItem.SHOW_AS_ACTION_ALWAYS);

    menu.add(0, 0, 0, "test")
      .setIcon(android.R.drawable.ic_input_add)
      .setOnMenuItemClickListener(
        item -> {
          loadSettings();
          return true;
        })
      .setShowAsAction( MenuItem.SHOW_AS_ACTION_ALWAYS);

    menu.add(0, 0, 0, "Q")
      .setIcon(android.R.drawable.ic_popup_disk_full)
      .setOnMenuItemClickListener(
        item -> {

          Observable<Doc> observable = dataStore
            .select(Doc.class)
            .get()
            .toObservable();

          observable.subscribeOn( Schedulers.newThread() )
            .subscribe(
              docs -> {
                Timber.tag(TAG).v( String.valueOf(docs.getEmail()) );
                Timber.tag(TAG).v( String.valueOf(docs.getSigner().getCity()) );
              }
            );



//          try {
//            jobManager.addJobInBackground( new InsertRxDocumentsJob(loaded_documents) );
//          } catch ( Exception e){
//            Timber.tag(TAG + " massInsert error").v( e );
//          }

          return true;
        })
      .setShowAsAction( MenuItem.SHOW_AS_ACTION_ALWAYS);




    return true;
  }

  @Override
  public void onStart() {
    super.onStart();

    if ( !EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().register(this);
    }
  }

  @Override
  public void onStop() {
    if ( EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().unregister(this);
    }
    super.onStop();
  }

  private void setAdapters() {

    List<FilterItem> filters = new ArrayList<FilterItem>();

    String[] filter_types = getResources().getStringArray(R.array.DOCUMENT_TYPES_VALUE);
    String[] filter_names = getResources().getStringArray(R.array.DOCUMENT_TYPES);

    for (int i = 0; i < filter_types.length; i++) {
      filters.add(new FilterItem( filter_names[i] , filter_types[i], "0"));
    }

    filterAdapter = new StatusAdapter(this, filters );
    DOCUMENT_TYPE_SELECTOR.setAdapter(filterAdapter);

    DOCUMENT_TYPE_SELECTOR.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        loadDocuments();
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
      }
    });

    // устанавливаем тип документов
    JOURNAL_TYPE_SELECTOR = (Spinner) findViewById(R.id.JOURNAL_TYPE);
    ArrayAdapter<CharSequence> journal_adapter = ArrayAdapter.createFromResource(this, R.array.JOURNAL_TYPES, android.R.layout.simple_spinner_item);
    journal_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    JOURNAL_TYPE_SELECTOR.setAdapter(journal_adapter);

    JOURNAL_TYPE_SELECTOR.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        int spinner_pos = JOURNAL_TYPE_SELECTOR.getSelectedItemPosition();
        String[] document_type = getResources().getStringArray(R.array.JOURNAL_TYPES_VALUE);
        String type = String.valueOf(document_type[spinner_pos]);

        Toast.makeText(context, type, Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
      }
    });

    // устанавливаем тип документов
    ArrayAdapter<CharSequence> organization_adapter = ArrayAdapter.createFromResource(this, R.array.ORGANIZATIONS, android.R.layout.simple_spinner_item);
    organization_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    ORGANIZATION_SELECTOR.setAdapter(organization_adapter);

    ORGANIZATION_SELECTOR.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
      }
    });

    Observable.from(filter_types).subscribe(
      this::loadDocumentsCountByType
    );
  }

  private void loadDocumentsCountByType( String TYPE){

    Retrofit retrofit = new RetrofitManager( this, Constant.HOST + "/v3/", okHttpClient).process();
    DocumentsService documentsService = retrofit.create( DocumentsService.class );

    Observable<Documents> documents = documentsService.getDocuments( LOGIN, TOKEN, TYPE, 0,0);

    documents.subscribeOn( Schedulers.newThread() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          total = data.getMeta().getTotal();

          if ( total != null && Integer.valueOf(total) > 0 ){


            String[] values = getResources().getStringArray(R.array.DOCUMENT_TYPES_VALUE);

            int index = -1;
            for (int i=0;i<values.length;i++) {
              if (values[i].equals(TYPE)) {
                index = i;
                break;
              }
            }

            Timber.tag(TAG).i( TYPE + " - " + total + " | " + index );

            FilterItem filterItem = filterAdapter.getItem(index);

            filterItem.setCount( total );
            filterAdapter.notifyDataSetChanged();
          }

        },
        error -> {
          Timber.tag(TAG).d( "_ERROR", error.getMessage() );
        });

  }

  private void loadDocuments(){

    progressBar.setVisibility(ProgressBar.VISIBLE);

    Retrofit retrofit = new RetrofitManager( this, Constant.HOST + "/v3/", okHttpClient).process();
    DocumentsService documentsService = retrofit.create( DocumentsService.class );

    int spinner_pos = DOCUMENT_TYPE_SELECTOR.getSelectedItemPosition();

    String[] document_type = getResources().getStringArray(R.array.DOCUMENT_TYPES_VALUE);
    String type = String.valueOf(document_type[spinner_pos]);

    Observable<Documents> documents = documentsService.getDocuments( LOGIN, TOKEN, type, 100,0);

    documents.subscribeOn( Schedulers.newThread() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          progressBar.setVisibility(ProgressBar.INVISIBLE);

          List<Document> docs = data.getDocuments();
          total = data.getMeta().getTotal();

          loaded_documents = docs;

          DocumentsAdapter documentsAdapter = new DocumentsAdapter(this, docs);
          rv.setAdapter(documentsAdapter);

//          Observable<SqlBrite.Query> users = db.createQuery(RxDocuments.TABLE, "SELECT * FROM "+RxDocuments.TABLE);
//          users.subscribe(query -> {
//            Cursor cursor = query.run();
//            if (cursor != null) {
//              while (cursor.moveToNext()) {
////                documentsAdapter.addItem(
////                  new Document(
////                    cursor.getString(0)
////                  )
////                );
//              }
//              documentsAdapter.notifyDataSetChanged();
//            }
//          });


          rvv = rv;

          FilterItem filterItem = filterAdapter.getItem(spinner_pos);
          filterItem.setCount( total );
          filterAdapter.notifyDataSetChanged();

        },
        error -> {
          Log.d( "_ERROR", error.getMessage() );
          progressBar.setVisibility(ProgressBar.INVISIBLE);

          Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
        });

  }

  private void process() {
    Toast.makeText( context, "Total: " + total, Toast.LENGTH_SHORT).show();
    try {
      jobManager.addJobInBackground( new GetDocumentInfoJob("1"));
    } catch ( Exception e){
      Log.d( "_ERROR", e.toString() );
    }

  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(GetDocumentInfoEvent event) {
    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(InsertRxDocumentsEvent event) {
    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show();
  }

}

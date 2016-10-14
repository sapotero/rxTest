package sapotero.rxtest.views.activities;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.db.models.RxDocuments;
import sapotero.rxtest.events.bus.GetDocumentInfoEvent;
import sapotero.rxtest.events.rx.InsertRxDocumentsEvent;
import sapotero.rxtest.jobs.bus.GetDocumentInfoJob;
import sapotero.rxtest.jobs.bus.UpdateAuthTokenJob;
import sapotero.rxtest.jobs.rx.InsertRxDocumentsJob;
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

  @Inject JobManager jobManager;
  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject public BriteDatabase db;

  private StatusAdapter filterAdapter;
  private List<Document> loaded_documents;

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

          loadSettings();

          try {
            jobManager.addJobInBackground( new UpdateAuthTokenJob(LOGIN, PASSWORD) );
          } catch ( Exception e){
            Timber.tag(TAG + " process error").v( e );
          }

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

          try {
            jobManager.addJobInBackground( new InsertRxDocumentsJob(loaded_documents) );
          } catch ( Exception e){
            Timber.tag(TAG + " massInsert error").v( e );
          }

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

          Observable<SqlBrite.Query> users = db.createQuery(RxDocuments.TABLE, "SELECT * FROM "+RxDocuments.TABLE);
          users.subscribe(query -> {
            Cursor cursor = query.run();
            if (cursor != null) {
              while (cursor.moveToNext()) {
                documentsAdapter.addItem(
                  new Document(
                    cursor.getString(0),
                    cursor.getString(0),
                    cursor.getString(0),
                    cursor.getString(0),
                    cursor.getString(0),
                    cursor.getString(0),
                    cursor.getString(0),
                    cursor.getString(0),
                    cursor.getString(0)
                  )
                );
              }
              documentsAdapter.notifyDataSetChanged();
            }
          });


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

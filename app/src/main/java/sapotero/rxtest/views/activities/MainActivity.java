package sapotero.rxtest.views.activities;

import android.content.Intent;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
import sapotero.rxtest.events.bus.GetDocumentInfoEvent;
import sapotero.rxtest.jobs.bus.GetDocumentInfoJob;
import sapotero.rxtest.retrofit.DocumentsService;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.retrofit.models.documents.Documents;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import sapotero.rxtest.views.adapters.DocumentsAdapter;

public class MainActivity extends AppCompatActivity {

  private static String TOKEN    = "";

  private static String LOGIN    = "";
  private static String PASSWORD = "";
  private static Integer total = 0;

  private static MainActivity context;
  protected static RecyclerView rvv;
  @BindView(R.id.documentsRecycleView) RecyclerView rv;

  @BindView(R.id.progressBar) View progressBar;
  @BindView(R.id.DOCUMENT_TYPE) Spinner DOCUMENT_TYPE_SELECTOR;
  @BindView(R.id.JOURNAL_TYPE)  Spinner JOURNAL_TYPE_SELECTOR;
  @BindView(R.id.ORGANIZATION)  Spinner ORGANIZATION_SELECTOR;

  @Inject JobManager jobManager;
  @Inject OkHttpClient okHttpClient;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    context = this;
    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    Bundle extras = getIntent().getExtras();

    if (extras != null) {
      LOGIN    = extras.getString( EsdApplication.LOGIN);
      TOKEN    = extras.getString( EsdApplication.TOKEN);
      PASSWORD = extras.getString( EsdApplication.PASSWORD);

      Log.d( "__INTENT", LOGIN );
      Log.d( "__INTENT", PASSWORD );
      Log.d( "__INTENT", TOKEN );
    }

    // устанавливаем статус документов
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.DOCUMENT_TYPES, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    DOCUMENT_TYPE_SELECTOR.setAdapter(adapter);

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

    GridLayoutManager   gridLayoutManager = new GridLayoutManager(
      this,
      2,
      GridLayoutManager.VERTICAL,
      false
    );

    rv.setLayoutManager(gridLayoutManager);
    progressBar.setVisibility(ProgressBar.INVISIBLE);


  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.info, menu);

    menu.add(0, 0, 0, "Загрузка документов")
      .setIcon(android.R.drawable.ic_menu_info_details)
      .setOnMenuItemClickListener(
        item -> {

          process();

          return true;
        })
      .setShowAsAction( MenuItem.SHOW_AS_ACTION_ALWAYS);

    return true;
  }

  @Override
  public void onStart() {
    super.onStart();
    EventBus.getDefault().register(this);
  }

  @Override
  public void onStop() {
    EventBus.getDefault().unregister(this);
    super.onStop();
  }

  private void loadDocuments(){

    progressBar.setVisibility(ProgressBar.VISIBLE);

    Retrofit retrofit = new RetrofitManager( this, EsdApplication.HOST + "/v3/", okHttpClient).process();
    DocumentsService documentsService = retrofit.create( DocumentsService.class );

    int spinner_pos = DOCUMENT_TYPE_SELECTOR.getSelectedItemPosition();
    String[] document_type = getResources().getStringArray(R.array.DOCUMENT_TYPES_VALUE);
    String type = String.valueOf(document_type[spinner_pos]);

    Observable<Documents> documents = documentsService.getDocuments( LOGIN, TOKEN, type);

    documents.subscribeOn( Schedulers.newThread() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          progressBar.setVisibility(ProgressBar.INVISIBLE);

          List<Document> docs = data.getDocuments();
          total = docs.size();

          DocumentsAdapter adapter = new DocumentsAdapter(this, docs);
          rv.setAdapter(adapter);
          rvv = rv;
        },
        error -> {
          Log.d( "_ERROR", error.getMessage() );
          progressBar.setVisibility(ProgressBar.INVISIBLE);

          Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
        });

  }

  public static void showDocumentInfo(View view, int position) {
    Intent intent = new Intent(context, InfoActivity.class);

    intent.putExtra( EsdApplication.LOGIN,    LOGIN );
    intent.putExtra( EsdApplication.PASSWORD, PASSWORD );
    intent.putExtra( EsdApplication.TOKEN,    TOKEN );
    intent.putExtra(String.valueOf(EsdApplication.POSITION),    position );

    context.startActivity(intent);
  }
  private void process() {
    Toast.makeText( context, "Total: "+total.toString(), Toast.LENGTH_SHORT).show();
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
}

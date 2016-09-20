package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.EsdConfig;
import sapotero.rxtest.R;
import sapotero.rxtest.models.documents.Document;
import sapotero.rxtest.models.documents.Documents;
import sapotero.rxtest.retrofit.DocumentsService;
import sapotero.rxtest.retrofit.utils.RxErrorHandlingCallAdapterFactory;
import sapotero.rxtest.views.adapters.DocumentsAdapter;

public class MainActivity extends AppCompatActivity {

    private static String TOKEN    = "";
    private static String LOGIN    = "";
    private static String PASSWORD = "";

  private static MainActivity context;

  private static View progressBar;
    public static RecyclerView rv;

    private static Spinner DOCUMENT_TYPE_SELECTOR;

  final OkHttpClient okHttpClient = new OkHttpClient.Builder()
      .readTimeout(60, TimeUnit.SECONDS)
      .connectTimeout(60, TimeUnit.SECONDS)
      .addNetworkInterceptor(
          new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS)
      )
      .build();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    context = this;

    Bundle extras = getIntent().getExtras();

    if (extras != null) {
      LOGIN    = extras.getString( EsdConfig.LOGIN);
      TOKEN    = extras.getString( EsdConfig.TOKEN);
      PASSWORD = extras.getString( EsdConfig.PASSWORD);

      Log.d( "__INTENT", LOGIN );
      Log.d( "__INTENT", PASSWORD );
      Log.d( "__INTENT", TOKEN );
    }

    progressBar  = findViewById(R.id.progressBar);



    // устанавливаем тип документов
    DOCUMENT_TYPE_SELECTOR = (Spinner) findViewById(R.id.DOCUMENT_TYPE);
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


    rv = (RecyclerView) findViewById(R.id.documentsRecycleView);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

    GridLayoutManager   gridLayoutManager = new GridLayoutManager(
      this,
      2,
      GridLayoutManager.VERTICAL,
      false
    );

    rv.setLayoutManager(gridLayoutManager);
    progressBar.setVisibility(ProgressBar.INVISIBLE);


  }

  private void loadDocuments(){

    progressBar.setVisibility(ProgressBar.VISIBLE);

    Retrofit retrofit = new Retrofit.Builder()
        .client(okHttpClient)
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://mobile.esd.n-core.ru/v3/")
        .build();

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
                DocumentsAdapter adapter = new DocumentsAdapter(this, docs);
                rv.setAdapter(adapter);
            },
            error -> {
                Log.d( "_ERROR", error.getMessage() );
                progressBar.setVisibility(ProgressBar.INVISIBLE);

                Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            });

  }


  public static void showDocumentInfo(View view, int position) {
    Intent intent = new Intent(context, InfoActivity.class);
    intent.putExtra( EsdConfig.LOGIN,    LOGIN );
    intent.putExtra( EsdConfig.PASSWORD, PASSWORD );
    intent.putExtra( EsdConfig.TOKEN,    TOKEN );
    intent.putExtra(String.valueOf(EsdConfig.POSITION),    position );

    context.startActivity(intent);
  }
}

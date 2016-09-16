package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
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
import sapotero.rxtest.R;
import sapotero.rxtest.models.AuthToken;
import sapotero.rxtest.models.document.DocumentInfo;
import sapotero.rxtest.models.documents.Document;
import sapotero.rxtest.models.documents.Documents;
import sapotero.rxtest.retrofit.AuthTokenService;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.DocumentsService;
import sapotero.rxtest.views.adapters.DocumentsAdapter;

public class MainActivity extends AppCompatActivity {

  private static View progressBar;
  private TextView auth_token;
  private static RecyclerView rv;
  private static String token;

  private static TextView uid;
  private static TextView md5;
  private static TextView title;
  private static TextView registration_number;
  private static TextView urgency;
  private static TextView short_description;
  private static TextView comment;
  private static TextView external_document_number;
  private static TextView receipt_date;

  private static TableLayout infoTable;

  final OkHttpClient okHttpClient = new OkHttpClient.Builder()
      .readTimeout(60, TimeUnit.SECONDS)
      .connectTimeout(60, TimeUnit.SECONDS)
      .addInterceptor(
          new HttpLoggingInterceptor(
            message -> {
              Log.d("_ERROR", message);
            }
      ))
      .build();


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    progressBar  = findViewById(R.id.progressBar);
    auth_token   = (TextView) findViewById(R.id.auth_token);

    uid                      = (TextView) findViewById(R.id.uid);
    md5                      = (TextView) findViewById(R.id.md5);
    title                    = (TextView) findViewById(R.id.title);
    registration_number      = (TextView) findViewById(R.id.registration_number);
    urgency                  = (TextView) findViewById(R.id.urgency);
    short_description        = (TextView) findViewById(R.id.short_description);
    comment                  = (TextView) findViewById(R.id.comment);
    external_document_number = (TextView) findViewById(R.id.external_document_number);
    receipt_date             = (TextView) findViewById(R.id.receipt_date);
    infoTable                = (TableLayout) findViewById(R.id.infoTable);

    rv = (RecyclerView) findViewById(R.id.documentsRecycleView);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

    rv.setLayoutManager(linearLayoutManager);
    progressBar.setVisibility(ProgressBar.INVISIBLE);


  }


  public void run(View view) throws InterruptedException {
    progressBar.setVisibility(ProgressBar.VISIBLE);

    Retrofit retrofit = new Retrofit.Builder()
        .client(okHttpClient)
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://mobile.esd.n-core.ru/")
        .build();

    AuthTokenService authTokenService = retrofit.create( AuthTokenService.class );

    Observable<AuthToken> user = authTokenService.getAuth("admin", "123456");

    user.subscribeOn( Schedulers.newThread() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
            data -> {

                String _token = data.getAuthToken();

                progressBar.setVisibility(ProgressBar.INVISIBLE);
                auth_token.setText( "Auth_token: " + _token);

                token = _token;

                loadDocuments();

                Log.e("_", _token );
            },
            error -> {
              Log.d( "_ERROR", error.getMessage() );
              progressBar.setVisibility(ProgressBar.INVISIBLE);

              Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
  }

  private void loadDocuments(){

    progressBar.setVisibility(ProgressBar.VISIBLE);

    Retrofit retrofit = new Retrofit.Builder()
        .client(okHttpClient)
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://mobile.esd.n-core.ru/v3/")
        .build();

    DocumentsService documentsService = retrofit.create( DocumentsService.class );

    Observable<Documents> documents = documentsService.getDocuments("admin", token, "sent_to_the_report");

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

  public static void showDocumentInfo(View view, int position){
    progressBar.setVisibility(ProgressBar.VISIBLE);
    infoTable.setVisibility(View.INVISIBLE);

    Log.d("_showDocumentInfo", String.valueOf(position));
    Log.d("_showDocumentInfo", String.valueOf(rv.getAdapter().getItemCount()));

    DocumentsAdapter rvAdapter = (DocumentsAdapter) rv.getAdapter();

    Retrofit retrofit = new Retrofit.Builder()
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://mobile.esd.n-core.ru/v3/documents/")
        .build();

    DocumentService documentService = retrofit.create( DocumentService.class );

    Observable<DocumentInfo> info = documentService.getInfo(
        rvAdapter.getItem(position).getUid(),
        "admin",
        token
    );

    info.subscribeOn( Schedulers.newThread() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
            data -> {
                progressBar.setVisibility(ProgressBar.INVISIBLE);

                infoTable.setVisibility(View.VISIBLE);

                uid.setText( data.getUid() );
                md5.setText( data.getMd5() );
                title.setText( data.getTitle() );
                registration_number.setText( data.getRegistrationNumber() );
                urgency.setText( data.getUrgency() );
                short_description.setText( data.getShortDescription() );
                comment.setText( data.getComment() );
                external_document_number.setText( data.getExternalDocumentNumber() );
                receipt_date.setText( data.getReceiptDate() );
        },
            error -> {
              Log.d( "_ERROR", error.getMessage() );
              progressBar.setVisibility(ProgressBar.INVISIBLE);

//              Toast.makeText( this, error.getMessage(), Toast.LENGTH_SHORT).show();
            });

  }
}

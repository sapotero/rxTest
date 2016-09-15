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

import java.util.List;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
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
import sapotero.rxtest.utils.RecyclerItemClickListener;
import sapotero.rxtest.views.adapters.DocumentsAdapter;

public class MainActivity extends AppCompatActivity {

  private View progressBar;
  private TextView auth_token;
  private RecyclerView rv;
  private String token;

  private TextView uid;
  private TextView md5;
  private TextView title;
  private TextView registration_number;
  private TextView urgency;
  private TextView short_description;
  private TextView comment;
  private TextView external_document_number;
  private TextView receipt_date;

  private TableLayout infoTable;


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

    rv           = (RecyclerView) findViewById(R.id.documentsRecycleView);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
    rv.setLayoutManager(linearLayoutManager);
    rv.setHasFixedSize(true);

    rv.addOnItemTouchListener(
        new RecyclerItemClickListener(this, rv,new RecyclerItemClickListener.OnItemClickListener() {
          @Override public void onItemClick(View view, int position) {
            showDocumentInfo(view, position);
          }

          @Override public void onLongItemClick(View view, int position) {
            // do whatever
          }
        })
    );

    progressBar.setVisibility(ProgressBar.INVISIBLE);

  }

  Subscriber<String> mySubscriber = new Subscriber<String>() {
    @Override
    public void onNext(String s) {
      System.out.println(s);
    }

    @Override
    public void onCompleted() { }

    @Override
    public void onError(Throwable e) { }
  };

  Observable<String> myObservable = Observable.create(
      new Observable.OnSubscribe<String>() {
        @Override
        public void call(Subscriber<? super String> sub) {
          sub.onNext("Hello, world!");
          sub.onCompleted();
        }
      }
  );

  public void run(View view) throws InterruptedException {
    progressBar.setVisibility(ProgressBar.VISIBLE);
    Retrofit retrofit = new Retrofit.Builder()
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://mobile.esd.n-core.ru/")
        .build();

    AuthTokenService authTokenService = retrofit.create( AuthTokenService.class );

    Observable<AuthToken> user = authTokenService.getAuth("admin", "123456");

    user.subscribeOn( Schedulers.newThread() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(data -> {

          String _token = data.getAuthToken();

          progressBar.setVisibility(ProgressBar.INVISIBLE);
          auth_token.setText( "Auth_token: " + _token);

          token = _token;

          loadDocuments();

          Log.e("_", _token );
        });
  }

  private void loadDocuments(){

    progressBar.setVisibility(ProgressBar.VISIBLE);

    Retrofit retrofit = new Retrofit.Builder()
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://mobile.esd.n-core.ru/v3/")
        .build();

    DocumentsService documentsService = retrofit.create( DocumentsService.class );

    Observable<Documents> documents = documentsService.getDocuments("admin", token, "sent_to_the_report");

    documents.subscribeOn( Schedulers.newThread() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(data -> {

          progressBar.setVisibility(ProgressBar.INVISIBLE);

          List<Document> docs = data.getDocuments();
          DocumentsAdapter adapter = new DocumentsAdapter(docs);
          rv.setAdapter(adapter);
        });

  }

  private void showDocumentInfo(View view, int position){
    progressBar.setVisibility(ProgressBar.VISIBLE);
    infoTable.setVisibility(View.INVISIBLE);

    Log.d("_showDocumentInfo", String.valueOf(position));
    Log.d("_showDocumentInfo", String.valueOf(rv.getAdapter().getItemCount()));

    DocumentsAdapter rvAdapter = (DocumentsAdapter) rv.getAdapter();
    Log.d("_showDocumentInfo", rvAdapter.getItem(position).getUid() );

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
        .subscribe(data -> {
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



        });

  }
}

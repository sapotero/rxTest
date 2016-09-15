package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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
import sapotero.rxtest.models.documents.Document;
import sapotero.rxtest.models.documents.Documents;
import sapotero.rxtest.retrofit.AuthTokenService;
import sapotero.rxtest.retrofit.DocumentsService;
import sapotero.rxtest.utils.RecyclerItemClickListener;
import sapotero.rxtest.views.adapters.DocumentsAdapter;

public class MainActivity extends AppCompatActivity {

  private View progressBar;
  private TextView auth_token;
  private RecyclerView rv;
  private String token;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    progressBar  = findViewById(R.id.progressBar);
    auth_token   = (TextView) findViewById(R.id.auth_token);

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

          List<Document> docs = data.getDocuments();
          DocumentsAdapter adapter = new DocumentsAdapter(docs);
          rv.setAdapter(adapter);

          Log.e("_", docs.get(0).getMd5() );
        });

  }

  private void showDocumentInfo(View view, int position){
    Log.d("_showDocumentInfo", String.valueOf(position));
    Log.d("_showDocumentInfo", String.valueOf(rv.getAdapter().getItemCount()));

    DocumentsAdapter rvAdapter = (DocumentsAdapter) rv.getAdapter();


    Log.d("_showDocumentInfo", rvAdapter.getItem(position).getUid() );

  }
}

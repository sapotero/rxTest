package sapotero.rxtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.models.AuthToken;
import sapotero.rxtest.models.Documents;
import sapotero.rxtest.retrofit.AuthTokenService;
import sapotero.rxtest.retrofit.DocumentsService;

public class MainActivity extends AppCompatActivity {

  private View progressBar;
  private TextView auth_token;
  private String token;

  private ListView documentList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    progressBar  = findViewById(R.id.progressBar);
    auth_token   = (TextView) findViewById(R.id.auth_token);




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

          Log.e("_", data.getDocuments().get(0).getMd5() );
        });

  }
}

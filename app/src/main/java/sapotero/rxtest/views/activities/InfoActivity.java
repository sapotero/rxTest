package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.EsdConfig;
import sapotero.rxtest.R;
import sapotero.rxtest.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.views.adapters.DocumentsAdapter;

public class InfoActivity extends AppCompatActivity {

  private static TextView uid;
  private static TextView sort_key;
  private static TextView title;
  private static TextView registration_number;
  private static TextView urgency;
  private static TextView short_description;
  private static TextView comment;
  private static TextView external_document_number;
  private static TextView receipt_date;
  private static TextView signer;
  private static TextView organisation;

  private static String TOKEN    = "";
  private static String LOGIN    = "";
  private static String PASSWORD = "";
  private static Integer POSITION = 0;
  private View loader;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_info);

    uid                      = (TextView) findViewById(R.id._uid);
    sort_key                 = (TextView) findViewById(R.id.SortKey);
    title                    = (TextView) findViewById(R.id._title);
    registration_number      = (TextView) findViewById(R.id.registration_number);
    urgency                  = (TextView) findViewById(R.id.urgency);
    short_description        = (TextView) findViewById(R.id.short_description);
    comment                  = (TextView) findViewById(R.id.comment);
    external_document_number = (TextView) findViewById(R.id.external_document_number);
    receipt_date             = (TextView) findViewById(R.id.receipt_date);

    loader  = findViewById(R.id.loader);
    loader.setVisibility(ProgressBar.VISIBLE);

    signer = (TextView) findViewById(R.id.signer);
    organisation = (TextView) findViewById(R.id.organisation);

    Bundle extras = getIntent().getExtras();

    if (extras != null) {
      LOGIN    = extras.getString( EsdConfig.LOGIN);
      TOKEN    = extras.getString( EsdConfig.TOKEN);
      PASSWORD = extras.getString( EsdConfig.PASSWORD);
      POSITION = extras.getInt(String.valueOf(EsdConfig.POSITION));

      Log.d( "__INTENT", LOGIN );
      Log.d( "__INTENT", PASSWORD );
      Log.d( "__INTENT", TOKEN );

      DocumentsAdapter rvAdapter = (DocumentsAdapter) MainActivity.rv.getAdapter();

      Retrofit retrofit = new Retrofit.Builder()
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("http://mobile.esd.n-core.ru/v3/documents/")
        .build();

      DocumentService documentService = retrofit.create( DocumentService.class );

      Observable<DocumentInfo> info = documentService.getInfo(
        rvAdapter.getItem(POSITION).getUid(),
        LOGIN,
        TOKEN
      );

      info.subscribeOn( Schedulers.newThread() )
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(
          data -> {
            loader.setVisibility(ProgressBar.INVISIBLE);

            title.setText( data.getTitle() );
            uid.setText( data.getUid() );
            sort_key.setText( data.getSortKey().toString() );
            registration_number.setText(data.getRegistrationNumber());
            urgency.setText( data.getUrgency() );
            short_description.setText( data.getShortDescription() );
            comment.setText( data.getComment() );
            external_document_number.setText(data.getExternalDocumentNumber());
            receipt_date.setText(data.getReceiptDate());

            signer.setText( data.getSigner().getName() );
            organisation.setText( data.getSigner().getOrganisation() );
          },
          error -> {
            loader.setVisibility(ProgressBar.INVISIBLE);
              Log.d( "_ERROR", error.getMessage() );
              Toast.makeText( this, error.getMessage(), Toast.LENGTH_SHORT).show();
          });
    }
  }

}

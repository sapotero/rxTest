package sapotero.rxtest.views.activities;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.mikepenz.actionitembadge.library.ActionItemBadge;
import com.mikepenz.actionitembadge.library.ActionItemBadgeAdder;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.squareup.sqlbrite.BriteDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.db.models.Auth;
import sapotero.rxtest.events.bus.MassInsertDoneEvent;
import sapotero.rxtest.events.bus.SetActiveDecisonEvent;
import sapotero.rxtest.jobs.bus.MassInsertJob;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.views.adapters.DecisionAdapter;
import sapotero.rxtest.views.adapters.DocumentsAdapter;
import sapotero.rxtest.views.adapters.TabPagerAdapter;
import sapotero.rxtest.views.fragments.InfoCardFragment;
import timber.log.Timber;

public class InfoActivity extends AppCompatActivity implements InfoCardFragment.OnFragmentInteractionListener {

  @BindView(R.id.desigions_recycler_view) RecyclerView desigions_recycler_view;
  @BindView(R.id.desigion_view)           LinearLayout desigion_view;


  @BindView(R.id.loader)                   View loader;

  @BindView(R.id.tab_main)                 ViewPager viewPager;
  @BindView(R.id.tabs)                     TabLayout tabLayout;

  @Inject BriteDatabase db;
  @Inject JobManager jobManager;
  @Inject CompositeSubscription subscriptions;
  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;

  private static byte[] CARD;

  private static String  TOKEN    = "";
  private static String  LOGIN    = "";
  private static String  PASSWORD = "";
  private static Integer POSITION = 0;

  private DocumentInfo DOCUMENT;

  private Menu button;
  private Auth Auth;
  private String TAG = InfoActivity.class.getSimpleName();

  private Context context;
  private DecisionAdapter decision_adapter;

//  @Singleton
//  private CompositeSubscription subscriptions = new CompositeSubscription();;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    // меняем загрузочную тему
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    context = this;

    EsdApplication.getComponent(this).inject(this);

    setContentView(R.layout.activity_info);
    ButterKnife.bind(this);

    loadSettings();
    setTabContent();
    loadDocuments();
  }

  private void loadDocuments() {
    DocumentsAdapter rvAdapter = (DocumentsAdapter) MainActivity.rvv.getAdapter();

    Retrofit retrofit = new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl(Constant.HOST + "v3/documents/")
      .client(okHttpClient)
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
          DOCUMENT = data;

          loader.setVisibility(ProgressBar.INVISIBLE);

          setTitle( data.getTitle() );

//          title.setText( data.getTitle() );
//          uid.setText( data.getUid() );
//          //            sort_key.setText( data.getSortKey() );
//          registration_number.setText(data.getRegistrationNumber());
//          urgency.setText( data.getUrgency() );
//          short_description.setText( data.getShortDescription() );
//          external_document_number.setText(data.getExternalDocumentNumber());
//          receipt_date.setText(data.getReceiptDate());
//          comment.setText( data.getComment() );
//
//          signer.setText( data.getSigner().getName() );
//          organisation.setText( data.getSigner().getOrganisation() );

          if ( data.getDecisions().size() >= 1 ){

            desigions_recycler_view.setLayoutManager(new LinearLayoutManager(this));

            List<Decision> decisions_list = new ArrayList<>();
            for (Decision decision: data.getDecisions()) {
              decisions_list.add(decision);
            }

            decision_adapter = new DecisionAdapter(decisions_list, this, desigions_recycler_view);
            desigions_recycler_view.setAdapter(decision_adapter);
            desigions_recycler_view.setLayoutManager(new LinearLayoutManager(this));

            RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
            itemAnimator.setAddDuration(1000);
            itemAnimator.setRemoveDuration(1000);
            desigions_recycler_view.setItemAnimator(itemAnimator);

          } else {
//            decision_row.setVisibility(TableRow.INVISIBLE);
          }

          CARD = Base64.decode( data.getInfoCard().getBytes(), Base64.DEFAULT );

        },
        error -> {
          loader.setVisibility(ProgressBar.INVISIBLE);
          Log.d( "_ERROR", error.getMessage() );
          Toast.makeText( this, error.getMessage(), Toast.LENGTH_SHORT).show();
        });
  }

  private void setTabContent() {
    viewPager.setAdapter( new TabPagerAdapter(getSupportFragmentManager(), InfoActivity.this) );

    tabLayout.setupWithViewPager(viewPager);
    tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override
      public void onTabSelected(TabLayout.Tab tab) {
        if ( Objects.equals( tab.getPosition(), 1 ) ) {
          WebView webView = (WebView) findViewById(R.id.web_infocard);
          try {
            if ( CARD != null && CARD.length != 0 ){
              webView.loadData( new String(CARD, "UTF-8"), "text/html; charset=utf-8", "utf-8" );
            }
          } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
          }
        }
      }

      @Override
      public void onTabUnselected(TabLayout.Tab tab) {

      }

      @Override
      public void onTabReselected(TabLayout.Tab tab) {

      }
    });
  }

  public void createDecisionTableHeader(){
    TableRow header = new TableRow(this);

    TextView field_num = new TextView(this);
    field_num.setText(" № ");
    field_num.setTextColor( Color.BLACK );
    header.addView(field_num);

    TextView field_type = new TextView(this);
    field_type.setText(" Тип ");
    field_type.setTextColor( Color.BLACK );
    header.addView(field_type);

    TextView field_date = new TextView(this);
    field_date.setText(" Дата ");
    field_date.setTextColor( Color.BLACK );
    header.addView(field_date);

    TextView field_resolution = new TextView(this);
    field_resolution.setText(" Резолюция ");
    field_resolution.setTextColor( Color.BLACK );
    header.addView(field_resolution);

    TextView field_status = new TextView(this);
    field_status.setText(" Статус ");
    field_status.setTextColor( Color.BLACK );
    header.addView(field_status);

//    decision_table.addView(header);
  }



  private void massInsert() {
    try {
      jobManager.addJobInBackground( new MassInsertJob(10000) );
    } catch ( Exception e){
      Timber.tag(TAG + " massInsert error").v( e );
    }
  }



  public void count() {

    if ( subscriptions != null && subscriptions.hasSubscriptions() ){
      subscriptions.unsubscribe();
    }

    Observable<Integer> itemCount = db.createQuery(sapotero.rxtest.db.models.Auth.TABLE, sapotero.rxtest.db.models.Auth.COUNT_QUERY)
      .map(query -> {
        try (Cursor cursor = query.run()) {
          if ( !(cursor != null && cursor.moveToNext()) ) {
            Timber.tag(TAG + " total error").v("No rows");
            throw new AssertionError("No rows");
          }
          return cursor.getInt(0);
        }
      });

    subscriptions.add(
      itemCount
        .subscribeOn( Schedulers.newThread() )
        .sample(5, TimeUnit.SECONDS)
        .observeOn( AndroidSchedulers.mainThread() )
        .subscribe(title -> {
          Timber.tag( TAG + " total").v(String.valueOf(title));
          ActionItemBadge.update(this, button.findItem(22), MaterialDesignIconic.Icon.gmi_account, ActionItemBadge.BadgeStyles.DARK_GREY, title);
        })
    );


  }

  private void loadSettings() {
    Preference<String> username = settings.getString("login");
    LOGIN = username.get();

    Preference<String> password = settings.getString("password");
    PASSWORD = password.get();

    Preference<String> token = settings.getString("token");
    TOKEN = token.get();

    Preference<Integer> position = settings.getInteger("position");
    POSITION = position.get();

    Timber.tag(TAG).v("LOGIN: "+ LOGIN );
    Timber.tag(TAG).v("PASSWORD: "+ PASSWORD );
    Timber.tag(TAG).v("TOKEN: "+ TOKEN );
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.info, menu);

    menu
      .add(0, 0, 0, "Информационная карточка")
      .setIcon(android.R.drawable.ic_dialog_info)
      .setOnMenuItemClickListener(
        item -> {

//          ArrayList<ArrayList<String>> result = Auth.select("*", null, null, null, null);
//          Log.d( "_DATA", result.toString() );
          count();

          return true;
        })
      .setShowAsAction( MenuItem.SHOW_AS_ACTION_ALWAYS);

    menu
      .add(0, 0, 0, "Добавить")
      .setIcon(android.R.drawable.ic_input_add)
      .setOnMenuItemClickListener(
        item -> {

//          if ( Auth.hasUser( LOGIN ) ){
//            Auth.update( Auth.token, TOKEN, Auth.login, LOGIN );
//          } else {
//            Auth.insert(LOGIN, TOKEN, null, null);
//          };

          return true;
        })
      .setShowAsAction( MenuItem.SHOW_AS_ACTION_ALWAYS);

    menu
      .add(0, 0, 0, "Удалить")
      .setIcon(android.R.drawable.ic_delete)
      .setOnMenuItemClickListener(
        item -> {

//          Auth.deleteAll();

          return true;
        })
      .setShowAsAction( MenuItem.SHOW_AS_ACTION_ALWAYS);

    menu
      .add(0, 0, 0, "Insert")
      .setIcon(android.R.drawable.ic_menu_week)
      .setOnMenuItemClickListener(
        item -> {

          massInsert();
          return true;


        })
      .setShowAsAction( MenuItem.SHOW_AS_ACTION_ALWAYS);

    button = new ActionItemBadgeAdder()
      .act(this)
      .menu(menu)
      .title("TEST")
      .itemDetails(0, 22, 1)
      .showAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
      .add(
        new IconicsDrawable(this)
          .icon(MaterialDesignIconic.Icon.gmi_account)
          .color(Color.WHITE)
          .sizeDp(16),
        ActionItemBadge.BadgeStyles.RED,
        123
      );


    return true;
  }

  public void addRowToDecisionTable(Decision data){

    TableRow row = new TableRow(this);

    TextView field_num = new TextView(this);
    field_num.setText( data.getId() );
    row.addView(field_num);

    TextView field_type = new TextView(this);
    field_type.setText( data.getSigner() );
    row.addView(field_type);

    TextView field_date = new TextView(this);
    field_date.setText( data.getDate() );
    row.addView(field_date);

    TextView field_resolution = new TextView(this);
    field_resolution.setText( data.getLetterhead() );
    row.addView(field_resolution);

    TextView field_status = new TextView(this);
    field_status.setText(data.getApproved() ? "Утверждена" : "Не утверждена" );
    row.addView(field_status);

//    decision_table.addView(row);

  }

  @Override
  public void onStart() {
    super.onStart();

    if ( !EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().register(this);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if ( EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().unregister(this);
    }

    if ( subscriptions != null && subscriptions.hasSubscriptions() ){
      subscriptions.unsubscribe();
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(MassInsertDoneEvent event) {
    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(SetActiveDecisonEvent event) {

    desigion_view.removeAllViews();
    Decision decision = decision_adapter.getItem(event.decision);

    if( decision.getLetterhead() != null ) {
      printLetterHead(decision.getLetterhead());
    }
    if( decision.getUrgencyText() != null ){
      printUrgency( decision.getUrgencyText().toString() );
    }

    if( decision.getBlocks().size() > 0 ){
      List<Block> blocks = decision.getBlocks();
      for (Block block: blocks){
        Timber.tag("block").v( block.getText() );
        printAppealText( block );

        if ( block.getTextBefore() ){
          printBlockText( block.getText() );
          printBlockPerformers( block.getPerformers(), block.getToFamiliarization(), block.getNumber() );
        } else {
          printBlockPerformers( block.getPerformers(), block.getToFamiliarization(), block.getNumber() );
          printBlockText( block.getText() );
        }
      }
    }

    printSigner( decision.getShowPosition(), decision.getSignerBlankText(), decision.getSignerPositionS(), decision.getDate(), DOCUMENT.getRegistrationNumber()  );
  }

  private void printSigner(Boolean showPosition, String signerBlankText, String signerPositionS, String date, String registrationNumber) {

    LinearLayout relativeSigner = new LinearLayout(this);
    relativeSigner.setOrientation(LinearLayout.VERTICAL);
    relativeSigner.setVerticalGravity( Gravity.BOTTOM );
    LinearLayout.LayoutParams relativeSigner_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    relativeSigner_params.height = LinearLayout.LayoutParams.MATCH_PARENT;
    relativeSigner.setLayoutParams( relativeSigner_params );




    LinearLayout signer_view = new LinearLayout(this);
    signer_view.setOrientation(LinearLayout.VERTICAL);
    signer_view.setPadding(0,40,0,0);

    if ( showPosition ){
      TextView signerPositionView = new TextView(this);
      signerPositionView.setText( signerPositionS );
      signerPositionView.setTextColor( Color.BLACK );
      signerPositionView.setGravity( Gravity.END );
      signer_view.addView( signerPositionView );
    }
    TextView signerBlankTextView = new TextView(this);
    signerBlankTextView.setText( signerBlankText );
    signerBlankTextView.setTextColor( Color.BLACK );
    signerBlankTextView.setGravity( Gravity.END);
    signer_view.addView( signerBlankTextView );





    LinearLayout date_and_number_view = new LinearLayout(this);
    date_and_number_view.setOrientation(LinearLayout.HORIZONTAL);

    TextView numberView = new TextView(this);
    numberView.setText( "№ " + registrationNumber );
    numberView.setTextColor( Color.BLACK );
    LinearLayout.LayoutParams numberViewParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
    numberView.setLayoutParams(numberViewParams);
    date_and_number_view.addView(numberView);

    TextView dateView = new TextView(this);
    dateView.setText( date );
    dateView.setGravity( Gravity.END );
    dateView.setTextColor( Color.BLACK );
    RelativeLayout.LayoutParams dateView_params = new RelativeLayout.LayoutParams(
      RelativeLayout.LayoutParams.MATCH_PARENT,
      RelativeLayout.LayoutParams.WRAP_CONTENT);
    dateView_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    dateView.setLayoutParams(dateView_params);
    LinearLayout.LayoutParams dateView_params1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
    dateView.setLayoutParams(dateView_params1);
    date_and_number_view.addView(dateView);

    relativeSigner.addView( signer_view );
    relativeSigner.addView( date_and_number_view );


    desigion_view.addView( relativeSigner );
  }

  private void printUrgency(String urgency) {
    TextView urgencyView = new TextView(this);
    urgencyView.setGravity(Gravity.RIGHT);
    urgencyView.setAllCaps(true);
    urgencyView.setPaintFlags( Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG );
    urgencyView.setText( urgency );
    urgencyView.setTextColor( ContextCompat.getColor(context, R.color.md_black_1000) );

    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    params.setMargins(0,0,0,10);
    urgencyView.setLayoutParams(params);

    desigion_view.addView( urgencyView );
  }

  private void printLetterHead(String letterhead) {
    TextView letterHead = new TextView(this);
    letterHead.setGravity(Gravity.CENTER);
    letterHead.setText( letterhead );
    letterHead.setTextColor( Color.BLACK );
    desigion_view.addView( letterHead );

    TextView delimiter = new TextView(this);
    delimiter.setGravity(Gravity.CENTER);
    delimiter.setHeight(1);
    delimiter.setWidth(400);
    delimiter.setBackgroundColor( ContextCompat.getColor(context, R.color.md_blue_grey_200) );

    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    params.setMargins(50, 10, 50, 10);
    delimiter.setLayoutParams(params);

    desigion_view.addView( delimiter );
  }

  private void printBlockText(String text) {
    TextView block_view = new TextView(this);
    block_view.setText( text );
    block_view.setTextColor( Color.BLACK );

    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 10, 0, 10);
    block_view.setLayoutParams(params);

    desigion_view.addView( block_view );
  }

  private void printAppealText( Block block ) {

    String text = "";
    String appealText;
    String number;
    boolean toFamiliarization = block.getToFamiliarization();

    if ( block.getAppealText() != null ){
      appealText = block.getAppealText().toString();
    } else {
      appealText = "";
    }

    if ( block.getNumber() != null ){
      number = block.getNumber().toString();
    } else {
      number = "1";
    }



    if (toFamiliarization){
      text += number + ". ";
      block.setToFamiliarization(false);
    }
    text += appealText;

    TextView blockAppealView = new TextView(this);
    blockAppealView.setGravity(Gravity.CENTER);
    blockAppealView.setText( text );
    blockAppealView.setTextColor( Color.BLACK );
    blockAppealView.setTextSize( TypedValue.COMPLEX_UNIT_SP, 12 );

    desigion_view.addView( blockAppealView );
  }

  private void printBlockPerformers(List<Performer> performers, Boolean toFamiliarization, Integer number) {

    boolean numberPrinted = false;
    LinearLayout users_view = new LinearLayout(this);
    users_view.setOrientation(LinearLayout.VERTICAL);
    users_view.setPadding(40,5,5,5);

    if( performers.size() > 0 ){
      List<Performer> users = performers;
      for (Performer user: users){

        String performerName = "";

        if (toFamiliarization && !numberPrinted){
          performerName += number.toString() + ". ";
          numberPrinted = true;
        } else {
          performerName += user.getPerformerText();
        }

        TextView performer_view = new TextView(this);
        performer_view.setText( performerName );
        performer_view.setTextColor( Color.BLACK );
        users_view.addView(performer_view);
      }
    }


    desigion_view.addView( users_view );
  }


  @Override
  public void onFragmentInteraction(Uri uri) {
  }
}

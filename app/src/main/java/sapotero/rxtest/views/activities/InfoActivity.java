package sapotero.rxtest.views.activities;

import android.content.Context;
import android.content.Intent;
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
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.query.Tuple;
import io.requery.rx.SingleEntityStore;
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
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RBlock;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecision;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformer;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.events.bus.MassInsertDoneEvent;
import sapotero.rxtest.events.bus.SetActiveDecisonEvent;
import sapotero.rxtest.jobs.rx.SetActiveDecisionJob;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.views.adapters.DecisionAdapter;
import sapotero.rxtest.views.adapters.TabPagerAdapter;
import sapotero.rxtest.views.fragments.InfoCardDocumentsFragment;
import sapotero.rxtest.views.fragments.InfoCardWebViewFragment;
import sapotero.rxtest.views.interfaces.DocumentManager;
import sapotero.rxtest.views.managers.menu.OperationManager;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class InfoActivity extends AppCompatActivity implements InfoCardDocumentsFragment.OnFragmentInteractionListener, InfoCardWebViewFragment.OnFragmentInteractionListener, DocumentManager.Callback, OperationManager.Callback {

  @BindView(R.id.desigions_recycler_view) RecyclerView desigions_recycler_view;
  @BindView(R.id.desigion_view)           LinearLayout desigion_view;

  @BindView(R.id.tab_main) ViewPager viewPager;
  @BindView(R.id.tabs) TabLayout tabLayout;


  @Inject JobManager jobManager;
  @Inject CompositeSubscription subscriptions;
  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  private byte[] CARD;

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> PASSWORD;
  private Preference<String> UID;
  private Preference<String> DOCUMENT_UID;
  private Preference<String> STATUS_CODE;
  private Preference<Integer> POSITION;

  private DocumentManager documentManager;
  private DocumentInfo DOCUMENT;

  private String TAG = this.getClass().getSimpleName();
  private Context context;
  private DecisionAdapter decision_adapter;
  @BindView(R.id.toolbar) Toolbar toolbar;

  private Preference<String> HOST;
  private Preview preview;
  private OperationManager operationManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    // меняем загрузочную тему
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    context = this;

    preview = new Preview(this);

    EsdApplication.getComponent(this).inject(this);

    setContentView(R.layout.activity_info);
    ButterKnife.bind(this);

    documentManager = new DocumentManager(this);
    documentManager.registerCallBack(this);

    operationManager = new OperationManager(this);
    operationManager.registerCallBack(this);

    Timber.w("documentManager:\nUID: %s\nstate: %s\ntype: %s\n\n",
      documentManager.getCurrentDocumentNumber(),
      documentManager.getState(),
      documentManager.getType()
    );

    loadSettings();

    setToolbar();
    loadDocuments();
  }

  private void setToolbar() {
    toolbar.setTitle("Все документы");
    toolbar.setTitleTextColor( getResources().getColor( R.color.md_grey_100 ) );
    toolbar.setSubtitleTextColor( getResources().getColor( R.color.md_grey_400 ) );

    toolbar.setContentInsetStartWithNavigation(250);

    toolbar.setNavigationOnClickListener(v ->{
      finish();
      }
    );


    // sent_to_the_report (отправлен на доклад)
    // sent_to_the_performance (отправлен на исполнение)
    // primary_consideration (первичное рассмотрение)
    // approval (согласование проектов документов)
    // signing (подписание проектов документов)

    int menu;

    switch ( STATUS_CODE.get() ){
      case "sent_to_the_report":
        menu = R.menu.info_menu_sent_to_the_report;
        break;
      case "sent_to_the_performance":
        menu = R.menu.info_menu_sent_to_the_performance;
        break;
      case "primary_consideration":
        menu = R.menu.info_menu_primary_consideration;
        break;
      case "approval":
        menu = R.menu.info_menu_approval;
        break;
      case "signing":
        menu = R.menu.info_menu_signing;
        break;
      default:
        menu = R.menu.info_menu;
        break;
    }
    toolbar.inflateMenu(menu);


    toolbar.setOnMenuItemClickListener(
      item -> {

        String operation;
        CommandParams params = new CommandParams();

        switch ( item.getItemId() ){
          // sent_to_the_report (отправлен на доклад)
          case R.id.menu_info_from_the_report:
            operation = "menu_info_from_the_report";
            break;
          case R.id.return_to_the_primary_consideration:
            operation = "return_to_the_primary_consideration";
            break;

          // sent_to_the_report (отправлен на доклад)
          case R.id.menu_info_delegate_performance:
            operation = "menu_info_delegate_performance";
            params.setPerson( "USER_UD" );
            break;
          case R.id.menu_info_to_the_approval_performance:
            operation = "menu_info_to_the_approval_performance";
            params.setPerson( "USER_UD" );
            break;

          // primary_consideration (первичное рассмотрение)
          case R.id.menu_info_to_the_primary_consideration:
            operation = "menu_info_to_the_primary_consideration";
            params.setPerson( "USER_UD" );
            break;

          // approval (согласование проектов документов)
          case R.id.menu_info_approval_change_person:
            operation = "menu_info_change_person";
            params.setPerson( "USER_UD" );
            break;
          case R.id.menu_info_approval_next_person:
            operation = "menu_info_next_person";
            params.setSign( "SIGN" );
            break;
          case R.id.menu_info_approval_prev_person:
            operation = "menu_info_prev_person";
            params.setSign( "SIGN" );
            break;

          // approval (согласование проектов документов)
          case R.id.menu_info_sign_change_person:
            operation = "menu_info_change_person";
            params.setPerson( "USER_UD" );
            break;
          case R.id.menu_info_sign_next_person:
            operation = "menu_info_next_person";
            params.setSign( "SIGN" );
            break;
          case R.id.menu_info_sign_prev_person:
            operation = "menu_info_prev_person";
            params.setSign( "SIGN" );
            break;



          case R.id.action_info_create_to_control:
            operation = "action_info_create_to_control";
            break;
          case R.id.action_info_create_decision:
            operation = "action_info_create_decision";

            Intent intent = new Intent(this, DecisionConstructorActivity.class);
            startActivity(intent);

            break;
          default:
            operation = "incorrect";
            break;
        }

        operationManager.execute( operation, params );

        Timber.tag(TAG).i( "operation: %s", operation );
        return false;
      }
    );
  }

  private Boolean documentExist(String uid){
    Integer count = dataStore
      .count( RDocumentEntity.class )
      .where( RDocumentEntity.UID.eq(uid) )
      .and( RDocumentEntity.INFO_CARD.ne("") )
      .get()
      .value();

    Boolean result = count > 0;
    Timber.i( "documentExist %s", result );

    return result;
  }

  private void loadDocuments() {

    setTabContent();

    if ( documentExist( UID.get() ) ){
      loadFromDb();
    } else {
      loadFromJson();
    }

  }

  private void loadFromDb() {
    Timber.tag("loadFromDb").v("start");

    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq( UID.get() ))
      .get()
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(doc -> {

        Timber.tag("loadFromDb").i( "loaded %s", doc.getId() );

        preview.showEmpty();

        toolbar.setTitle( doc.getTitle() );
        toolbar.setContentInsetStartWithNavigation(250);

        if ( doc.getDecisions().size() >= 1 ){

          desigions_recycler_view.setLayoutManager(new LinearLayoutManager(this));

          List<Decision> decisions_list = new ArrayList<>();



          for (RDecision rDecision: doc.getDecisions()) {
            Decision raw_decision = new Decision();

            RDecisionEntity decision = (RDecisionEntity) rDecision;

            raw_decision.setId( String.valueOf(decision.getUid()) );
            raw_decision.setLetterhead(decision.getLetterhead());
            raw_decision.setSigner(decision.getSigner());
            raw_decision.setSignerId(decision.getSignerId());
            raw_decision.setAssistantId(decision.getAssistantId());
            raw_decision.setSignerBlankText(decision.getSignerBlankText());
            raw_decision.setComment(decision.getComment());
            raw_decision.setDate(decision.getDate());
            raw_decision.setApproved(decision.isApproved());
            raw_decision.setUrgencyText(decision.getUrgencyText());
            raw_decision.setSignerIsManager(decision.isSignerIsManager());
            raw_decision.setShowPosition(decision.isShowPosition());

            for (RBlock rBlock: decision.getBlocks()) {
              RBlockEntity block = (RBlockEntity) rBlock;
              Block raw_block = new Block();

              raw_block.setNumber(block.getNumber());
              raw_block.setText(block.getText());
              raw_block.setAppealText(block.getAppealText());
              raw_block.setTextBefore(block.isTextBefore());
              raw_block.setHidePerformers(block.isHidePerformers());
              raw_block.setToCopy(block.isToCopy());
              raw_block.setToFamiliarization(block.isToFamiliarization());

              for (RPerformer rPerformer: block.getPerformers()) {
                RPerformerEntity performer = (RPerformerEntity) rPerformer;
                Performer raw_performer = new Performer();

                raw_performer.setNumber(performer.getNumber());
                raw_performer.setPerformerId(performer.getPerformerId());
                raw_performer.setPerformerType(performer.getPerformerType());
                raw_performer.setPerformerText(performer.getPerformerText());
                raw_performer.setOrganizationText(performer.getOrganizationText());
                raw_performer.setIsOriginal(performer.isIsOriginal());
                raw_performer.setIsResponsible(performer.isIsResponsible());

                raw_block.getPerformers().add(raw_performer);
              }

              raw_decision.getBlocks().add(raw_block);
            }

            decisions_list.add(raw_decision);
          }

          decision_adapter = new DecisionAdapter(decisions_list, this, desigions_recycler_view);
          desigions_recycler_view.setAdapter(decision_adapter);

          // если есть резолюции, то отобразить первую
          if ( decision_adapter.getItemCount() > 0 ) {
//            jobManager.addJobInBackground( new SetActiveDecisionJob(0) );
            Timber.w("decisions_list.size() > 0");
            preview.show( decision_adapter.getItem(0) );
          } else {
            Timber.w("decisions_list.size() < 0");
            preview.showEmpty();
          }

          desigions_recycler_view.setLayoutManager(new LinearLayoutManager(this));

          RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
          itemAnimator.setAddDuration(10);
          itemAnimator.setRemoveDuration(10);
          desigions_recycler_view.setItemAnimator(itemAnimator);

        }

        if ( doc.getInfoCard() != null ){
          CARD = Base64.decode( doc.getInfoCard().getBytes(), Base64.DEFAULT );
        } else {
          CARD = Base64.decode( "".getBytes(), Base64.DEFAULT );
        }

        Preference<String> infocard = settings.getString("document.infoCard");
        infocard.set( new String(CARD , StandardCharsets.UTF_8) );

      });
  }

  private void loadFromJson(){
    HOST = settings.getString("settings_username_host");

    Retrofit retrofit = new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl(HOST.get() + "v3/documents/")
      .client(okHttpClient)
      .build();

    DocumentService documentService = retrofit.create( DocumentService.class );

    Observable<DocumentInfo> info = documentService.getInfo(
      UID.get(),
      LOGIN.get(),
      TOKEN.get()
    );

    info.subscribeOn( Schedulers.newThread() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {
          DOCUMENT = data;

          Gson gson = new Gson();

          Preference<String> documentNumber = settings.getString("document.number");
          documentNumber.set( DOCUMENT.getRegistrationNumber() );

          Preference<String> documentJson = settings.getString("document.json");
          documentJson.set( gson.toJson(DOCUMENT) );

          Preference<String> documentImages = settings.getString("document.images");
          documentImages.set( gson.toJson( DOCUMENT.getImages() ) );

          toolbar.setTitle( data.getTitle() );

          if ( data.getDecisions().size() >= 1 ){

            desigions_recycler_view.setLayoutManager(new LinearLayoutManager(this));

            List<Decision> decisions_list = new ArrayList<>();
            for (Decision decision: data.getDecisions()) {
              decisions_list.add(decision);
            }

            decision_adapter = new DecisionAdapter(decisions_list, this, desigions_recycler_view);
            desigions_recycler_view.setAdapter(decision_adapter);

            // если есть резолюции, то отобразить первую
            if ( decisions_list.size() > 0 ) {
              try {
                jobManager.addJobInBackground( new SetActiveDecisionJob(0) );
              } catch ( Exception e){
                Timber.tag(TAG + " massInsert error").v( e );
              }
            }

            desigions_recycler_view.setLayoutManager(new LinearLayoutManager(this));

            RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
            itemAnimator.setAddDuration(10);
            itemAnimator.setRemoveDuration(10);
            desigions_recycler_view.setItemAnimator(itemAnimator);

          }

          if ( data.getInfoCard() != null ){
            CARD = Base64.decode( data.getInfoCard().getBytes(), Base64.DEFAULT );
          } else {
            CARD = Base64.decode( "".getBytes(), Base64.DEFAULT );
          }
          Preference<String> infocard = settings.getString("document.infoCard");
          infocard.set( new String(CARD , StandardCharsets.UTF_8) );

        },
        error -> {
          Log.d( "++_ERROR", error.getMessage() );
          Toast.makeText( this, error.getMessage(), Toast.LENGTH_SHORT).show();
        });
  }

  private void setTabContent() {

    tabLayout.addTab(tabLayout.newTab().setText("Документ"));
    tabLayout.addTab(tabLayout.newTab().setText("Инфокарточка"));
    tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

    final TabPagerAdapter adapter = new TabPagerAdapter (getSupportFragmentManager(), tabLayout.getTabCount());
    viewPager.setAdapter(adapter);
    tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override
      public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
      }

      @Override
      public void onTabUnselected(TabLayout.Tab tab) {
      }

      @Override
      public void onTabReselected(TabLayout.Tab tab) {
      }
    });
  }


  private void loadSettings() {
    LOGIN    = settings.getString("login");
    UID      = settings.getString("info.uid");
    PASSWORD = settings.getString("password");
    TOKEN    = settings.getString("token");
    POSITION = settings.getInteger("position");
    DOCUMENT_UID = settings.getString("document.uid");
    STATUS_CODE = settings.getString("info.status");

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    Timber.tag(TAG).i(String.valueOf(menu));
    return false;
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

    finish();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(MassInsertDoneEvent event) {
    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(SetActiveDecisonEvent event) {
    Decision decision = decision_adapter.getItem(event.decision);
    preview.show( decision );
  }

  @Override
  public void onFragmentInteraction(Uri uri) {
  }

  @Override
  protected void onResume() {
    super.onResume();
    loadFromDb();
  }



  /* DocumentManager.Callback */
  @Override
  public void onGetStateSuccess() {
    Timber.tag("DocumentManagerCallback").i("onGetStateSuccess");
  }

  @Override
  public void onGetStateError() {
    Timber.tag("DocumentManagerCallback").i("onGetStateError");
  }



  /* OperationManager.Callback */
  @Override
  public void onExecuteSuccess() {
    Timber.tag("OpManagerCallback").i("onExecuteSuccess");
  }

  @Override
  public void onExecuteError() {
    Timber.tag("OpManagerCallback").i("onExecuteSuccess");
  }

  class Preview{

    private final Context context;
    private String TAG = this.getClass().getSimpleName();

    public Preview(InfoActivity infoActivity) {
      this.context = infoActivity;
    }

    private void clear(){
      desigion_view.removeAllViews();
    };

    private void show( Decision decision ){
      clear();

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

          Boolean f = block.getToFamiliarization();
          if (f == null)
            f = false;

          if ( block.getTextBefore() ){
            printBlockText( block.getText() );
            if (!block.getHidePerformers())
              printBlockPerformers( block.getPerformers(), f, block.getNumber() );

          } else {
            if (!block.getHidePerformers())
              printBlockPerformers( block.getPerformers(), f, block.getNumber() );
            printBlockText( block.getText() );
          }
        }
      }

      Tuple doc = dataStore
        .select(RDocumentEntity.REGISTRATION_NUMBER)
        .where(RDocumentEntity.UID.eq(UID.get()))
        .and(RDocumentEntity.INFO_CARD.ne(""))
        .get().first();

      printSigner( decision.getShowPosition(), decision.getSignerBlankText(), decision.getSignerPositionS(), decision.getDate(), doc.get(0)  );
    }

    private void showEmpty(){
      Timber.tag(TAG).d( "showEmpty" );

      clear();
      printLetterHead( getString(R.string.decision_blank) );
    }

    private void printSigner(Boolean showPosition, String signerBlankText, String signerPositionS, String date, String registrationNumber) {

      LinearLayout relativeSigner = new LinearLayout(context);
      relativeSigner.setOrientation(LinearLayout.VERTICAL);
      relativeSigner.setVerticalGravity( Gravity.BOTTOM );
      relativeSigner.setMinimumHeight(350);
      LinearLayout.LayoutParams relativeSigner_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      relativeSigner_params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
      relativeSigner.setLayoutParams( relativeSigner_params );




      LinearLayout signer_view = new LinearLayout(context);
      signer_view.setOrientation(LinearLayout.VERTICAL);
      signer_view.setPadding(0,40,0,0);

      if ( showPosition ){
        TextView signerPositionView = new TextView(context);
        signerPositionView.setText( signerPositionS );
        signerPositionView.setTextColor( Color.BLACK );
        signerPositionView.setGravity( Gravity.END );
        signer_view.addView( signerPositionView );
      }
      TextView signerBlankTextView = new TextView(context);
      signerBlankTextView.setText( signerBlankText );
      signerBlankTextView.setTextColor( Color.BLACK );
      signerBlankTextView.setGravity( Gravity.END);
      signer_view.addView( signerBlankTextView );





      LinearLayout date_and_number_view = new LinearLayout(context);
      date_and_number_view.setOrientation(LinearLayout.HORIZONTAL);

      TextView numberView = new TextView(context);
      numberView.setText( "№ " + registrationNumber );
      numberView.setTextColor( Color.BLACK );
      LinearLayout.LayoutParams numberViewParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
      numberView.setLayoutParams(numberViewParams);
      date_and_number_view.addView(numberView);

      TextView dateView = new TextView(context);
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
      TextView urgencyView = new TextView(context);
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
      TextView letterHead = new TextView(context);
      letterHead.setGravity(Gravity.CENTER);
      letterHead.setText( letterhead );
      letterHead.setTextColor( Color.BLACK );
      desigion_view.addView( letterHead );

      TextView delimiter = new TextView(context);
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
      TextView block_view = new TextView(context);
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
      boolean toFamiliarization = block.getToFamiliarization() == null ? false : block.getToFamiliarization();

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

      TextView blockAppealView = new TextView(context);
      blockAppealView.setGravity(Gravity.CENTER);
      blockAppealView.setText( text );
      blockAppealView.setTextColor( Color.BLACK );
      blockAppealView.setTextSize( TypedValue.COMPLEX_UNIT_SP, 12 );

      desigion_view.addView( blockAppealView );
    }

    private void printBlockPerformers(List<Performer> performers, Boolean toFamiliarization, Integer number) {

      boolean numberPrinted = false;
      LinearLayout users_view = new LinearLayout(context);
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

          TextView performer_view = new TextView(context);
          performer_view.setText( performerName );
          performer_view.setTextColor( Color.BLACK );
          users_view.addView(performer_view);
        }
      }


      desigion_view.addView( users_view );
    }


  }
}

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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.control_labels.RControlLabelsEntity;
import sapotero.rxtest.db.requery.models.decisions.RBlock;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecision;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformer;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.db.requery.models.exemplars.RExemplarEntity;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.events.bus.MassInsertDoneEvent;
import sapotero.rxtest.events.bus.SetActiveDecisonEvent;
import sapotero.rxtest.jobs.rx.SetActiveDecisionJob;
import sapotero.rxtest.retrofit.DocumentService;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.ControlLabel;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.DocumentInfo;
import sapotero.rxtest.retrofit.models.document.Exemplar;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.views.adapters.DecisionAdapter;
import sapotero.rxtest.views.adapters.TabPagerAdapter;
import sapotero.rxtest.views.fragments.InfoCardDocumentsFragment;
import sapotero.rxtest.views.fragments.InfoCardWebViewFragment;
import timber.log.Timber;

public class InfoActivity extends AppCompatActivity implements InfoCardDocumentsFragment.OnFragmentInteractionListener, InfoCardWebViewFragment.OnFragmentInteractionListener {

  @BindView(R.id.desigions_recycler_view) RecyclerView desigions_recycler_view;
  @BindView(R.id.desigion_view)           LinearLayout desigion_view;

  @BindView(R.id.loader) View loader;

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
  private Preference<Integer> POSITION;

  private DocumentInfo DOCUMENT;

  private String TAG = InfoActivity.class.getSimpleName();
  private Context context;

  private DecisionAdapter decision_adapter;
  @BindView(R.id.toolbar) Toolbar toolbar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    // меняем загрузочную тему
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    context = this;

    EsdApplication.getComponent(this).inject(this);

    setContentView(R.layout.activity_info);
    ButterKnife.bind(this);


    toolbar.setTitle("Все документы");
    toolbar.setTitleTextColor( getResources().getColor( R.color.md_grey_100 ) );
    toolbar.setSubtitleTextColor( getResources().getColor( R.color.md_grey_400 ) );

    toolbar.setContentInsetStartWithNavigation(250);

    toolbar.inflateMenu(R.menu.info_menu);
    toolbar.setNavigationOnClickListener(v ->{
        finish();
      }
    );

    toolbar.setOnMenuItemClickListener(
      item -> {

        String operation;

        switch ( item.getItemId() ){
          case R.id.action_info_create_to_control:
            operation = "action_info_create_to_control";
            break;
          case R.id.action_info_create_to_favorites:
            operation = "action_info_create_to_favorites";
            break;
          case R.id.action_info_create_no_answer:
            operation = "action_info_create_no_answer";
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

        Timber.tag(TAG).i( operation );
        return false;
      }
    );

    loadSettings();
    loadDocuments();
  }

  private Boolean documentExist(String uid){
    Integer count = dataStore
      .count( RDocumentEntity.class )
      .where( RDocumentEntity.UID.eq(uid) )
      .and( RDocumentEntity.INFO_CARD.ne("") )
      .get()
      .value();

    Boolean result = count > 0;
    Timber.i( "documentExist " + result.toString() );

    return result;
  }

  private void loadDocuments() {

    if ( documentExist( UID.get() ) ){
      loadFromDb();
    } else {
      loadFromJson();
    }

  }

  private void loadFromDb() {
    Timber.tag("loadFromDb").v("start");
    loader.setVisibility(ProgressBar.INVISIBLE);

    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq( UID.get() ))
      .get()
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(doc -> {

        setTabContent();

        toolbar.setTitle( doc.getTitle() );
        toolbar.setContentInsetStartWithNavigation(250);

        if ( doc.getDecisions().size() >= 1 ){

          desigions_recycler_view.setLayoutManager(new LinearLayoutManager(this));

          List<Decision> decisions_list = new ArrayList<>();



          for (RDecision rDecision: doc.getDecisions()) {
            Decision raw_decision = new Decision();

            RDecisionEntity decision = (RDecisionEntity) rDecision;

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

        if ( doc.getInfoCard() != null ){
          CARD = Base64.decode( doc.getInfoCard().getBytes(), Base64.DEFAULT );
        } else {
          CARD = Base64.decode( "".getBytes(), Base64.DEFAULT );
        }

        Preference<String> infocard = settings.getString("document.infoCard");
        infocard.set( new String(CARD , StandardCharsets.UTF_8) );

      });
  }

  private void saveToDb(DocumentInfo document){
    String tag = "saveToDb";

    Timber.tag(tag).v("document " + document.getUid());

    RDocumentEntity rDoc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(UID.get()))
      .get()
      .first();

    if ( document.getDecisions() != null && document.getDecisions().size() >= 1 ){
      for (Decision d: document.getDecisions() ) {

        RDecisionEntity decision = new RDecisionEntity();
        decision.setLetterhead(d.getLetterhead());
        decision.setApproved(d.getApproved());
        decision.setSigner(d.getSigner());
        decision.setSignerId(d.getSignerId());
        decision.setAssistantId(d.getAssistantId());
        decision.setSignerBlankText(d.getSignerBlankText());
        decision.setSignerIsManager(d.getSignerIsManager());
        decision.setComment(d.getComment());
        decision.setDate(d.getDate());
        decision.setUrgencyText(d.getUrgencyText());
        decision.setShowPosition(d.getShowPosition());

        if ( d.getBlocks() != null && d.getBlocks().size() >= 1 ){

          for (Block b: d.getBlocks() ) {
            RBlockEntity block = new RBlockEntity();
            block.setNumber(b.getNumber());
            block.setText(b.getText());
            block.setAppealText(b.getAppealText());
            block.setTextBefore(b.getTextBefore());
            block.setHidePerformers(b.getHidePerformers());
            block.setToCopy(b.getToCopy());
            block.setToFamiliarization(b.getToFamiliarization());

            if ( b.getPerformers() != null && b.getPerformers().size() >= 1 ) {

              for (Performer p : b.getPerformers()) {
                RPerformerEntity performer = new RPerformerEntity();

                performer.setNumber(p.getNumber());
                performer.setPerformerId(p.getPerformerId());
                performer.setPerformerType(p.getPerformerType());
                performer.setPerformerText(p.getPerformerText());
                performer.setOrganizationText(p.getOrganizationText());
                performer.setIsOriginal(p.getIsOriginal());
                performer.setIsResponsible(p.getIsResponsible());

                performer.setBlock(block);
                block.getPerformers().add(performer);
              }
            }


            block.setDecision(decision);
            decision.getBlocks().add(block);
          }

        }


        decision.setDocument(rDoc);
        rDoc.getDecisions().add(decision);
      }
    }

    if ( document.getExemplars() != null && document.getExemplars().size() >= 1 ){
      for (Exemplar e: document.getExemplars() ) {
        RExemplarEntity exemplar = new RExemplarEntity();
        exemplar.setNumber(String.valueOf(e.getNumber()));
        exemplar.setIsOriginal(e.getIsOriginal());
        exemplar.setStatusCode(e.getStatusCode());
        exemplar.setAddressedToId(e.getAddressedToId());
        exemplar.setAddressedToName(e.getAddressedToName());
        exemplar.setDate(e.getDate());
        exemplar.setDocument(rDoc);
        rDoc.getExemplars().add(exemplar);
      }
    }

    if ( document.getImages() != null && document.getImages().size() >= 1 ){
      for (Image i: document.getImages() ) {
        RImageEntity image = new RImageEntity();
        image.setTitle(i.getTitle());
        image.setNumber(i.getNumber());
        image.setMd5(i.getMd5());
        image.setSize(i.getSize());
        image.setPath(i.getPath());
        image.setContentType(i.getContentType());
        image.setSigned(i.getSigned());
        image.setDocument(rDoc);
        rDoc.getImages().add(image);
      }
    }

    if ( document.getControlLabels() != null && document.getControlLabels().size() >= 1 ){
      for (ControlLabel l: document.getControlLabels() ) {
        RControlLabelsEntity label = new RControlLabelsEntity();
        label.setCreatedAt(l.getCreatedAt());
        label.setOfficialId(l.getOfficialId());
        label.setOfficialName(l.getOfficialName());
        label.setSkippedOfficialId(l.getSkippedOfficialId());
        label.setSkippedOfficialName(l.getSkippedOfficialName());
        label.setState(l.getState());
        label.setDocument(rDoc);
        rDoc.getControlLabels().add(label);
      }
    }

    if ( document.getInfoCard() != null){
      rDoc.setInfoCard( document.getInfoCard() );
    }


    dataStore.update(rDoc)
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        result -> {
          Timber.tag(tag).d("updated " + result.getUid());
        },
        error ->{
          error.printStackTrace();
        }
      );


  }

  private void loadFromJson(){
    Retrofit retrofit = new Retrofit.Builder()
      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .baseUrl(Constant.HOST + "v3/documents/")
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

          saveToDb(DOCUMENT);

          Gson gson = new Gson();

          Preference<String> documentNumber = settings.getString("document.number");
          documentNumber.set( DOCUMENT.getRegistrationNumber() );

          Preference<String> documentJson = settings.getString("document.json");
          documentJson.set( gson.toJson(DOCUMENT) );

          Preference<String> documentImages = settings.getString("document.images");
          documentImages.set( gson.toJson( DOCUMENT.getImages() ) );

          setTabContent();

          loader.setVisibility(ProgressBar.INVISIBLE);

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
          loader.setVisibility(ProgressBar.INVISIBLE);
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
    viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
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

        Boolean f = block.getToFamiliarization();
        if (f == null)
            f = false;

        if ( block.getTextBefore() ){
          printBlockText( block.getText() );
          printBlockPerformers( block.getPerformers(), f, block.getNumber() );
        } else {
          printBlockPerformers( block.getPerformers(), f, block.getNumber() );
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
    relativeSigner.setMinimumHeight(350);
    LinearLayout.LayoutParams relativeSigner_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    relativeSigner_params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
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

package sapotero.rxtest.views.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.decisions.RBlock;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecision;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformer;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.events.decision.HasNoActiveDecisionConstructor;
import sapotero.rxtest.views.activities.DecisionConstructorActivity;
import sapotero.rxtest.views.adapters.DecisionSpinnerAdapter;
import sapotero.rxtest.views.adapters.models.DecisionSpinnerItem;
import sapotero.rxtest.views.dialogs.DecisionMagniferFragment;
import sapotero.rxtest.views.managers.menu.OperationManager;
import sapotero.rxtest.views.managers.menu.factories.CommandFactory;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;
import sapotero.rxtest.views.managers.toolbar.ToolbarManager;
import timber.log.Timber;


@SuppressLint("ValidFragment")
public class InfoActivityDecisionPreviewFragment extends Fragment implements OperationManager.Callback {

  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject OperationManager operationManager;

  private ToolbarManager toolbarManager;
  private OnFragmentInteractionListener mListener;

  private Preference<String> DOCUMENT_UID;
  private Preference<String> UID;
  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> PASSWORD;
  private Preference<String> STATUS_CODE;
  private Preference<Integer> POSITION;


  @BindView(R.id.activity_info_decision_preview_head) LinearLayout preview_head;

  @BindView(R.id.activity_info_decision_preview_body) LinearLayout preview_body;
  @BindView(R.id.activity_info_decision_preview_bottom) LinearLayout preview_bottom;

  @BindView(R.id.activity_info_button_magnifer) ImageButton magnifer_button;
  @BindView(R.id.activity_info_button_edit) ImageButton edit;

  @BindView(R.id.activity_info_decision_spinner) Spinner decision_spinner;
  @BindView(R.id.activity_info_decision_preview_toolbar) Toolbar decision_toolbar;

  @BindView(R.id.activity_info_decision_preview_next_person) Button next_person_button;
  @BindView(R.id.activity_info_decision_preview_prev_person) Button prev_person_button;
  @BindView(R.id.activity_info_decision_preview_approved_text) TextView approved_text;


  private ArrayList<DecisionSpinnerItem> decisionSpinnerItems  = new ArrayList<>();;

  private DecisionSpinnerAdapter decision_spinner_adapter;
  private Preview preview;

  private Unbinder binder;
  private Preference<String> REG_NUMBER;
  private String uid;
  private RDecisionEntity current_decision;
  private String TAG = this.getClass().getSimpleName();

  public InfoActivityDecisionPreviewFragment() {
  }


  public InfoActivityDecisionPreviewFragment(ToolbarManager toolbarManager) {
    this.toolbarManager = toolbarManager;
  }

  // Approve current decision
  @OnClick(R.id.activity_info_decision_preview_next_person)
  public void decision_preview_next(){

    CommandFactory.Operation operation;
    operation =CommandFactory.Operation.APPROVE_DECISION;

    CommandParams params = new CommandParams();
    params.setDecisionId( current_decision.getUid() );
    params.setDecision( current_decision );

    operationManager.execute(operation, params);
  }

  // Reject current decision
  @OnClick(R.id.activity_info_decision_preview_prev_person)
  public void decision_preview_prev(){

    CommandFactory.Operation operation;
    operation =CommandFactory.Operation.REJECT_DECISION;

    CommandParams params = new CommandParams();
    params.setDecisionId( current_decision.getUid() );
    params.setDecision( current_decision );

    operationManager.execute(operation, params);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_info_card_decision_preview, container, false);

    EsdApplication.getComponent( getContext() ).inject(this);
    binder = ButterKnife.bind(this, view);

    operationManager.registerCallBack(this);

    loadSettings();
    loadDocument();

    setAdapter();

    decision_toolbar.inflateMenu(R.menu.decision_preview_menu);
    decision_toolbar.setOnMenuItemClickListener(item -> {
      switch ( item.getItemId() ){
        case R.id.decision_preview_magnifer:
          magnifer();
          break;
        case R.id.decision_preview_edit:
          edit();
          break;
        default:
          break;
      }
      return false;
    });

    preview = new Preview(getContext());
    return view;
  }

  private void setAdapter() {
    decision_spinner_adapter = new DecisionSpinnerAdapter(getContext(), settings.getString("current_user_id").get() , decisionSpinnerItems);
    decision_spinner.setAdapter(decision_spinner_adapter);

    decision_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        if ( decision_spinner_adapter.getCount() > 0 ) {
          Timber.tag(TAG).e("onItemSelected %s %s ", position, id);
          current_decision = decision_spinner_adapter.getItem(position).getDecision();
          displayDecision();
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
        if ( decision_spinner_adapter.getCount() > 0 ){
          current_decision = decision_spinner_adapter.getItem(0).getDecision();
          Timber.tag(TAG).e("onNothingSelected");
          displayDecision();
        }
      }
    });
  }

  private void updateVisibility(Boolean approved) {
    next_person_button.setVisibility(approved ? View.GONE : View.VISIBLE);
    prev_person_button.setVisibility(approved ? View.GONE : View.VISIBLE);
    approved_text.setVisibility(!approved ? View.GONE : View.VISIBLE);

    if ( !decision_spinner_adapter.hasActiveDecision() ){
      Timber.tag(TAG).e("NO ACTIVE DECISION");
      EventBus.getDefault().post( new HasNoActiveDecisionConstructor() );
    }
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    binder.unbind();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public Fragment withUid(String uid) {
    this.uid = uid;
    return this;
  }

  public interface OnFragmentInteractionListener {
    void onFragmentInteraction(Uri uri);
  }

  private void loadSettings() {
    LOGIN    = settings.getString("login");
    UID      = settings.getString("activity_main_menu.uid");
    PASSWORD = settings.getString("password");
    TOKEN    = settings.getString("token");
    POSITION = settings.getInteger("position");
    DOCUMENT_UID = settings.getString("document.uid");
    STATUS_CODE = settings.getString("activity_main_menu.start");
    REG_NUMBER = settings.getString("activity_main_menu.regnumber");

  }

  @OnClick(R.id.activity_info_button_magnifer)
  public void magnifer(){

    DecisionSpinnerItem decision;
    DecisionMagniferFragment magnifer = new DecisionMagniferFragment();

    if ( decision_spinner_adapter.size() > 0 ){
      decision = decision_spinner_adapter.getItem( decision_spinner.getSelectedItemPosition() );
      magnifer.setDecision( decision );
      magnifer.setRegNumber( preview.getRegNumber() );
    }

    magnifer.show( getFragmentManager() , "DecisionMagniferFragment");
  }


  @OnClick(R.id.activity_info_button_edit)
  public void edit(){


    Gson gson = new Gson();
    RDecisionEntity data = decision_spinner_adapter.getItem( decision_spinner.getSelectedItemPosition() ).getDecision();
//    String json = gson.toJson(data, Decision.class);

//    Timber.tag("button_edit").i( json );




    Context context = getContext();
    Intent intent = new Intent( context , DecisionConstructorActivity.class);
//    intent.putExtra("decision", json);
    context.startActivity(intent);

  }


  private Boolean documentExist(){
    Integer count = dataStore
      .count( RDocumentEntity.class )
      .where( RDocumentEntity.UID.eq( uid == null? UID.get() : uid ) )
      .get()
      .value();

    return count > 0;
  }

  private void loadDocument() {

    if ( documentExist() ){
      loadFromDb();
    } else {
      loadFromJson();
    }
  }

  private void loadFromDb() {
    Timber.tag("loadFromDb").v("start");

    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq( uid == null? UID.get() : uid ))
      .get()
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(doc -> {

        Timber.tag("loadFromDb").i( "loaded %s", doc.getId() );

        Preference<String> documentNumber = settings.getString("document.number");
        documentNumber.set( doc.getRegistrationNumber() );

        preview.showEmpty();

        if ( doc.getDecisions().size() > 0 ){

          decision_spinner_adapter.clear();
//          decision_spinner_adapter.add( new DecisionSpinnerItem(null, "Всего резолюций", doc.getDecisions().size() ) );
//
//          desigions_recycler_view.setLayoutManager(new LinearLayoutManager(this));
//          decisions_list = new ArrayList<>();



          for (RDecision rDecision: doc.getDecisions()) {
//            Decision raw_decision = new Decision();
//
            RDecisionEntity decision = (RDecisionEntity) rDecision;
//
//            raw_decision.setId( String.valueOf(decision.getUid()) );
//            raw_decision.setLetterhead(decision.getLetterhead());
//            raw_decision.setSigner(decision.getSigner());
//            raw_decision.setSignerId(decision.getSignerId());
//            raw_decision.setAssistantId(decision.getAssistantId());
//            raw_decision.setSignerBlankText(decision.getSignerBlankText());
//            raw_decision.setComment(decision.getComment());
//            raw_decision.setDate(decision.getDate());
//            raw_decision.setApproved(decision.isApproved());
//            raw_decision.setUrgencyText(decision.getUrgencyText());
//            raw_decision.setSignerIsManager(decision.isSignerIsManager());
//            raw_decision.setShowPosition(decision.isShowPosition());
//            raw_decision.setSignBase64(decision.getSignBase64());
//            raw_decision.setApproved(decision.isApproved());
//
//
//            for (RBlock rBlock: decision.getBlocks()) {
//              RBlockEntity block = (RBlockEntity) rBlock;
//              Block raw_block = new Block();
//
//              raw_block.setNumber(block.getNumber());
//              raw_block.setText(block.getText());
//              raw_block.setAppealText(block.getAppealText());
//              raw_block.setTextBefore(block.isTextBefore());
//              raw_block.setHidePerformers(block.isHidePerformers());
//              raw_block.setToCopy(block.isToCopy());
//              raw_block.setToFamiliarization(block.isToFamiliarization());
//
//              for (RPerformer rPerformer: block.getPerformers()) {
//                RPerformerEntity performer = (RPerformerEntity) rPerformer;
//                Performer raw_performer = new Performer();
//
//                raw_performer.setNumber(performer.getNumber());
//                raw_performer.setPerformerId(performer.getPerformerId());
//                raw_performer.setPerformerType(performer.getPerformerType());
//                raw_performer.setPerformerText(performer.getPerformerText());
//                raw_performer.setOrganizationText(performer.getOrganizationText());
//                raw_performer.setIsOriginal(performer.isIsOriginal());
//                raw_performer.setIsResponsible(performer.isIsResponsible());
//
//                raw_block.getPerformers().add(raw_performer);
//              }
//
//              raw_decision.getBlocks().add(raw_block);
//            }


            decision_spinner_adapter.add( new DecisionSpinnerItem( decision, decision.getSignerBlankText(), decision.getDate() ) );

          }

//           если есть резолюции, то отобразить первую
          if ( decision_spinner_adapter.size() > 0 ) {
            current_decision = decision_spinner_adapter.getItem(0).getDecision();
            Timber.tag(TAG).e("decision_spinner_adapter > 0");
            displayDecision();
          }
        } else {
          Timber.e("no decisions");
          magnifer_button.setVisibility(View.GONE);
          toolbarManager.setEditDecisionMenuItemVisible(false);
          preview.showEmpty();
          EventBus.getDefault().post( new HasNoActiveDecisionConstructor() );
          decision_spinner_adapter.add( new DecisionSpinnerItem(null, "Нет резолюций", 0 ) );
        }

      });
  }

  private void displayDecision() {
    if (current_decision != null) {
      preview.show( current_decision );
      settings.getString("decision.active.id").set( current_decision.getUid() );
      toolbarManager.setEditDecisionMenuItemVisible( !current_decision.isApproved() );
      updateVisibility(current_decision.isApproved());
    }
  }

  private void loadFromJson(){
    Timber.e("loadFromJson");
//    HOST = settings.getString("settings_username_host");
//
//    Retrofit retrofit = new Retrofit.Builder()
//      .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//      .addConverterFactory(GsonConverterFactory.create())
//      .baseUrl(HOST.get() + "v3/documents/")
//      .client(okHttpClient)
//      .build();
//
//    DocumentService documentService = retrofit.create( DocumentService.class );
//
//    Observable<DocumentInfo> activity_main_menu = documentService.getInfo(
//      UID.get(),
//      LOGIN.get(),
//      TOKEN.get()
//    );
//
//    activity_main_menu.subscribeOn( Schedulers.newThread() )
//      .observeOn( AndroidSchedulers.mainThread() )
//      .subscribe(
//        data -> {
//          DOCUMENT = data;
//
//          Gson gson = new Gson();
//
//          Preference<String> documentNumber = settings.getString("document.number");
//          documentNumber.set( DOCUMENT.getRegistrationNumber() );
//
//          Preference<String> documentJson = settings.getString("document.json");
//          documentJson.set( gson.toJson(DOCUMENT) );
//
//          Preference<String> documentImages = settings.getString("document.images");
//          documentImages.set( gson.toJson( DOCUMENT.getImages() ) );
//
//          toolbar.setTitle( data.getTitle() );
//
//          if ( data.getDecisions().size() >= 1 ){
//
//            desigions_recycler_view.setLayoutManager(new LinearLayoutManager(this));
//
//            List<Decision> decisions_list = new ArrayList<>();
//            for (Decision decision: data.getDecisions()) {
//              decisions_list.add(decision);
//            }
//
//            decision_adapter = new DecisionAdapter(decisions_list, this, desigions_recycler_view);
//            desigions_recycler_view.setAdapter(decision_adapter);
//
//            // если есть резолюции, то отобразить первую
//            if ( decisions_list.size() > 0 ) {
//              try {
//                jobManager.addJobInBackground( new SetActiveDecisionJob(0) );
//              } catch ( Exception e){
//                Timber.tag(TAG + " massInsert error").v( e );
//              }
//            }
//
//            desigions_recycler_view.setLayoutManager(new LinearLayoutManager(this));
//
//            RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
//            itemAnimator.setAddDuration(10);
//            itemAnimator.setRemoveDuration(10);
//            desigions_recycler_view.setItemAnimator(itemAnimator);
//
//          }
//
//          if ( data.getInfoCard() != null ){
//            CARD = Base64.decode( data.getInfoCard().getBytes(), Base64.DEFAULT );
//          } else {
//            CARD = Base64.decode( "".getBytes(), Base64.DEFAULT );
//          }
//          Preference<String> infocard = settings.getString("document.infoCard");
//          infocard.set( new String(CARD , StandardCharsets.UTF_8) );
//
//        },
//        error -> {
//          Log.d( "++_ERROR", error.getMessage() );
//          Toast.makeText( this, error.getMessage(), Toast.LENGTH_SHORT).show();
//        });
  }

  public class Preview{

    private final Context context;
    private String TAG = this.getClass().getSimpleName();
    private String reg_number;

    Preview(Context context) {
      this.context = context;
    }

    private void clear(){
      preview_head.removeAllViews();
      preview_body.removeAllViews();
      preview_bottom.removeAllViews();
    };

    private void show( RDecisionEntity decision ){
      clear();

      Timber.tag("getUrgencyText").v("%s", decision.getUrgencyText() );
      Timber.tag("getLetterhead").v("%s",  decision.getLetterhead() );

      if( decision.getLetterhead() != null ) {
        printLetterHead(decision.getLetterhead());
      }

      if( decision.getUrgencyText() != null ){
        printUrgency(decision.getUrgencyText());
      }

      if( decision.getBlocks().size() > 0 ){


        Set<RBlock> blocks = decision.getBlocks();
//        List<Block> blocks = decision.getBlocks();


        for (RBlock b: blocks){
          RBlockEntity block = (RBlockEntity) b;
          Timber.tag("block").v( block.getText() );
          printAppealText( block );

          Boolean f = block.isToFamiliarization();
          if (f == null)
            f = false;

          Set<RPerformer> _performers = block.getPerformers();

          if ( block.isTextBefore() ){
            printBlockText( block.getText() );
            if (!block.isHidePerformers())
              printBlockPerformers( _performers, f, block.getNumber() );

          } else {
            if (!block.isHidePerformers())
              printBlockPerformers( _performers, f, block.getNumber() );
            printBlockText( block.getText() );
          }

        }
      }

      printSigner( decision.isShowPosition(), decision.getSignerBlankText(), decision.getSignerPositionS(), decision.getDate(), REG_NUMBER.get(), decision.getSignBase64()  );
    }

    private void showEmpty(){
      Timber.tag(TAG).d( "showEmpty" );

      clear();
      printLetterHead( getString(R.string.decision_blank) );
    }

    private void printSigner(Boolean showPosition, String signerBlankText, String signerPositionS, String date, String registrationNumber, String base64) {

      LinearLayout relativeSigner = new LinearLayout(context);
      relativeSigner.setOrientation(LinearLayout.VERTICAL);
      relativeSigner.setVerticalGravity( Gravity.BOTTOM );
      relativeSigner.setPadding(0,0,0,0);
//      relativeSigner.setMinimumHeight(350);
      LinearLayout.LayoutParams relativeSigner_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      relativeSigner_params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
      relativeSigner.setLayoutParams( relativeSigner_params );




      LinearLayout signer_view = new LinearLayout(context);
      signer_view.setOrientation(LinearLayout.VERTICAL);
//      signer_view.setPadding(0,0,0,0);

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


      if (base64 != null){
        ImageView image = new ImageView(getContext());


        byte[] decodedString = Base64.decode( base64 , Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        image.setImageBitmap( decodedByte );
        relativeSigner.addView( image );
      }

      relativeSigner.addView( signer_view );
      relativeSigner.addView( date_and_number_view );


      preview_bottom.addView( relativeSigner );
    }

    private void printUrgency(String urgency) {
      TextView urgencyView = new TextView(context);
      urgencyView.setGravity(Gravity.RIGHT);
      urgencyView.setAllCaps(true);
      urgencyView.setPaintFlags( Paint.UNDERLINE_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG | Paint.FAKE_BOLD_TEXT_FLAG );
      urgencyView.setText( urgency );
      urgencyView.setTextColor( ContextCompat.getColor(context, R.color.md_black_1000) );

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      params.setMargins(0,2,0,2);
      urgencyView.setLayoutParams(params);

      preview_head.addView( urgencyView );
    }

    private void printLetterHead(String letterhead) {
      TextView letterHead = new TextView(context);
      letterHead.setGravity(Gravity.CENTER);
      letterHead.setText( letterhead );
      letterHead.setTextColor( Color.BLACK );
      preview_head.addView( letterHead );

      TextView delimiter = new TextView(context);
      delimiter.setGravity(Gravity.CENTER);
      delimiter.setHeight(1);
      delimiter.setWidth(400);
      delimiter.setBackgroundColor( ContextCompat.getColor(context, R.color.md_blue_grey_200) );

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      params.setMargins(50, 10, 50, 10);
      delimiter.setLayoutParams(params);

      preview_head.addView( delimiter );
    }

    private void printBlockText(String text) {
      TextView block_view = new TextView(context);
      block_view.setText( text );
      block_view.setTextColor( Color.BLACK );

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      params.setMargins(0, 10, 0, 10);
      block_view.setLayoutParams(params);

      preview_body.addView( block_view );
    }

    private void printAppealText(RBlockEntity block ) {

      String text = "";
      String appealText;
      String number;
      boolean toFamiliarization = block.isToFamiliarization() == null ? false : block.isToFamiliarization();

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

      preview_body.addView( blockAppealView );
    }

    private void printBlockPerformers(Set<RPerformer> performers, Boolean toFamiliarization, Integer number) {

      boolean numberPrinted = false;
      LinearLayout users_view = new LinearLayout(context);
      users_view.setOrientation(LinearLayout.VERTICAL);
      users_view.setPadding(40,5,5,5);

      if( performers.size() > 0 ){

//        List<Performer> users = performers;
        for (RPerformer u: performers){

          RPerformerEntity user = (RPerformerEntity) u;
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


      preview_body.addView( users_view );
    }


    public String getRegNumber() {
      return reg_number;
    }
  }


  @Override
  public void onExecuteSuccess(String command) {
    Timber.tag(TAG).i("OperationManager.onExecuteSuccess %s", command);
    switch (command ){
      case "approve_decision":
        current_decision.setApproved(true);
        displayDecision();
        Snackbar.make( getView(), "Резолюция утверждена", Snackbar.LENGTH_LONG ).show();
        break;
      case "reject_decision":
        Snackbar.make( getView(), "Резолюция отклонена", Snackbar.LENGTH_LONG ).show();
      default:
        break;
    }
  }

  @Override
  public void onExecuteError() {
    Timber.tag(TAG).i("OperationManager.onExecuteError");
  }
}

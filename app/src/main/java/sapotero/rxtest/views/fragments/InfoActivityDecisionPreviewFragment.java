package sapotero.rxtest.views.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Base64;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
import sapotero.rxtest.db.requery.utils.DecisionConverter;
import sapotero.rxtest.events.decision.ApproveDecisionEvent;
import sapotero.rxtest.events.decision.HasNoActiveDecisionConstructor;
import sapotero.rxtest.events.decision.RejectDecisionEvent;
import sapotero.rxtest.events.view.InvalidateDecisionSpinnerEvent;
import sapotero.rxtest.events.view.ShowPrevDocumentEvent;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.managers.menu.OperationManager;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.managers.toolbar.ToolbarManager;
import sapotero.rxtest.utils.queue.QueueManager;
import sapotero.rxtest.views.activities.DecisionConstructorActivity;
import sapotero.rxtest.views.adapters.DecisionSpinnerAdapter;
import sapotero.rxtest.views.adapters.models.DecisionSpinnerItem;
import sapotero.rxtest.views.dialogs.DecisionMagniferFragment;
import timber.log.Timber;


@SuppressLint("ValidFragment")
public class InfoActivityDecisionPreviewFragment extends Fragment{

  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject OperationManager operationManager;
  @Inject QueueManager queue;

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
  @BindView(R.id.desigion_view_root) LinearLayout desigion_view_root;

  @BindView(R.id.activity_info_button_magnifer) ImageButton magnifer_button;
  @BindView(R.id.activity_info_button_edit) ImageButton edit;

  @BindView(R.id.activity_info_decision_spinner) Spinner decision_spinner;
  @BindView(R.id.activity_info_decision_preview_toolbar) Toolbar decision_toolbar;
  @BindView(R.id.activity_info_decision_preview_magnifer) ImageButton magnifer;

  @BindView(R.id.activity_info_decision_preview_next_person) Button next_person_button;
  @BindView(R.id.activity_info_decision_preview_prev_person) Button prev_person_button;
  @BindView(R.id.activity_info_decision_preview_approved_text) TextView approved_text;
  @BindView(R.id.activity_info_decision_preview_temporary) TextView temporary;



  private ArrayList<DecisionSpinnerItem> decisionSpinnerItems  = new ArrayList<>();;

  private DecisionSpinnerAdapter decision_spinner_adapter;
  private Preview preview;

  private Unbinder binder;
  private Preference<String> REG_NUMBER;
  private String uid;
  private RDecisionEntity current_decision;
  private String TAG = this.getClass().getSimpleName();
  private GestureDetector gestureDetector;
  private RDocumentEntity doc;

  public InfoActivityDecisionPreviewFragment() {
  }


  public InfoActivityDecisionPreviewFragment(ToolbarManager toolbarManager) {
    this.toolbarManager = toolbarManager;
  }

  @OnClick(R.id.activity_info_decision_preview_magnifer)
  public void expandView(){
    magnifer();
  }


  // Approve current decision
  @OnClick(R.id.activity_info_decision_preview_next_person)
  public void decision_preview_next(){
    Timber.tag(TAG).v("decision_preview_next star");

    if ( settings.getBoolean("settings_view_show_actions_confirm").get() ){
      // resolved https://tasks.n-core.ru/browse/MVDESD-12765
      // выводить подтверждение при подписании резолюции

        MaterialDialog.Builder prev_dialog = new MaterialDialog.Builder( getContext() )
          .content(R.string.decision_approve_body)
          .cancelable(true)
          .positiveText(R.string.yes)
          .negativeText(R.string.no)
          .onPositive((dialog1, which) -> {

            CommandFactory.Operation operation = CommandFactory.Operation.APPROVE_DECISION;

            CommandParams params = new CommandParams();

            params.setDecisionId( current_decision.getUid() );
            params.setDecisionModel( DecisionConverter.formatDecision(current_decision) );

            params.setDocument( settings.getString("activity_main_menu.uid").get() );
            params.setActiveDecision( decision_spinner_adapter.hasActiveDecision() );

            operationManager.execute(operation, params);
            updateAfteButtonPressed();
            EventBus.getDefault().post( new ShowPrevDocumentEvent());
          })
          .autoDismiss(true);

        prev_dialog.build().show();

    } else {
      CommandFactory.Operation operation;
      operation =CommandFactory.Operation.APPROVE_DECISION;

      CommandParams params = new CommandParams();

      params.setDecisionId( current_decision.getUid() );
//      params.setDecision( current_decision );
      params.setDecisionModel( DecisionConverter.formatDecision(current_decision) );
      params.setDocument( settings.getString("activity_main_menu.uid").get() );
      params.setActiveDecision( decision_spinner_adapter.hasActiveDecision() );

      operationManager.execute(operation, params);
      updateAfteButtonPressed();
      EventBus.getDefault().post( new ShowPrevDocumentEvent());
    }

    Timber.tag(TAG).v("decision_preview_next end");
  }

  // Reject current decision
  @OnClick(R.id.activity_info_decision_preview_prev_person)
  public void decision_preview_prev(){
    Timber.tag(TAG).v("decision_preview_prev");

    // resolved https://tasks.n-core.ru/browse/MVDESD-12765
    // Добавить ввод комментариев на "Отклонить резолюцию" и "без ответа"

    if ( settings.getBoolean("settings_view_show_comment_post").get() ){

      MaterialDialog.Builder prev_dialog = new MaterialDialog.Builder( getContext() )
        .content(R.string.decision_reject_body)
        .cancelable(true)
        .positiveText(R.string.yes)
        .negativeText(R.string.no)
        .onPositive((dialog1, which) -> {

          CommandFactory.Operation operation =CommandFactory.Operation.REJECT_DECISION;

          CommandParams commandParams = new CommandParams();
          commandParams.setDecisionId( current_decision.getUid() );
//          commandParams.setDecision( current_decision );
          commandParams.setDecisionModel( DecisionConverter.formatDecision(current_decision) );
          commandParams.setActiveDecision( decision_spinner_adapter.hasActiveDecision() );
          commandParams.setComment( dialog1.getInputEditText().getText().toString() );

          operationManager.execute(operation, commandParams);
          updateAfteButtonPressed();
          EventBus.getDefault().post( new ShowPrevDocumentEvent());
        })
        .autoDismiss(true);

      // настройка
      // Показывать комментарий при отклонении
      if ( settings.getBoolean("settings_view_show_comment_post").get() ){
        prev_dialog.inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES )
          .input(R.string.comment_hint, R.string.dialog_empty_value, (dialog12, input) -> {});
      }


      prev_dialog.build().show();

    } else {

      CommandFactory.Operation operation;
      operation =CommandFactory.Operation.REJECT_DECISION;

      CommandParams params = new CommandParams();
      params.setDecisionId( current_decision.getUid() );
//      params.setDecision( current_decision );
      params.setDecisionModel( DecisionConverter.formatDecision(current_decision) );
      params.setActiveDecision( decision_spinner_adapter.hasActiveDecision() );

      operationManager.execute(operation, params);
      updateAfteButtonPressed();
      EventBus.getDefault().post( new ShowPrevDocumentEvent());
    }
  }

  private void updateAfteButtonPressed() {
    decision_spinner_adapter.setCurrentAsTemporary( decision_spinner.getSelectedItemPosition() );
    current_decision = decision_spinner_adapter.getItem(decision_spinner.getSelectedItemPosition()).getDecision();
    displayDecision();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

  }

  @Override
  public void onResume() {
    super.onResume();
    invalidate();
  }

  public class GestureListener extends
    GestureDetector.SimpleOnGestureListener {

    @Override
    public boolean onDown(MotionEvent e) {
      return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
      Timber.tag("GestureListener").w("DOUBLE TAP");



      if ( !doc.isFromLinks() && current_decision != null ){

        if ( !queue.getConnected() &&
          current_decision.isTemporary() != null &&
          current_decision.isTemporary() ){

          edit();
        }

        if ( queue.getConnected() &&
          current_decision.isTemporary() != null &&
          current_decision.isTemporary() ){
          Toast.makeText( getContext(), "Запрещено редактировать резолюции в онлайне!\nДождитесь синхронизации.", Toast.LENGTH_SHORT ).show();
        }

        if ( current_decision.isApproved() != null &&
          !current_decision.isApproved() &&
          current_decision.isTemporary() != null &&
          !current_decision.isTemporary()){
          edit();
        }
      }

      if (!doc.isFromLinks() &&
        current_decision == null ){

        settings.getString("decision.active.id").set(null);
        Context context = getContext();
        Intent create_intent = new Intent(context, DecisionConstructorActivity.class);
        context.startActivity(create_intent);

      }

      return true;
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_info_card_decision_preview, container, false);

    EsdApplication.getComponent( getContext() ).inject(this);
    binder = ButterKnife.bind(this, view);

    invalidate();

    return view;
  }

  private void invalidate() {
    initEvents();
    loadSettings();
    loadDocument();

    initToolBar();
    setAdapter();



    gestureDetector = new GestureDetector( getContext(),new GestureListener() );

    desigion_view_root.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
      }
    });
    preview_body.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
      }
    });

    preview = new Preview(getContext());
  }

  private void initToolBar() {

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

    showDecisionCardTollbarMenuItems(true);

    // FIX для ссылок
    if (current_decision == null) {
      next_person_button.setVisibility( !approved ? View.INVISIBLE : View.GONE);
      prev_person_button.setVisibility( !approved ? View.INVISIBLE : View.GONE);
    }


    if (approved){
      decision_toolbar.getMenu().findItem(R.id.decision_preview_edit).setVisible(false);
    }

    approved_text.setVisibility(!approved ? View.GONE : View.VISIBLE);

    if ( !decision_spinner_adapter.hasActiveDecision() ){
      Timber.tag(TAG).e("NO ACTIVE DECISION");
      EventBus.getDefault().post( new HasNoActiveDecisionConstructor() );
    }


    //FIX не даем выполнять операции для связанных документов
    if ( doc!=null && doc.isFromLinks()!=null && doc.isFromLinks() ){
      next_person_button.setVisibility( View.GONE );
      prev_person_button.setVisibility( View.GONE );
    }


    if ( current_decision.isTemporary() != null && current_decision.isTemporary() ){
      temporary.setVisibility(View.VISIBLE);
      next_person_button.setVisibility( View.GONE );
      prev_person_button.setVisibility( View.GONE );
    } else {
      temporary.setVisibility(View.GONE);
    }

    // resolved https://tasks.n-core.ru/browse/MVDESD-13146
    // для статуса "на первичное рассмотрение" вместо "Подписать" должно быть "Согласовать"
    // Если подписывающий в резолюции и оператор в МП совпадают, то кнопка должна быть "Подписать"
    if ( doc.getFilter() != null && doc.getFilter().equals("primary_consideration") ){
      if ( current_decision != null &&
           current_decision.getSignerId() != null &&
           current_decision.getSignerId().equals( settings.getString("current_user_id").get() ) ){
        next_person_button.setText( getString(R.string.menu_info_next_person));
      } else {
        next_person_button.setText( getString(R.string.menu_info_sign_next_person) );
      }
    }

  }

  private void showDecisionCardTollbarMenuItems(boolean visible) {
    try {
      decision_toolbar.getMenu().findItem(R.id.decision_preview_magnifer).setVisible(visible);
      decision_toolbar.getMenu().findItem(R.id.decision_preview_edit).setVisible(visible);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    binder.unbind();

    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
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
    STATUS_CODE = settings.getString("activity_main_menu.star");
    REG_NUMBER = settings.getString("activity_main_menu.regnumber");

  }

  @OnClick(R.id.activity_info_button_magnifer)
  public void magnifer(){
    Timber.tag(TAG).v("magnifer");
    DecisionSpinnerItem decision;
    DecisionMagniferFragment magnifer = new DecisionMagniferFragment();

    if ( decision_spinner_adapter.size() > 0 ){
      decision = decision_spinner_adapter.getItem( decision_spinner.getSelectedItemPosition() );
      magnifer.setDecision( decision );
      magnifer.setRegNumber( REG_NUMBER.get() );
    }

    magnifer.show( getFragmentManager() , "DecisionMagniferFragment");
  }


  @OnClick(R.id.activity_info_button_edit)
  public void edit(){

    Timber.tag(TAG).v("edit");
    Gson gson = new Gson();
    RDecisionEntity data = decision_spinner_adapter.getItem( decision_spinner.getSelectedItemPosition() ).getDecision();


    Timber.tag(TAG).v("DECISION");
    Timber.tag(TAG).v("%s", data);


    Context context = getContext();
    Intent intent = new Intent( context , DecisionConstructorActivity.class);
    Bundle bundle = intent.getExtras();

//    if (current_decision != null) {
//      bundle.putString( "decision", gson.toJson( DecisionConverter.formatDecision(current_decision), Decision.class)  );
//    }

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
    Timber.tag("loadFromDb").v("star");

    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq( uid == null? UID.get() : uid ))
      .get()
      .toObservable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(doc -> {

        this.doc = doc;

        Timber.tag("loadFromDb").i( "loaded %s", doc.getId() );

        Preference<String> documentNumber = settings.getString("document.number");
        documentNumber.set( doc.getRegistrationNumber() );

        preview.showEmpty();

        if ( doc.getDecisions().size() > 0 ){

          decision_spinner_adapter.clear();

          List<DecisionSpinnerItem> unsorterd_decisions = new ArrayList<DecisionSpinnerItem>();


          for (RDecision rDecision: doc.getDecisions()) {
            RDecisionEntity decision = (RDecisionEntity) rDecision;
            unsorterd_decisions.add( new DecisionSpinnerItem( decision, decision.getSignerBlankText(), decision.getDate() ) );
          }

          Collections.sort(unsorterd_decisions, (o1, o2) -> o1.getDecision().getUid().compareTo( o2.getDecision().getUid() ));

          decision_spinner_adapter.addAll( unsorterd_decisions );


//           если есть резолюции, то отобразить первую
          if ( decision_spinner_adapter.size() > 0 ) {
            current_decision = decision_spinner_adapter.getItem(0).getDecision();
            Timber.tag(TAG).e("decision_spinner_adapter > 0");
            displayDecision();
          }
        } else {
          Timber.e("no decisions");
          magnifer_button.setVisibility(View.GONE);
          if (toolbarManager != null) {
            toolbarManager.setEditDecisionMenuItemVisible(false);
          }
          preview.showEmpty();
          EventBus.getDefault().post( new HasNoActiveDecisionConstructor() );
          decision_spinner_adapter.add( new DecisionSpinnerItem(null, "Нет резолюций", 0 ) );

          showDecisionCardTollbarMenuItems(false);
        }

      });
  }

  private void displayDecision() {
    Timber.tag(TAG).v("displayDecision");

    if (current_decision != null) {
      preview.show( current_decision );

      settings.getInteger("decision.active.id").set( current_decision.getId() );

      if (toolbarManager != null) {
        toolbarManager.setEditDecisionMenuItemVisible( !current_decision.isApproved() );
      }
      updateVisibility( current_decision.isApproved() );
    }

//    if ( !decision_spinner_adapter.hasActiveDecision() ){
//      toolbarManager.showAsProcessed();
//    }
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
//              decisions_list.addByOne(decision);
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
      clear();
    }

    private void clear(){
      try {
        preview_head.removeAllViews();
        preview_body.removeAllViews();
        preview_bottom.removeAllViews();
      } catch (Exception e) {
        e.printStackTrace();
      }
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

        Boolean isOnlyOneBlock = false;

        if (decision.getBlocks().size() == 1){
          isOnlyOneBlock = true;
        }

        Set<RBlock> _blocks = decision.getBlocks();

        ArrayList<RBlockEntity> blocks = new ArrayList<>();
        for (RBlock b: _blocks){
          blocks.add( (RBlockEntity) b );
        }

        Collections.sort(blocks, (o1, o2) -> o1.getNumber().compareTo( o2.getNumber() ));


        for (RBlock b: blocks){
          RBlockEntity block = (RBlockEntity) b;

          Timber.tag("showPosition").v( "ShowPosition: %s", block.getAppealText() );

          printAppealText( block, isOnlyOneBlock );

          if ( block.isTextBefore() ){
            printBlockText( block.getText() );
            if ( block.isHidePerformers() != null && !block.isHidePerformers())
              printBlockPerformers( block, isOnlyOneBlock );

          } else {
            if ( block.isHidePerformers() != null && !block.isHidePerformers())
              printBlockPerformers( block, isOnlyOneBlock );
            printBlockText( block.getText() );
          }

        }
      }

      printSigner( decision, REG_NUMBER.get() );
    }

    private void showEmpty(){
      Timber.tag(TAG).d( "showEmpty" );

      clear();
      printLetterHead( getString(R.string.decision_blank) );
    }

    private void printSigner(RDecisionEntity decision, String registrationNumber) {

//      Timber.tag(TAG).i("DECISION\n%s", new Gson().toJson(decision));

      LinearLayout relativeSigner = new LinearLayout(context);
      relativeSigner.setOrientation(LinearLayout.VERTICAL);
      relativeSigner.setVerticalGravity( Gravity.BOTTOM );
      relativeSigner.setPadding(0,0,0,0);
      LinearLayout.LayoutParams relativeSigner_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      relativeSigner_params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
      relativeSigner.setLayoutParams( relativeSigner_params );


      LinearLayout.LayoutParams viewsLayotuParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);


      LinearLayout signer_view = new LinearLayout(context);
      signer_view.setOrientation(LinearLayout.VERTICAL);

      if ( decision.isShowPosition() != null && decision.isShowPosition() ){
        TextView signerPositionView = new TextView(context);
        signerPositionView.setText( decision.getSignerPositionS() );
        signerPositionView.setTextColor( ContextCompat.getColor(context, R.color.md_grey_800) );
        signerPositionView.setTypeface( Typeface.create("sans-serif-light", Typeface.NORMAL) );
        signerPositionView.setGravity( Gravity.END );

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.MATCH_PARENT,
          LinearLayout.LayoutParams.MATCH_PARENT,
          1.0f
        );
        signerPositionView.setLayoutParams(param);

        signer_view.addView( signerPositionView );
      }

      TextView signerBlankTextView = new TextView(context);
      signerBlankTextView.setText( DecisionConverter.formatName( decision.getSignerBlankText() ) );
      signerBlankTextView.setTextColor( Color.BLACK );
      signerBlankTextView.setGravity( Gravity.END);
      signerBlankTextView.setTypeface( Typeface.create("sans-serif-medium", Typeface.NORMAL) );
      signerBlankTextView.setLayoutParams(viewsLayotuParams);

      LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT,
        1.0f
      );
      signerBlankTextView.setLayoutParams(param);

      signer_view.addView( signerBlankTextView );





      LinearLayout date_and_number_view = new LinearLayout(context);
      date_and_number_view.setOrientation(LinearLayout.HORIZONTAL);

      TextView numberView = new TextView(context);
      numberView.setText( "№ " + registrationNumber );
      numberView.setTextColor( Color.BLACK );
      numberView.setTypeface( Typeface.create("sans-serif-medium", Typeface.NORMAL) );
      numberView.setLayoutParams(viewsLayotuParams);
      numberView.setGravity( Gravity.END );

      TextView dateView = new TextView(context);
      dateView.setText( decision.getDate() );
      dateView.setGravity( Gravity.START );
      dateView.setTextColor( Color.BLACK );
      dateView.setTypeface( Typeface.create("sans-serif-medium", Typeface.NORMAL) );
      dateView.setLayoutParams(viewsLayotuParams);

      date_and_number_view.addView(dateView);
      date_and_number_view.addView(numberView);


      if (decision.getSignBase64() != null){
        ImageView image = new ImageView(getContext());


        byte[] decodedString = Base64.decode( decision.getSignBase64() , Base64.DEFAULT);
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
      letterHead.setTypeface( Typeface.create("sans-serif-medium", Typeface.NORMAL) );
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
      block_view.setText( "\u00A0     " + text );
      block_view.setTextColor( Color.BLACK );
      block_view.setTypeface( Typeface.create("sans-serif-light", Typeface.NORMAL) );

      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      params.setMargins(0, 10, 0, 10);
      block_view.setLayoutParams(params);

      preview_body.addView( block_view );
    }

    private void printAppealText(RBlock _block, Boolean isOnlyOneBlock) {

      RBlockEntity block = (RBlockEntity) _block;
      String text = "";

      if (block.getAppealText() != null && !Objects.equals(block.getAppealText(), "")) {

        if (!isOnlyOneBlock ){
          text += block.getNumber().toString() + ". ";
        }

        text += block.getAppealText();
      }

      TextView blockAppealView = new TextView(context);
      blockAppealView.setGravity(Gravity.CENTER);
      blockAppealView.setText( text );
      blockAppealView.setTextColor( Color.BLACK );
      blockAppealView.setTextSize( TypedValue.COMPLEX_UNIT_SP, 12 );

      preview_body.addView( blockAppealView );
    }

    private void printBlockPerformers(RBlock _block, Boolean isOnlyOneBlock) {

      RBlockEntity block = (RBlockEntity) _block;

      boolean numberPrinted = false;
      LinearLayout users_view = new LinearLayout(context);
      users_view.setOrientation(LinearLayout.VERTICAL);
      users_view.setPadding(40,5,5,5);

      if( block.getPerformers().size() > 0 ){
        for (RPerformer _user: block.getPerformers()){

          RPerformerEntity user = (RPerformerEntity) _user;
          String performerName = "";

          if ( block.getAppealText() == null && !numberPrinted && !isOnlyOneBlock ){
            performerName += block.getNumber().toString() + ". ";
            numberPrinted = true;
          }

          performerName += DecisionConverter.formatName( user.getPerformerText() );

          if (user.isIsResponsible() != null && user.isIsResponsible()){
            performerName += " *";
          }

//          performerName = performerName.replaceAll( "\\(.+\\)", "" );


          TextView performer_view = new TextView( getActivity() );
          performer_view.setText( performerName );
          performer_view.setTextColor( Color.BLACK );
          performer_view.setPaintFlags( Paint.ANTI_ALIAS_FLAG );
          performer_view.setGravity(Gravity.CENTER);
          performer_view.setTypeface( Typeface.create("sans-serif-medium", Typeface.NORMAL) );

          users_view.addView(performer_view);
        }
      }


      preview_body.addView( users_view );
    }


    public String getRegNumber() {
      return reg_number;
    }
  }

  private void initEvents() {
    Timber.tag(TAG).v("initEvents");
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(ApproveDecisionEvent event) throws Exception {
    Timber.d("ApproveDecisionEvent");
    current_decision.setApproved(true);
    displayDecision();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(RejectDecisionEvent event) throws Exception {
    Timber.d("RejectDecisionEvent");
    current_decision.setApproved(false);
    displayDecision();
    hideButtons();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(UpdateCurrentDocumentEvent event) throws Exception {
    Timber.tag(TAG).w("UpdateCurrentDocumentEvent %s", event.uid);
    if (Objects.equals(event.uid, UID.get())){
      invalidate();
    }
  }


  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(InvalidateDecisionSpinnerEvent event) throws Exception {
    Timber.tag(TAG).w("InvalidateDecisionSpinnerEvent %s", event.uid);
    decision_spinner_adapter.invalidate(event.uid);
  }


  private void hideButtons() {
    next_person_button.setVisibility( View.INVISIBLE );
    prev_person_button.setVisibility( View.INVISIBLE );
  }

}

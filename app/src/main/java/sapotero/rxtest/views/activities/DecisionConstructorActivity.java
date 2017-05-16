package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RUrgencyEntity;
import sapotero.rxtest.db.requery.models.decisions.RBlock;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformer;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.db.requery.utils.DecisionConverter;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.decision.ApproveDecisionEvent;
import sapotero.rxtest.events.decision.RejectDecisionEvent;
import sapotero.rxtest.managers.menu.OperationManager;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.managers.view.DecisionManager;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.views.adapters.models.FontItem;
import sapotero.rxtest.views.adapters.models.UrgencyItem;
import sapotero.rxtest.views.custom.SpinnerWithLabel;
import sapotero.rxtest.views.dialogs.DecisionTextDialog;
import sapotero.rxtest.views.dialogs.SelectOshsDialogFragment;
import sapotero.rxtest.views.dialogs.SelectTemplateDialogFragment;
import sapotero.rxtest.views.fragments.DecisionFragment;
import sapotero.rxtest.views.fragments.DecisionPreviewFragment;
import timber.log.Timber;

public class DecisionConstructorActivity extends AppCompatActivity implements DecisionFragment.OnFragmentInteractionListener, DecisionPreviewFragment.OnFragmentInteractionListener, OperationManager.Callback, SelectOshsDialogFragment.Callback, SelectTemplateDialogFragment.Callback {

  @Inject RxSharedPreferences settings;
  @Inject Settings settings2;
  @Inject OperationManager operationManager;
  @Inject SingleEntityStore<Persistable> dataStore;

  @BindView(R.id.toolbar) Toolbar toolbar;

//  @BindView(R.id.fragment_decision_autocomplete_field) DelayAutoCompleteTextView user_autocomplete;
//  @BindView(R.id.fragment_decision_autocomplete_field_loading_indicator) ProgressBar indicator;

  @BindView(R.id.activity_decision_constructor_wrapper) RelativeLayout wrapper;
  @BindView(R.id.decision_constructor_decision_preview) RelativeLayout testWrapper;

  @BindView(R.id.urgency_selector) SpinnerWithLabel<UrgencyItem> urgency_selector;
  @BindView(R.id.head_font_selector) SpinnerWithLabel<FontItem> font_selector;
  @BindView(R.id.signer_oshs_selector) EditText signer_oshs_selector;


  @BindView(R.id.sign_as_current_user) Button sign_as_current_user;
  @BindView(R.id.select_oshs_wrapper) LinearLayout select_oshs_wrapper;

  @BindView(R.id.activity_decision_constructor_scroll_wrapper) ScrollView scroll;


  @BindView(R.id.decision_constructor_decision_comment) EditText decision_comment;
  @BindView(R.id.decision_constructor_decision_date)    EditText decision_date;




  private String TAG = this.getClass().getSimpleName();
  private DecisionManager manager;
  private Decision raw_decision;
  private RDecisionEntity rDecisionEntity;
  private SelectOshsDialogFragment dialogFragment;
  private Fields.Status status;
  private DecisionConstructorActivity context;

  private String originalSigner;
  private String originalSignerBlankText;
  private String originalSignerId;
  private String originalSignerAssistantId;
  private ArrayList<UrgencyItem> urgency = new ArrayList<UrgencyItem>();

  @Override
  public void finish() {
    settings.getString("_status").set( "" );
    super.finish();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_decision_constructor);

    ButterKnife.bind(this);
    EsdApplication.getManagerComponent().inject(this);

    context = this;

    status  = Fields.Status.findStatus( settings2.getStatusCode() );

    toolbar.setTitleTextColor( getResources().getColor( R.color.md_grey_100 ) );
    toolbar.setSubtitleTextColor( getResources().getColor( R.color.md_grey_400 ) );

    toolbar.setContentInsetStartWithNavigation(250);

    toolbar.setTitle("Редактор резолюции ");
    toolbar.inflateMenu(R.menu.info_decision_constructor);



    toolbar.setNavigationOnClickListener( v -> {


      Gson gson = new GsonBuilder().setPrettyPrinting().create();

      Decision _dec_ = manager.getDecision();

      if ( settings2.isDecisionWithAssignment() ){
        Timber.tag(TAG).w("ASSIGNMENT: %s", settings2.isDecisionWithAssignment() );
        _dec_.setAssignment(true);
      }

      String json = gson.toJson( _dec_ );



      Timber.tag(TAG).w("DECISION: %s", json );
      Timber.tag(TAG).w("ASSIGNMENT: %s", settings2.isDecisionWithAssignment() );

      Decision save_decision = manager.getDecision();


      if ( manager.isChanged() ){
        Boolean showSaveDialog = checkDecision();

        String content = "Резолюция была изменена";

        if ( settings2.isDecisionWithAssignment() ){
          content = "Поручение не отправлено. Вернуться назад и удалить поручение?";
        }

        if (showSaveDialog){
          new MaterialDialog.Builder(this)
            .title("Имеются несохранненые данные")
            .content(content)
            .positiveText("сохранить")
            .onPositive(
              (dialog, which) -> {



//                manager.getDecisionBuilder().build();

                Decision decision = manager.getDecision();

                CommandParams params = new CommandParams();
                params.setDecisionModel( decision );

                decision.setDocumentUid( settings2.getUid() );

                if (rDecisionEntity != null) {
//                  params.setDecision( rDecisionEntity );
                  params.setDecisionModel( DecisionConverter.formatDecision(rDecisionEntity) );
                  params.setDecisionId( rDecisionEntity.getUid() );

                  RDocumentEntity doc = (RDocumentEntity) rDecisionEntity.getDocument();
                  params.setDocument( settings2.getUid() );

                  if (doc != null) {
                    params.setDocument(doc.getUid());
                  }
                } else {
                  params.setDocument( settings2.getUid() );
                }



                CommandFactory.Operation operation = rDecisionEntity == null ? CommandFactory.Operation.CREATE_DECISION : CommandFactory.Operation.SAVE_DECISION;

                if (save_decision != null && operation == CommandFactory.Operation.SAVE_DECISION) {
                  params.setDecisionModel(save_decision);

                  rDecisionEntity.setTemporary(true);
                }






                if ( settings2.isDecisionWithAssignment() ){
                  decision = manager.getDecision();

                  params = new CommandParams();
                  params.setDecisionModel( decision );

                  decision.setDocumentUid( settings2.getUid() );

                  if (rDecisionEntity != null) {
                    params.setDecisionModel( DecisionConverter.formatDecision(rDecisionEntity) );
                    params.setDecisionId( rDecisionEntity.getUid() );
                  }

                  operation = CommandFactory.Operation.CREATE_AND_APPROVE_DECISION;

                  params.setAssignment(true);
                  decision.setAssignment(true);

                }
                operationManager.execute( operation, params );

                finish();

              }
            )
            .neutralText("выход")
            .onNeutral(
              (dialog, which) -> {
                Timber.tag(TAG).w("nothing");

                // Restore original signer
                raw_decision.setSignerId(originalSignerId);
                raw_decision.setSigner(originalSigner);
                raw_decision.setSignerBlankText(originalSignerBlankText);
                raw_decision.setAssistantId(originalSignerAssistantId);

                if (rDecisionEntity != null) {
                  rDecisionEntity.setSignerId(originalSignerId);
                  rDecisionEntity.setSigner(originalSigner);
                  rDecisionEntity.setSignerBlankText(originalSignerBlankText);
                  rDecisionEntity.setAssistantId(originalSignerAssistantId);
                }

                finish();
//                activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
              }
            )
            .negativeText("возврат")
            .onNegative(
              (dialog, which) -> {
                Timber.tag(TAG).w("negative");
              }
            )
            .show();
        }

      } else {
        finish();
//        activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
      }

    } );


    toolbar.setOnMenuItemClickListener(item -> {

      CommandParams params;
      CommandFactory.Operation operation;

      switch (item.getItemId()){


        case R.id.action_constructor_create_and_sign:
          boolean canCreateAndSign = checkDecision();

          if (canCreateAndSign) {


            Decision decision = manager.getDecision();

            params = new CommandParams();
            params.setDecisionModel( decision );

            decision.setDocumentUid( settings2.getUid() );

            if (rDecisionEntity != null) {
              params.setDecisionModel( DecisionConverter.formatDecision(rDecisionEntity) );
              params.setDecisionId( rDecisionEntity.getUid() );
            }

            operation = CommandFactory.Operation.CREATE_AND_APPROVE_DECISION;

            if (rDecisionEntity != null){
              operation = CommandFactory.Operation.SAVE_AND_APPROVE_DECISION;
              params.setDecisionId( rDecisionEntity.getUid() );
              params.setDecisionModel( manager.getDecision() );
            }

            if ( settings2.isDecisionWithAssignment() ){
              Timber.tag(TAG).w("ASSIGNMENT: %s", settings2.isDecisionWithAssignment() );
              params.setAssignment(true);
              decision.setAssignment(true);
            }

            operationManager.execute( operation, params );

            finish();
          }

          break;
        case R.id.action_constructor_add_block:
          manager.getDecisionBuilder().addBlock();

//          scroll.fullScroll(ScrollView.FOCUS_DOWN);

          break;

        case R.id.action_constructor_next:
          // настройка
          // Показывать подтверждения о действиях с документом
          if ( settings2.isActionsConfirm() ){
            showNextDialog();
          } else {

            // operationManager.registerCallBack(this);

            operation =CommandFactory.Operation.APPROVE_DECISION;

            params = new CommandParams();
            params.setDecisionId( rDecisionEntity.getUid() );
//            params.setDecision( rDecisionEntity );
            params.setDecisionModel( DecisionConverter.formatDecision(rDecisionEntity) );

            if ( settings2.isDecisionWithAssignment() ){
              Timber.tag(TAG).w("ASSIGNMENT: %s", settings2.isDecisionWithAssignment() );
              params.setAssignment(true);
            }
            operationManager.execute(operation, params);
          }

          break;
        case R.id.action_constructor_prev:

          // настройка
          // Показывать подтверждения о действиях с документом
          if ( settings2.isActionsConfirm() ){
            showPrevDialog();
          } else {
//            operationManager.registerCallBack(this);

            operation =CommandFactory.Operation.REJECT_DECISION;

            params = new CommandParams();
            params.setDecisionId( rDecisionEntity.getUid() );
//            params.setDecision( rDecisionEntity );
            params.setDecisionModel( DecisionConverter.formatDecision(rDecisionEntity) );
            operationManager.execute(operation, params);
          }

          break;

        default:
          break;
      }

      return false;
    });

    raw_decision = null;
    Gson gson = new Gson();

    Intent intent = getIntent();

    if (null != intent) {
      String data = intent.getStringExtra("decision");
      raw_decision = gson.fromJson(data, Decision.class);

      Timber.tag(TAG).v( "getIntent ++" + raw_decision);
      if (raw_decision == null) {

        loadDecision();

      }
    }

    manager = new DecisionManager(this, getSupportFragmentManager(), raw_decision);
    manager.build();


    // настройка
    if (!settings2.isShowUrgency()){
      urgency_selector.setVisibility(View.GONE);
    } else {

      urgency_selector.setVisibility(View.VISIBLE);
      dataStore
        .select(RUrgencyEntity.class)
        .where(RUrgencyEntity.USER.eq( settings2.getLogin() ))
        .get()
        .toObservable()
        .filter(rUrgencyEntity -> {
          Boolean result = true;

          Timber.d("filter: %s", new Gson().toJson(rUrgencyEntity) );

          if ( settings.getBoolean("settings_view_only_urgent").get() ){
            if (!Objects.equals(rUrgencyEntity.getName().toLowerCase(), "срочно")){
              result = false;
            }
          }

          return result;
        })
        .toList()
        .observeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          list -> {


            if (list.size() > 0){
              for (RUrgencyEntity u: list) {
                urgency.add(new UrgencyItem(u.getName(), u.getUid()));
              }
              urgency_selector.setItems(urgency);
            }


            if ( raw_decision != null && raw_decision.getUrgencyText() != null ){

              for (int i = 0; i < urgency.size(); i++) {
                UrgencyItem urgency_item = urgency.get(i);
                Timber.tag(TAG).d("urgency: %s | %s", raw_decision.getUrgencyText(), urgency_item.getLabel());

                if (Objects.equals(urgency_item.getLabel().toLowerCase(), raw_decision.getUrgencyText().toLowerCase())){
                  Timber.tag(TAG).d("SUCCES: %s | %s", raw_decision.getUrgencyText(), urgency_item.getLabel());

                  manager.setUrgencyText(urgency_item.getLabel());
                  manager.setUrgency(urgency_item.getValue());

                  urgency_selector.setText(urgency_item.getLabel());
                  urgency_selector.setSelected(true);
                  break;
                }
              }
            }

          },
          error -> {
            Timber.tag(TAG).e(error);
          }
        );
    }



    urgency_selector.setOnItemSelectedListener((item, selectedIndex) -> {
      manager.setUrgency( item );
    });


    signer_oshs_selector.setOnClickListener(v -> {
      dialogFragment = new SelectOshsDialogFragment();
      dialogFragment.registerCallBack( this );
      dialogFragment.withSearch(true);
      dialogFragment.withChangePerson(false);
      dialogFragment.showWithAssistant(true);
      dialogFragment.show( getFragmentManager(), "SelectOshsDialogFragment");
    });

    sign_as_current_user.setOnClickListener(v -> {
      Timber.tag(TAG).e( "%s | %s", rDecisionEntity == null, raw_decision == null );
      updateSigner(
              getCurrentUserId(),
              getCurrentUserName(),
              getCurrentUserOrganization(),
              null
      );

      invalidateSaveAndSignButton();
    });

    if ( rDecisionEntity != null ){
      manager.setSigner( rDecisionEntity.getSigner() );
      manager.setSignerId( rDecisionEntity.getSignerId() );
      manager.setSignerBlankText( rDecisionEntity.getSignerBlankText() );
      decision_comment.setText( rDecisionEntity.getComment() );
      signer_oshs_selector.setText( rDecisionEntity.getSigner() );
    } else {
      String signerName = getCurrentUserName();
      String signerOrganization = getCurrentUserOrganization();
      raw_decision.setSignerId( getCurrentUserId() );
      raw_decision.setSigner( makeSignerWithOrganizationText(signerName, signerOrganization) );
      raw_decision.setSignerBlankText( signerName );
      signer_oshs_selector.setText( raw_decision.getSigner() );
    }

    if ( rDecisionEntity != null && rDecisionEntity.getDate() != null ){
      decision_date.setText( rDecisionEntity.getDate() );
      manager.setDate( rDecisionEntity.getDate() );
    } else {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
      Calendar cal = Calendar.getInstance();
      String date = dateFormat.format(cal.getTime());
      decision_date.setEnabled(false);
      decision_date.setFocusable(false);
      decision_date.setText( date );
      manager.setDate( date );
    }

    // настройка
    if ( settings.getBoolean("settings_view_show_decision_date_update").get() ){
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
      Calendar cal = Calendar.getInstance();
      String date = dateFormat.format(cal.getTime());
      decision_date.setEnabled(false);
      decision_date.setFocusable(false);
      decision_date.setText( date );
      manager.setDate( date );
    }




    if ( status == Fields.Status.SENT_TO_THE_REPORT ){
      // настройка
      if ( !settings.getBoolean("settings_view_show_change_signer").get() ){
        select_oshs_wrapper.setVisibility(View.GONE);
      }
    }

    // Disable EditText scrolling
    decision_comment.setMovementMethod(null);

    decision_comment.setOnClickListener(v -> {
      String title = getString(R.string.comment_hint);
      new DecisionTextDialog(context, decision_comment, title, title).show();
    });

    decision_comment.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        manager.setComment(s);
      }

      @Override
      public void afterTextChanged(Editable s) {

      }
    });

    // настройка
    if (settings.getBoolean("settings_view_show_decision_change_font").get()){
      List<FontItem> fonts = new ArrayList<>();
      fonts.add(new FontItem("10", "10"));
      fonts.add(new FontItem("11", "11"));
      fonts.add(new FontItem("12", "12"));
      fonts.add(new FontItem("13", "13"));
      fonts.add(new FontItem("14", "14"));
      fonts.add(new FontItem("15", "15"));

      font_selector.setItems(fonts);
      font_selector.setOnItemSelectedListener((item, selectedIndex) -> {
        manager.setPerformersFontSize(item.getValue());
      });

      Timber.tag(TAG).e("font-size: %s %s", raw_decision.getLetterheadFontSize(), raw_decision.getPerformersFontSize());

      if ( raw_decision != null && raw_decision.getPerformersFontSize() != null ){

        Timber.tag(TAG).e("FONT SIZE: %s", Integer.parseInt( raw_decision.getPerformersFontSize().substring(1) ));

        font_selector.setText( raw_decision.getPerformersFontSize() );
        manager.setPerformersFontSize( raw_decision.getPerformersFontSize() );
      } else {
        font_selector.setText( "15" );
        manager.setPerformersFontSize( "15" );
      }

    } else {
      font_selector.setVisibility(View.GONE);
    }

    // Save original signer
    originalSignerId = raw_decision.getSignerId();
    originalSigner = raw_decision.getSigner();
    originalSignerBlankText = raw_decision.getSignerBlankText();
    originalSignerAssistantId = raw_decision.getAssistantId();


    if ( rDecisionEntity != null &&
      rDecisionEntity.getSignerId() != null &&
      !rDecisionEntity.getSignerId().equals( settings2.getCurrentUserId() ) &&
      !settings.getBoolean("settings_view_show_approve_on_primary").get() ){

      // resolved https://tasks.n-core.ru/browse/MVDESD-13438
      // Добавить настройку наличия кнопки Согласовать в Первичном рассмотрении
      toolbar.getMenu().findItem(R.id.action_constructor_create_and_sign).setVisible(false);

    } else {
      toolbar.getMenu().findItem(R.id.action_constructor_create_and_sign).setVisible(true);
    }

  }

  private void invalidateSaveAndSignButton(){

    Timber.tag(TAG).e("invalidateSaveAndSignButton" );
    // resolved https://tasks.n-core.ru/browse/MVDESD-13438
    // При создании новой резолюции, кнопка "Сохранить и подписать"
    // должна быть только в том случае, если Подписант=текущему пользователю.
    // В остальных случаях, кнопки "Сохранить и подписать" быть не должно.
    if ( !settings.getBoolean("settings_view_show_approve_on_primary").get() && Objects.equals(settings.getString("_status").get(), "primary_consideration")){
      if (
          manager.getDecision() != null &&
            manager.getDecision().getSignerId() != null &&
          Objects.equals(manager.getDecision().getSignerId(), settings.getString("current_user_id").get())){
        toolbar.getMenu().findItem(R.id.action_constructor_create_and_sign).setVisible(true);
      } else {
        toolbar.getMenu().findItem(R.id.action_constructor_create_and_sign).setVisible(false);
      }
    }
  }

  private boolean checkDecision() {
    boolean showSaveDialog = true;

    if ( !manager.allSignersSet() ) {
      showSaveDialog = false;
      new MaterialDialog.Builder(this)
              .title("Внимание")
              .content("Укажите хотя бы одного исполнителя")
              .positiveText("Ок")
              .negativeText("Выход")
              .onPositive(
                      (dialog, which) -> {
                        dialog.dismiss();
                      }
              )
              .onNegative(
                      (dialog, which) -> {
                        finish();
                      }
              )
              .show();
    }

    if ( showSaveDialog && !manager.hasBlocks() ) {
      showSaveDialog = false;
      new MaterialDialog.Builder(this)
              .title("Внимание")
              .content("Необходимо добавить хотя бы один блок")
              .positiveText("Ок")
              .onPositive(
                      (dialog, which) -> {
                        dialog.dismiss();
                      }
              )
              .show();

    }

    if ( showSaveDialog && !manager.hasSigner() ) {
      showSaveDialog = false;
      new MaterialDialog.Builder(this)
              .title("Внимание")
              .content("Необходимо выбрать подписавшего")
              .positiveText("Ок")
              .onPositive(
                      (dialog, which) -> {
                        dialog.dismiss();
                      }
              )
              .show();
    }

    // Check if signer and performers are different persons
    if ( showSaveDialog && manager.hasBlocks() && manager.hasSigner() ) {
      Decision decision = manager.getDecision();
      if (decision != null) {
        String signerId = decision.getSignerId();
        String assistantId = decision.getAssistantId();
        boolean signerEqualsPerformer = false;
        List<Block> blocks = decision.getBlocks();

        for (Block block : blocks) {
          List<Performer> performers = block.getPerformers();
          for (Performer performer : performers) {
            String performerId = performer.getPerformerId();

            // fix null pointer exception
            if ( signerId!= null && performerId.equals(signerId) || assistantId != null && performerId.equals(assistantId) ) {
              signerEqualsPerformer = true;
              break;
            }
          }
          if (signerEqualsPerformer) {
            break;
          }
        }

        if (signerEqualsPerformer) {
          showSaveDialog = false;
          new MaterialDialog.Builder(this)
                  .title("Внимание")
                  .content("Подписавший и исполнитель совпадают")
                  .positiveText("Ок")
                  .onPositive(
                          (dialog, which) -> {
                            dialog.dismiss();
                          }
                  )
                  .show();
        }
      }
    }

    return showSaveDialog;
  }


//  @Override
//  public boolean dispatchTouchEvent(MotionEvent ev) {
//    View view = getCurrentFocus();
//    if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
//      int scrcoords[] = new int[2];
//      view.getLocationOnScreen(scrcoords);
//      float x = ev.getRawX() + view.getLeft() - scrcoords[0];
//      float y = ev.getRawY() + view.getTop() - scrcoords[1];
//      if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
//        hideKeyboard(this);
//    }
//    return super.dispatchTouchEvent(ev);
//  }

//  private void hideKeyboard(DecisionConstructorActivity constructorActivity) {
//    if( constructorActivity != null ){
//      ((InputMethodManager) constructorActivity
//        .getSystemService(Context.INPUT_METHOD_SERVICE))
//        .hideSoftInputFromWindow(
//          constructorActivity
//            .getWindow()
//            .getDecorView()
//            .getApplicationWindowToken(),
//          0);
//    }
//  }

  @Override
  protected void onResume() {
    super.onPostResume();


    operationManager.registerCallBack(this);
  }

  private void loadDecision() {
//    settings2.setDecisionActiveId( current_decision.getId() );
    Integer decision_id = settings2.getDecisionActiveId();

    rDecisionEntity = dataStore
      .select(RDecisionEntity.class)
      .where(RDecisionEntity.ID.eq(decision_id))
      .get().firstOrNull();

    if (rDecisionEntity != null) {
      raw_decision = new Decision();

      raw_decision.setId( rDecisionEntity.getUid() );
      raw_decision.setLetterhead(rDecisionEntity.getLetterhead());
      raw_decision.setApproved(rDecisionEntity.isApproved());
      raw_decision.setSigner(rDecisionEntity.getSigner());
      raw_decision.setSignerId(rDecisionEntity.getSignerId());
      raw_decision.setAssistantId(rDecisionEntity.getAssistantId());
      raw_decision.setSignerBlankText(rDecisionEntity.getSignerBlankText());
      raw_decision.setSignerIsManager(rDecisionEntity.isSignerIsManager());
      raw_decision.setComment(rDecisionEntity.getComment());
      raw_decision.setDate(rDecisionEntity.getDate());
      raw_decision.setUrgencyText(rDecisionEntity.getUrgencyText());
      raw_decision.setShowPosition(rDecisionEntity.isShowPosition());
      raw_decision.setLetterheadFontSize(rDecisionEntity.getLetterheadFontSize());
      raw_decision.setPerformersFontSize(rDecisionEntity.getPerformerFontSize());

      if ( rDecisionEntity.getBlocks() != null && rDecisionEntity.getBlocks().size() >= 1 ){

        ArrayList<Block> list = new ArrayList<>();

        for (RBlock _block: rDecisionEntity.getBlocks() ) {

          RBlockEntity b = (RBlockEntity) _block;
          Block block = new Block();
          block.setNumber(b.getNumber());
          block.setFontSize(b.getFontSize());
          block.setText(b.getText());
          block.setAppealText(b.getAppealText());
          block.setTextBefore(b.isTextBefore());
          block.setHidePerformers(b.isHidePerformers());
          block.setToCopy(b.isToCopy());
          block.setToFamiliarization(b.isToFamiliarization());

          if ( b.getPerformers() != null && b.getPerformers().size() >= 1 ) {

            for (RPerformer _performer : b.getPerformers()) {

              RPerformerEntity p = (RPerformerEntity) _performer;
              Performer performer = new Performer();

              performer.setNumber(p.getNumber());
              performer.setPerformerId(p.getPerformerId());
              performer.setPerformerType(p.getPerformerType());
              performer.setPerformerText(p.getPerformerText());
              performer.setPerformerGender(p.getPerformerGender());
              performer.setOrganizationText(p.getOrganizationText());
              performer.setIsOriginal(p.isIsOriginal());
              performer.setIsResponsible(p.isIsResponsible());
              performer.setOrganization(p.isIsOrganization());

              block.getPerformers().add(performer);
            }
          }

          Collections.sort(block.getPerformers(), (o1, o2) -> o1.getNumber().compareTo( o2.getNumber() ));
          list.add(block);
        }

        Collections.sort(list, (o1, o2) -> o1.getNumber().compareTo( o2.getNumber() ));
        raw_decision.setBlocks(list);
      }
    } else {

      SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
      Calendar cal = Calendar.getInstance();
      String date = dateFormat.format(cal.getTime());

      String signerName = getCurrentUserName();
      String signerOrganization = getCurrentUserOrganization();

      raw_decision = new Decision();
      raw_decision.setLetterhead("Бланк резолюции");
      raw_decision.setShowPosition(true);
      raw_decision.setSignerId( getCurrentUserId() );
      raw_decision.setSigner( makeSignerWithOrganizationText(signerName, signerOrganization) );
      raw_decision.setSignerBlankText( signerName );
      raw_decision.setUrgencyText("");
      raw_decision.setId(null);
      raw_decision.setDate( date );
      raw_decision.setBlocks(new ArrayList<>());

    }

  }

  @Override
  public void onFragmentInteraction(Uri uri) {
  }

  @Override
  public void onStart() {
    super.onStart();
  }
  @Override protected void onPause() {
    super.onPause();
  }

  @Override
  public void onStop() {
    super.onStop();
  }




  private void showPrevDialog() {
    // decision_assignment_approve_body

    MaterialDialog.Builder prev_dialog = new MaterialDialog.Builder(this)
      .content(R.string.decision_reject_body)
      .cancelable(true)
      .positiveText(R.string.yes)
      .negativeText(R.string.no)
      .onPositive((dialog1, which) -> {
        CommandFactory.Operation operation;
        operation =CommandFactory.Operation.REJECT_DECISION;

        CommandParams params = new CommandParams();
        params.setDecisionId( rDecisionEntity.getUid() );
//        params.setDecision( rDecisionEntity );
        params.setDecisionModel( DecisionConverter.formatDecision(rDecisionEntity) );

        operationManager.execute(operation, params);
      })
      .autoDismiss(true);

    prev_dialog.build().show();
  }

  private void showNextDialog() {

    MaterialDialog.Builder prev_dialog = new MaterialDialog.Builder(this)
      .content(R.string.decision_approve_body)
      .cancelable(true)
      .positiveText(R.string.yes)
      .negativeText(R.string.no)
      .onPositive((dialog1, which) -> {

        raw_decision.setApproved(true);

        CommandFactory.Operation operation;
        operation =CommandFactory.Operation.APPROVE_DECISION;

        CommandParams params = new CommandParams();
        params.setDecisionId( rDecisionEntity.getUid() );
//        params.setDecision( rDecisionEntity );
        params.setDecisionModel( DecisionConverter.formatDecision(rDecisionEntity) );

        if ( settings2.isDecisionWithAssignment() ){
          Timber.tag(TAG).w("ASSIGNMENT: %s", settings2.isDecisionWithAssignment() );
          params.setAssignment(true);
        }

        operationManager.execute(operation, params);
      })
      .autoDismiss(true);

    prev_dialog.build().show();
  }

  @Override
  public void onExecuteSuccess(String command) {
    if ( Objects.equals(command, "approve_decision") ) {
      finish();
//      activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
      EventBus.getDefault().post( new ApproveDecisionEvent() );
    }

    if ( Objects.equals(command, "reject_decision") ) {
      finish();
//      activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

      EventBus.getDefault().post( new RejectDecisionEvent() );
    }
  }


  @Override
  public void onExecuteError() {

  }

  @Override
  public void onSearchSuccess(Oshs user, CommandFactory.Operation operation, String uid) {
    Timber.tag(TAG).e("USER: %s", new Gson().toJson(user) );

    updateSigner(user.getId(), user.getName(), user.getOrganization(), user.getAssistantId());

    // resolved https://tasks.n-core.ru/browse/MVDESD-13438
    // Добавить настройку наличия кнопки Согласовать в Первичном рассмотрении
    if ( !settings.getBoolean("settings_view_show_approve_on_primary").get() ){
      invalidateSaveAndSignButton();
    }
  }

  @Override
  public void onSearchError(Throwable error) {

  }

  private void updateSigner(String signerId, String signerName, String signerOrganization, String assistantId) {

    String name = makeSignerWithOrganizationText(signerName, signerOrganization);

    if (rDecisionEntity != null) {
      rDecisionEntity.setSignerId( signerId );
      rDecisionEntity.setSigner( name );
      rDecisionEntity.setSignerBlankText( signerName );

      if ( assistantId != null ){
        rDecisionEntity.setAssistantId( assistantId );
      }
    }

    manager.setSignerId(signerId);
    manager.setSigner(name);
    manager.setSignerBlankText(signerName);

    if ( assistantId != null ){
      manager.setAssistantId(assistantId);
    }

    signer_oshs_selector.setText( name );

//    manager.setDecision( DecisionConverter.formatDecision(rDecisionEntity) );
    manager.update();
  }

  private String makeSignerWithOrganizationText(String signerName, String signerOrganization) {
    String name = signerName;

    if (!name.endsWith(")")){
      name = String.format("%s (%s)", name, signerOrganization );
    }

    return name;
  }

  private String getCurrentUserId() {
    return settings2.getCurrentUserId();
  }

  private String getCurrentUserName() {
    return settings2.getCurrentUser();
  }

  private String getCurrentUserOrganization() {
    return settings2.getCurrentUserOrganization();
  }

  @Override
  public void onSelectTemplate(String template) {

  }
}

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
import android.widget.ScrollView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.f2prateek.rx.preferences.Preference;
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
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.decisions.RBlock;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformer;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.decision.ApproveDecisionEvent;
import sapotero.rxtest.events.decision.RejectDecisionEvent;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.Performer;
import sapotero.rxtest.views.adapters.models.FontItem;
import sapotero.rxtest.views.adapters.models.UrgencyItem;
import sapotero.rxtest.views.custom.SpinnerWithLabel;
import sapotero.rxtest.views.dialogs.SelectOshsDialogFragment;
import sapotero.rxtest.views.fragments.DecisionFragment;
import sapotero.rxtest.views.fragments.DecisionPreviewFragment;
import sapotero.rxtest.views.managers.menu.OperationManager;
import sapotero.rxtest.views.managers.menu.factories.CommandFactory;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;
import sapotero.rxtest.views.managers.view.DecisionManager;
import timber.log.Timber;

public class DecisionConstructorActivity extends AppCompatActivity implements DecisionFragment.OnFragmentInteractionListener, DecisionPreviewFragment.OnFragmentInteractionListener, OperationManager.Callback, SelectOshsDialogFragment.Callback {

  @Inject RxSharedPreferences settings;
  @Inject OperationManager operationManager;
  @Inject SingleEntityStore<Persistable> dataStore;

  @BindView(R.id.toolbar) Toolbar toolbar;

//  @BindView(R.id.fragment_decision_autocomplete_field) DelayAutoCompleteTextView user_autocomplete;
//  @BindView(R.id.fragment_decision_autocomplete_field_loading_indicator) ProgressBar indicator;

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
  private final DecisionConstructorActivity activity = (DecisionConstructorActivity) this;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_decision_constructor);

    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    Preference<String> STATUS_CODE = settings.getString("activity_main_menu.star");
    status  = Fields.Status.findStatus( STATUS_CODE.get() );




    toolbar.setTitleTextColor( getResources().getColor( R.color.md_grey_100 ) );
    toolbar.setSubtitleTextColor( getResources().getColor( R.color.md_grey_400 ) );

    toolbar.setContentInsetStartWithNavigation(250);

    toolbar.setTitle("Редактор резолюции ");
    toolbar.inflateMenu(R.menu.info_decision_constructor);



    toolbar.setNavigationOnClickListener( v -> {


      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String json = gson.toJson( manager.getDecision() );
      Timber.tag(TAG).w("DECISION: %s", json );


      if ( manager.isChanged() ){
        Boolean showSaveDialog = true;

        if ( !manager.allSignersSet() ) {
          showSaveDialog = false;
          new MaterialDialog.Builder(this)
            .title("Внимание")
            .content("Укажите хотя бы одного подписавшего")
            .positiveText("Ок")
            .onPositive(
              (dialog, which) -> {
                dialog.dismiss();
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


        if (showSaveDialog){
          new MaterialDialog.Builder(this)
            .title("Имеются несохранненые данные")
            .content("Резолюция была изменена")
            .positiveText("сохранить")
            .onPositive(
              (dialog, which) -> {

                operationManager.registerCallBack(null);

//                manager.getDecisionBuilder().build();

                Decision decision = manager.getDecision();

                CommandParams params = new CommandParams();
                params.setDecisionModel( decision );

                decision.setDocumentUid( settings.getString("activity_main_menu.uid").get() );

                if (rDecisionEntity != null) {
                  params.setDecision( rDecisionEntity );
                  params.setDecisionId( rDecisionEntity.getUid() );
                }

                CommandFactory.Operation operation = rDecisionEntity == null ? CommandFactory.Operation.NEW_DECISION: CommandFactory.Operation.SAVE_DECISION;

                operationManager.execute( operation, params );

                finish();
//                activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

              }
            )
            .neutralText("выход")
            .onNeutral(
              (dialog, which) -> {
                Timber.tag(TAG).w("nothing");
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

      switch (item.getItemId()){
        case R.id.action_constructor_add_block:
          manager.getDecisionBuilder().addBlock();

//          scroll.fullScroll(ScrollView.FOCUS_DOWN);

          break;

        case R.id.action_constructor_next:
          // настройка
          // Показывать подтверждения о действиях с документом
          if ( settings.getBoolean("settings_view_show_actions_confirm").get() ){
            showNextDialog();
          } else {

            operationManager.registerCallBack(this);

            CommandFactory.Operation operation;
            operation =CommandFactory.Operation.APPROVE_DECISION;

            CommandParams params = new CommandParams();
            params.setDecisionId( rDecisionEntity.getUid() );
            params.setDecision( rDecisionEntity );

            operationManager.execute(operation, params);
          }

          break;
        case R.id.action_constructor_prev:

          // настройка
          // Показывать подтверждения о действиях с документом
          if ( settings.getBoolean("settings_view_show_actions_confirm").get() ){
            showPrevDialog();
          } else {
            operationManager.registerCallBack(this);

            CommandFactory.Operation operation;
            operation =CommandFactory.Operation.REJECT_DECISION;

            CommandParams params = new CommandParams();
            params.setDecisionId( rDecisionEntity.getUid() );
            params.setDecision( rDecisionEntity );

            operationManager.execute(operation, params);
          }

          break;

        default:
          break;
      }

      return false;
    });

    List<UrgencyItem> urgency = new ArrayList<>();


    // настройка
    if (!settings.getBoolean("settings_view_show_urgency").get()){
      urgency_selector.setVisibility(View.GONE);
    }
    // настройка
    if (settings.getBoolean("settings_view_only_urgent").get()){
      urgency.add(new UrgencyItem("Нет", ""));
      urgency.add(new UrgencyItem("Срочно", "Срочно"));
      urgency_selector.setVisibility(View.VISIBLE);
    } else {
      urgency.add(new UrgencyItem("Весьма срочно", "Весьма срочно"));
      urgency.add(new UrgencyItem("Крайне срочно", "Крайне срочно"));
      urgency.add(new UrgencyItem("Няшная срочность", "Няшная срочность"));
      urgency.add(new UrgencyItem("Очень срочно", "Очень срочно"));
      urgency.add(new UrgencyItem("Срочно", "Срочно"));
    }




    urgency_selector.setItems(urgency);
    urgency_selector.setOnItemSelectedListener((item, selectedIndex) -> {
      manager.setUrgency( item.getLabel() );
    });



    // настройка
    if (settings.getBoolean("settings_view_show_decision_change_font").get()){
      List<FontItem> fonts = new ArrayList<>();
      fonts.add(new FontItem("12", "12"));
      fonts.add(new FontItem("13", "13"));
      fonts.add(new FontItem("14", "14"));
      fonts.add(new FontItem("15", "15"));
      fonts.add(new FontItem("16", "16"));

      font_selector.setItems(fonts);
      font_selector.setOnItemSelectedListener((item, selectedIndex) -> {
        Timber.e("%s - %s", item.getLabel(), item.getValue());
      });
    } else {
      font_selector.setVisibility(View.GONE);
    }



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


    Oshs decision_signer = new Oshs();
    decision_signer.setId( settings.getString("current_user_id").get() );
    decision_signer.setName( settings.getString("current_user").get() );

    sign_as_current_user.setOnClickListener(v -> {
      if (rDecisionEntity != null) {
        rDecisionEntity.setSignerId( settings.getString("current_user_id").get() );
        rDecisionEntity.setSigner( settings.getString("current_user").get() );
        signer_oshs_selector.setText( rDecisionEntity.getSigner() );
      } else {
        signer_oshs_selector.setText( raw_decision.getSigner() );
      }
    });

    if ( rDecisionEntity != null ){
      decision_signer.setId( rDecisionEntity.getSignerId() );
      decision_signer.setName( rDecisionEntity.getSigner() );
      decision_comment.setText( rDecisionEntity.getComment() );
      signer_oshs_selector.setText( rDecisionEntity.getSigner() );
    } else {
      raw_decision.setSignerId( settings.getString("current_user_id").get() );
      raw_decision.setSigner( settings.getString("current_user").get() );
      signer_oshs_selector.setText( raw_decision.getSigner() );
    }

    if ( raw_decision.getUrgencyText() != null ){
      urgency_selector.setSelection( 0 );
      manager.setUrgency("");
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
      select_oshs_wrapper.setVisibility(View.GONE);
    }

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

  }

  @Override
  protected void onResume() {
    super.onPostResume();

    operationManager.registerCallBack(null);
    operationManager.registerCallBack(this);
  }

  private void loadDecision() {
//    settings.getInteger("decision.active.id").set( current_decision.getId() );
    Integer decision_id = settings.getInteger("decision.active.id").get();

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

      Timber.tag(TAG).e("getUrgencyText: %s", rDecisionEntity.getUrgencyText() );

      if ( rDecisionEntity.getBlocks() != null && rDecisionEntity.getBlocks().size() >= 1 ){

        ArrayList<Block> list = new ArrayList<>();

        for (RBlock _block: rDecisionEntity.getBlocks() ) {

          RBlockEntity b = (RBlockEntity) _block;
          Block block = new Block();
          block.setNumber(b.getNumber());
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
              performer.setOrganizationText(p.getOrganizationText());
              performer.setIsOriginal(p.isIsOriginal());
              performer.setIsResponsible(p.isIsResponsible());

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

      raw_decision = new Decision();
      raw_decision.setLetterhead("Бланк резолюции");
      raw_decision.setShowPosition(true);
      raw_decision.setSignerId( settings.getString("current_user_id").get() );
      raw_decision.setSigner( settings.getString("current_user").get() );
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
        params.setDecision( rDecisionEntity );

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
        params.setDecisionId( settings.getString("decision.active.id").get() );
        params.setDecision( rDecisionEntity );

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
  public void onSearchSuccess(Oshs user, CommandFactory.Operation operation) {
    Timber.tag(TAG).e("USER: %s", new Gson().toJson(user) );

    if (rDecisionEntity != null) {
      rDecisionEntity.setSignerId( user.getId() );
      rDecisionEntity.setSigner( user.getName() );
    }

    if ( user.getAssistantId() != null ){
      rDecisionEntity.setAssistantId( user.getAssistantId() );
    }

    manager.setSignerId(user.getId());
    manager.setSigner(user.getName());

    signer_oshs_selector.setText( user.getName() );

//    manager.setDecision( DecisionConverter.formatDecision(rDecisionEntity) );
    manager.update();
  }

  @Override
  public void onSearchError(Throwable error) {

  }
}

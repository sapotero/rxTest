package sapotero.rxtest.views.managers.toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RFolderEntity;
import sapotero.rxtest.db.requery.models.decisions.RDecision;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.crypto.SignDataEvent;
import sapotero.rxtest.events.decision.ApproveDecisionEvent;
import sapotero.rxtest.events.decision.RejectDecisionEvent;
import sapotero.rxtest.events.decision.ShowDecisionConstructor;
import sapotero.rxtest.events.view.RemoveDocumentFromAdapterEvent;
import sapotero.rxtest.events.view.ShowSnackEvent;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.views.activities.DecisionConstructorActivity;
import sapotero.rxtest.views.dialogs.SelectOshsDialogFragment;
import sapotero.rxtest.views.managers.menu.OperationManager;
import sapotero.rxtest.views.managers.menu.factories.CommandFactory;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class ToolbarManager  implements SelectOshsDialogFragment.Callback {

  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject RxSharedPreferences settings;
  @Inject OperationManager operationManager;

  private final String TAG = this.getClass().getSimpleName();

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> PASSWORD;
  private Preference<String> UID;
  private Preference<String> DOCUMENT_UID;
  private Preference<String> STATUS_CODE;
  private Preference<Integer> POSITION;
  private Preference<String> REG_NUMBER;
  private Preference<String> REG_DATE;

  private final Context context;
  private final Toolbar toolbar;
  private String SIGN;
  private Fields.Status status;
  private Fields.Journal journal;

  private MaterialDialog dialog;

  private SelectOshsDialogFragment oshs;
  private int decision_count;
  private RDocumentEntity doc;
  private Preference<String> CURRENT_USER_ID;

  // FIX переделать
  // пока глобальная переменная чтобы иметь возможность в диалогах
  // использовать операцию с параметрами без заморочек
//  private CommandFactory.Operation operation;

  public ToolbarManager(Context context, Toolbar toolbar) {
    this.context = context;
    this.toolbar = toolbar;
    EsdApplication.getComponent(context).inject(this);


    loadSettings();

    setListener();

    buildDialog();

    // FIX починить и убрать из релиза
    getFirstForLenovo();

  }

  private void getFirstForLenovo() {
    doc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(UID.get())).get().first();
  }

  private void setListener() {
    final Activity activity = (Activity) context;

    toolbar.setOnMenuItemClickListener(
      item -> {

        CommandFactory.Operation operation;
        CommandParams params = new CommandParams();
        params.setUser( LOGIN.get() );

        switch ( item.getItemId() ){
          // sent_to_the_report (отправлен на доклад)
//          case R.id.menu_info_from_the_report:
//            operation = CommandFactory.Operation.FROM_THE_REPORT;
//            break;
          case R.id.menu_info_to_the_primary_consideration:

            Timber.v("primary_consideration");

            SelectOshsDialogFragment dialogFragment = new SelectOshsDialogFragment();
            Bundle bundle1 = new Bundle();
            bundle1.putString("operation", "primary_consideration");
            dialogFragment.setArguments(bundle1);
            dialogFragment.withPrimaryConsideration(true);
            dialogFragment.withoutSearch(true);
            dialogFragment.registerCallBack( this );
            dialogFragment.show( activity.getFragmentManager(), "SelectOshsDialogFragment");

            operation = CommandFactory.Operation.INCORRECT;
            break;

          // sent_to_the_report (отправлен на доклад)
          case R.id.menu_info_delegate_performance:
            operation = CommandFactory.Operation.DELEGATE_PERFORMANCE;
            params.setPerson( settings.getString("current_user_id").get() );
            break;
          case R.id.menu_info_to_the_approval_performance:

            // настройка
            // Показывать подтверждения о действиях с документом
            if ( settings.getBoolean("settings_view_show_actions_confirm").get() ){
              operation = CommandFactory.Operation.INCORRECT;
              showFromTheReportDialog();
            } else {
              operation = CommandFactory.Operation.FROM_THE_REPORT;
              params.setPerson( settings.getString("current_user_id").get() );
            }
            break;

          // primary_consideration (первичное рассмотрение)
          case R.id.menu_info_approval_next_person:

            // настройка
            // Показывать подтверждения о действиях с документом
            if ( settings.getBoolean("settings_view_show_actions_confirm").get() ){
              operation = CommandFactory.Operation.INCORRECT;
              showNextDialog(false);
            } else {
              operation = CommandFactory.Operation.APPROVAL_NEXT_PERSON;
              params.setSign( "SIGN" );
            }
//
            break;
          case R.id.menu_info_approval_prev_person:
            // настройка
            // Показывать подтверждения о действиях с документом
            if ( settings.getBoolean("settings_view_show_actions_confirm").get() ){
              operation = CommandFactory.Operation.INCORRECT;
              showPrevDialog(true);
            } else {
              operation = CommandFactory.Operation.APPROVAL_PREV_PERSON;
              params.setSign( "SIGN" );
            }
            break;


          // approval (согласование проектов документов)
          case R.id.menu_info_approval_change_person:
            operation = CommandFactory.Operation.INCORRECT;

            SelectOshsDialogFragment approveDialogFragment = new SelectOshsDialogFragment();
            Bundle approveBundle = new Bundle();
            approveBundle.putString("operation", "approve");
            approveDialogFragment.setArguments(approveBundle);
            approveDialogFragment.withPrimaryConsideration(true);
            approveDialogFragment.withoutSearch(true);
            approveDialogFragment.registerCallBack( this );
            approveDialogFragment.show( activity.getFragmentManager(), "SelectOshsDialogFragment");
//
            break;

          case R.id.menu_info_sign_change_person:
          // operation = CommandFactory.Operation.APPROVAL_CHANGE_PERSON;
          operation = CommandFactory.Operation.INCORRECT;

            SelectOshsDialogFragment sign = new SelectOshsDialogFragment();
            Bundle signBundle = new Bundle();
            signBundle.putString("operation", "sign");
            sign.setArguments(signBundle);
            sign.withPrimaryConsideration(true);
            sign.withoutSearch(true);
            sign.registerCallBack( this );
            sign.show( activity.getFragmentManager(), "SelectOshsDialogFragment");

          break;

          case R.id.menu_info_sign_next_person:

            // настройка
            // Показывать подтверждения о действиях с документом
            if ( settings.getBoolean("settings_view_show_actions_confirm").get() ){
              operation = CommandFactory.Operation.INCORRECT;
              showNextDialog(true);
            } else {
              operation = CommandFactory.Operation.SIGNING_NEXT_PERSON;
              params.setSign( "SIGN" );
            }


            break;
          case R.id.menu_info_sign_prev_person:

            // настройка
            // Показывать подтверждения о действиях с документом
            if ( settings.getBoolean("settings_view_show_actions_confirm").get() ){
              operation = CommandFactory.Operation.INCORRECT;
              showPrevDialog(false);
            } else {
              operation = CommandFactory.Operation.SIGNING_PREV_PERSON;
              params.setSign( "SIGN" );
            }

            break;

          case R.id.menu_info_decision_create:
            operation = CommandFactory.Operation.INCORRECT;

            settings.getString("decision.active.id").set(null);

            Intent create_intent = new Intent(context, DecisionConstructorActivity.class);
            activity.startActivity(create_intent);
//            activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

            break;

          case R.id.menu_info_decision_edit:
            EventBus.getDefault().post( new ShowDecisionConstructor() );

//            intent.putExtra("decision", json);

            operation = CommandFactory.Operation.INCORRECT;
//            operation = CommandFactory.Operation.NEW_DECISION;
//
//            Intent edit_intent = new Intent(context, DecisionConstructorActivity.class);
//            context.startActivity(edit_intent);

            break;
          case R.id.menu_info_shared_to_favorites:
            operation = !doc.isFavorites() ? CommandFactory.Operation.ADD_TO_FOLDER: CommandFactory.Operation.REMOVE_FROM_FOLDER;

            String favorites = dataStore
              .select(RFolderEntity.class)
              .where(RFolderEntity.TYPE.eq("favorites"))
              .get().first().getUid();

            params.setFolder(favorites);
            params.setDocument( UID.get() );


            break;
          case R.id.menu_info_shared_to_control:
            // настройка
            // Показывать подтверждения о постановке на контроль документов для раздела «Обращение граждан»

            if ( settings.getBoolean("settings_view_show_control_confirm").get() && UID.get().startsWith( Fields.Journal.CITIZEN_REQUESTS.getValue() ) ){
              operation = CommandFactory.Operation.INCORRECT;

              showToControlDialog();

            } else {
              operation = CommandFactory.Operation.CHECK_FOR_CONTROL;
              params.setDocument( UID.get() );
            }
            break;


          default:
            operation = CommandFactory.Operation.INCORRECT;
            break;
        }

        operationManager.execute( operation, params );

        return false;
      }
    );
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
    REG_DATE = settings.getString("activity_main_menu.date");
    CURRENT_USER_ID = settings.getString("current_user_id");
  }

  private void invalidate() {
    Timber.tag(TAG).v("invalidate");

    getFirstForLenovo();

//    Timber.tag(TAG).v("invalidate: %s", new Gson().toJson(doc) );


    // проверяем, сколько есть резолюций у документа
    // если она одна, то показываем кнопки Подписать/Отклонить
    // если несколько, то показываем редактировать - для редактирования текущей
    // если нет - то показываем кнопку создать

    decision_count = doc.getDecisions().size();

    switch (decision_count) {
      case 0:
        processEmptyDecisions();
        break;
      default:
        try {
          toolbar.getMenu().findItem(R.id.menu_info_decision_create).setVisible(false);
          toolbar.getMenu().findItem(R.id.menu_info_decision_edit).setVisible(false);
        } catch (Exception e) {
          Timber.tag(TAG).v(e);
        }
        break;
    }

    // Если документ обработан - то изменяем резолюции на поручения
    if ( doc.isProcessed() != null || doc.isFromProcessedFolder() != null ) {
      try {
        toolbar.getMenu().findItem(R.id.menu_info_decision_edit).setVisible(false);
        toolbar.getMenu().findItem(R.id.menu_info_decision_create).setVisible(true);
      } catch (Exception e) {
        Timber.tag(TAG).v(e);
      }
    }

    for (int i = 0; i < toolbar.getMenu().size(); i++) {
      MenuItem item = toolbar.getMenu().getItem(i);

      switch (item.getItemId()) {
        case R.id.menu_info_shared_to_favorites:
          item.setTitle(context.getString(doc.isFavorites() != null && doc.isFavorites() ? R.string.remove_from_favorites : R.string.to_favorites));
          break;
        case R.id.menu_info_shared_to_control:
          item.setTitle(context.getString(doc.isControl() != null && doc.isControl() ? R.string.remove_from_control : R.string.to_control));
          break;
        default:
          break;
      }
    }



    if (doc!= null && doc.isFromFavoritesFolder() != null && doc.isFromFavoritesFolder() ){
      toolbar.getMenu().clear();
      toolbar.inflateMenu(R.menu.info_menu);

      try {
        toolbar.getMenu().findItem(R.id.menu_info_decision_edit).setVisible(false);
      } catch (Exception e) {
        e.printStackTrace();
      }


    }
    //настройка
    try {
      if (!settings.getBoolean("settings_view_show_create_decision_post").get() && doc.isFromFavoritesFolder() != null && doc.isFromFavoritesFolder() ) {
        if ( doc.isFromFavoritesFolder() != null && !doc.isFromFavoritesFolder() ){
          toolbar.getMenu().findItem(R.id.menu_info_decision_create).setVisible(false);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (Objects.equals(doc.getFilter(), Fields.Status.SIGNING.getValue()) || Objects.equals(doc.getFilter(), Fields.Status.APPROVAL.getValue()) || doc.isFromFavoritesFolder() != null && doc.isFromFavoritesFolder() ) {
      // resolved https://tasks.n-core.ru/browse/MVDESD-12765
      // убрать кнопку "К" у проектов из раздела на согласование("на подписание" её также быть не должно)
      try {
        toolbar.getMenu().findItem(R.id.menu_info_shared_to_control).setVisible(false);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  //REFACTOR переделать это
  private void processEmptyDecisions() {
    try {
      toolbar.getMenu().findItem( R.id.menu_info_decision_edit  ).setVisible(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      toolbar.getMenu().findItem( R.id.menu_info_decision_create).setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setEditDecisionMenuItemVisible(boolean visible){
    Timber.e("setEditDecisionMenuItemVisible %s", visible);
    try {
      if (visible){
        toolbar.getMenu().findItem( R.id.menu_info_decision_edit).setVisible(false);
      } else {
        toolbar.getMenu().findItem( R.id.menu_info_decision_edit).setVisible(false);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void update(String command){

    Timber.tag(TAG).w("update %s", command );

    if ( Objects.equals(command, "check_for_control") ) {
      EventBus.getDefault().post( new ShowSnackEvent("Отметки для постановки на контроль успешно обновлены.") );
    }

    if ( Objects.equals(command, "change_person") ) {
      toolbar.getMenu().clear();
      toolbar.inflateMenu(R.menu.info_menu);
      EventBus.getDefault().post( new ShowSnackEvent("Операция передачи успешно завершена") );
      if ( !doc.isProcessed() ){
        EventBus.getDefault().postSticky( new RemoveDocumentFromAdapterEvent( UID.get() ) );
      }
    }

    if ( Objects.equals(command, "next_person") ) {
      toolbar.getMenu().clear();
      toolbar.inflateMenu(R.menu.info_menu);
      EventBus.getDefault().post( new ShowSnackEvent("Операция подписания успешно завершена") );
      if ( !doc.isProcessed() ){
        EventBus.getDefault().postSticky( new RemoveDocumentFromAdapterEvent( UID.get() ) );
      }
    }

    if ( Objects.equals(command, "prev_person") ) {
      toolbar.getMenu().clear();
      toolbar.inflateMenu(R.menu.info_menu);
      EventBus.getDefault().post( new ShowSnackEvent("Операция отклонения успешно завершена") );
      if ( !doc.isProcessed() ){
        EventBus.getDefault().postSticky( new RemoveDocumentFromAdapterEvent( UID.get() ) );
      }
    }

    if ( Objects.equals(command, "to_the_primary_consideration") ) {
      toolbar.getMenu().clear();
      toolbar.inflateMenu(R.menu.info_menu);
      EventBus.getDefault().post( new ShowSnackEvent("Операция передачи первичного рассмотрения успешно завершена") );
      if ( !doc.isProcessed() ){
        EventBus.getDefault().postSticky( new RemoveDocumentFromAdapterEvent( UID.get() ) );
      }
    }


    if ( Objects.equals(command, "approve_decision") ) {

      if ( !hasActiveDecision() ){
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.info_menu);
      } else {
        invalidate();
      }
//      EventBus.getDefault().post( new ShowSnackEvent("Резолюция утверждена") );
      EventBus.getDefault().post( new ApproveDecisionEvent() );
    }

    if ( Objects.equals(command, "reject_decision") ) {

      if ( !hasActiveDecision() ){
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.info_menu);
      }
//      EventBus.getDefault().post( new ShowSnackEvent("Резолюция отклонена") );
      EventBus.getDefault().post( new RejectDecisionEvent() );
    }

    if ( Objects.equals(command, "from_the_report") ) {
      toolbar.getMenu().clear();
      toolbar.inflateMenu(R.menu.info_menu);
      EventBus.getDefault().post( new ShowSnackEvent("Операция исполнения без ответа успешно завершена") );
      EventBus.getDefault().postSticky( new RemoveDocumentFromAdapterEvent( UID.get() ) );
    }








    invalidate();

  }

  private Boolean hasActiveDecision(){
    RDocumentEntity doc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq( UID.get() ))
      .get().firstOrNull();

    Boolean result = false;

    if (doc != null && doc.getDecisions().size() > 0){
      for (RDecision _decision : doc.getDecisions()){
        RDecisionEntity decision = (RDecisionEntity) _decision;

        if (!decision.isApproved() && Objects.equals(decision.getSignerId(), CURRENT_USER_ID.get())){
          result = true;
        }
      }
    }

    Timber.tag(TAG).e("hasActiveDecision : %s", result);

    return result;
  }


  public void add( int id, String title ){
    try {
      toolbar.getMenu().add(Menu.NONE, id, Menu.NONE ,title);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void remove( int id ){
    try {
      toolbar.getMenu().removeItem(id);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  private void buildDialog() {
    dialog = new MaterialDialog.Builder( context )
      .title(R.string.app_name)
      .cancelable(false)
      .customView( R.layout.dialog_pin_check, true )
      .positiveText("OK")
      .autoDismiss(false)

      .onPositive( (dialog, which) -> {
        try {
          EditText pass = (EditText) this.dialog.getCustomView().findViewById(R.id.dialog_pin_password);
          pass.setVisibility(View.GONE);

          this.dialog.getCustomView().findViewById(R.id.dialog_pin_progress).setVisibility(View.VISIBLE);
          dialog.getActionButton(DialogAction.POSITIVE).setVisibility(View.GONE);

          EventBus.getDefault().post( new SignDataEvent( pass.getText().toString() ) );
        } catch (Exception e) {
          e.printStackTrace();
        }
      }).build();
  }

  public void init() {

    toolbar.setTitleTextColor( context.getResources().getColor( R.color.md_grey_100 ) );
    toolbar.setSubtitleTextColor( context.getResources().getColor( R.color.md_grey_400 ) );

    toolbar.setContentInsetStartWithNavigation(250);

    toolbar.setNavigationOnClickListener(v ->{
      Activity activity = (Activity) context;
      activity.finish();
//      activity.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
//      activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
      }
    );

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
      case "processed":
        menu = R.menu.info_menu;
        break;

      default:
        menu = R.menu.info_menu;
        break;
    }
    toolbar.inflateMenu(menu);


    status  = Fields.Status.findStatus(STATUS_CODE.get());
    journal = Fields.getJournalByUid( UID.get() );

    toolbar.setTitle( String.format("%s от %s", REG_NUMBER.get(), REG_DATE.get()) );

    Timber.tag("MENU").e( "STATUS CODE: %s", STATUS_CODE.get() );

    invalidate();

  }

  public void showCreateDecisionButton() {
    try {
      toolbar.getMenu().findItem( R.id.menu_info_decision_create).setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void hideDialog() {
    if (dialog != null) {
      dialog.hide();
    }
  }

  private void showToControlDialog() {
    Boolean isControl = false;

    if ( doc.isControl() != null && doc.isControl() ){
      isControl = true;
    }
    new MaterialDialog.Builder( context )
      .title(   isControl ? R.string.dialog_to_control_negative      : R.string.dialog_to_control_positive      )
      .content( isControl ? R.string.dialog_to_control_body_negative : R.string.dialog_to_control_body_positive )
      .cancelable(true)
      .positiveText(R.string.approve)
      .negativeText(R.string.cancel)
      .onPositive((dialog1, which) -> {
        CommandFactory.Operation operation = CommandFactory.Operation.CHECK_FOR_CONTROL;

        CommandParams params = new CommandParams();
        params.setUser( LOGIN.get() );
        params.setDocument( UID.get() );

        operationManager.execute( operation, params );
      })
      .autoDismiss(true)
      .build().show();

  }

  // Подписание/Согласование
  private void showNextDialog(Boolean isApproval) {
    new MaterialDialog.Builder( context )
      .content( isApproval ? R.string.dialog_approve_body : R.string.dialog_sign_body)
      .cancelable(true)
      .positiveText(R.string.yes)
      .negativeText(R.string.no)
      .onPositive((dialog1, which) -> {

        CommandFactory.Operation operation;
        operation = isApproval ? CommandFactory.Operation.APPROVAL_NEXT_PERSON: CommandFactory.Operation.SIGNING_NEXT_PERSON;

        CommandParams params = new CommandParams();
        params.setUser( LOGIN.get() );
        params.setDocument( UID.get() );
        params.setSign( "Sign" );

        operationManager.execute( operation, params );
      })
      .autoDismiss(true)
      .build().show();
  }

  private void showPrevDialog(Boolean isApproval) {
    String comment = "";
    CommandParams params = new CommandParams();

    MaterialDialog.Builder prev_dialog = new MaterialDialog.Builder(context)
      .content(R.string.dialog_reject_body)
      .cancelable(true)
      .positiveText(R.string.yes)
      .negativeText(R.string.no)
      .onPositive((dialog1, which) -> {

        CommandFactory.Operation operation;
        operation = isApproval ? CommandFactory.Operation.APPROVAL_PREV_PERSON : CommandFactory.Operation.SIGNING_PREV_PERSON;


        params.setUser(LOGIN.get());
        params.setSign("Sign");

        // если есть комментарий
        if (settings.getString("prev_dialog_comment").get() != null) {
          params.setComment("Sign");
        }

        params.setComment( dialog1.getInputEditText().getText().toString() );
        operationManager.execute(operation, params);
      })
      .autoDismiss(true);

      // настройка
      // Показывать комментарий при отклонении
      if ( settings.getBoolean("settings_view_show_comment_post").get() ){
        prev_dialog.inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES )
          .input(R.string.comment_hint, R.string.dialog_empty_value, (dialog12, input) -> {
            settings.getString("prev_dialog_comment").set( input.toString() );
            params.setComment( input.toString() );
          });
      }


    prev_dialog.build().show();
  }


  private void showFromTheReportDialog() {

    CommandParams params = new CommandParams();

    MaterialDialog.Builder fromTheReportDialog = new MaterialDialog.Builder(context)
      .content(R.string.document_from_the_report)
      .cancelable(true)
      .positiveText(R.string.yes)
      .negativeText(R.string.no)
      .onPositive((dialog1, which) -> {

        CommandFactory.Operation operation;

        operation = CommandFactory.Operation.FROM_THE_REPORT;
        params.setPerson( settings.getString("current_user_id").get() );
        params.setComment( dialog1.getInputEditText().getText().toString() );

        operationManager.execute(operation, params);
      })
      .autoDismiss(true);

    // настройка
    // Показывать комментарий при отклонении
    if ( settings.getBoolean("settings_view_show_comment_post").get() ){
      fromTheReportDialog.inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES )
        .input(R.string.comment_hint, R.string.dialog_empty_value, (dialog12, input) -> {
          settings.getString("prev_dialog_comment").set( input.toString() );
          params.setComment( input.toString() );
        });
    }


    fromTheReportDialog.build().show();
  }

  // OSHS selector
  @Override
  public void onSearchSuccess(Oshs user, CommandFactory.Operation operation) {
    CommandParams params = new CommandParams();
    params.setPerson( user.getId() );
    operationManager.execute( operation, params );
  }

  @Override
  public void onSearchError(Throwable error) {

  }

}

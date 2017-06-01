package sapotero.rxtest.managers.toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
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
import sapotero.rxtest.db.requery.models.images.RImage;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.crypto.SignDataEvent;
import sapotero.rxtest.events.decision.ShowDecisionConstructor;
import sapotero.rxtest.events.view.ShowSnackEvent;
import sapotero.rxtest.managers.menu.OperationManager;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.views.activities.DecisionConstructorActivity;
import sapotero.rxtest.views.dialogs.SelectOshsDialogFragment;
import timber.log.Timber;

public class ToolbarManager  implements SelectOshsDialogFragment.Callback, OperationManager.Callback {

  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject Settings settings;
  @Inject OperationManager operationManager;

  private final String TAG = this.getClass().getSimpleName();

  private int decision_count;

  private final Toolbar toolbar;
  private final Context context;

  private RDocumentEntity doc;
  private MaterialDialog dialog;

  public ToolbarManager (Context context, Toolbar toolbar) {
    this.context = context;
    this.toolbar = toolbar;
    EsdApplication.getManagerComponent().inject(this);

    setListener();

    buildDialog();

    operationManager.registerCallBack(this);

    // FIX починить и убрать из релиза
    getFirstForLenovo();

  }

  private void getFirstForLenovo() {
    doc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(settings.getUid())).get().first();
  }

  private void setListener() {
    final Activity activity = (Activity) context;

    toolbar.setOnMenuItemClickListener(
      item -> {

        CommandFactory.Operation operation;
        CommandParams params = new CommandParams();
        params.setUser( settings.getLogin() );
        params.setDocument( settings.getUid() );

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
            dialogFragment.withSearch(false);
            dialogFragment.withConfirm(true);
            dialogFragment.withChangePerson(true);
            dialogFragment.registerCallBack( this );
            dialogFragment.withDocumentUid( settings.getUid() );
            dialogFragment.show( activity.getFragmentManager(), "SelectOshsDialogFragment");

            operation = CommandFactory.Operation.INCORRECT;
            break;

          // sent_to_the_report (отправлен на доклад)
          case R.id.menu_info_delegate_performance:
            operation = CommandFactory.Operation.DELEGATE_PERFORMANCE;
            params.setPerson( settings.getCurrentUserId() );
            break;
          case R.id.menu_info_to_the_approval_performance:

            // настройка
            // Показывать подтверждения о действиях с документом
            if ( settings.isActionsConfirm() ){
              operation = CommandFactory.Operation.INCORRECT;
              showFromTheReportDialog();
            } else {
              operation = CommandFactory.Operation.FROM_THE_REPORT;
              params.setPerson( settings.getCurrentUserId() );
            }
            break;

          // primary_consideration (первичное рассмотрение)
          case R.id.menu_info_approval_next_person:

            // настройка
            // Показывать подтверждения о действиях с документом
            if ( settings.isActionsConfirm() ){
              operation = CommandFactory.Operation.INCORRECT;
              showNextDialog(false);
            } else {
              operation = CommandFactory.Operation.APPROVAL_NEXT_PERSON;
            }
//
            break;
          case R.id.menu_info_approval_prev_person:
            // настройка
            // Показывать подтверждения о действиях с документом
            if ( settings.isActionsConfirm() ){
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
            approveDialogFragment.withSearch(true);
            approveDialogFragment.withConfirm( true );
            approveDialogFragment.withPrimaryConsideration(false);
            approveDialogFragment.withChangePerson(true);
            approveDialogFragment.registerCallBack( this );
            approveDialogFragment.withDocumentUid( settings.getUid() );
            approveDialogFragment.show( activity.getFragmentManager(), "SelectOshsDialogFragment");
//
            break;

          case R.id.menu_info_sign_change_person:
          operation = CommandFactory.Operation.INCORRECT;

            SelectOshsDialogFragment sign = new SelectOshsDialogFragment();
            Bundle signBundle = new Bundle();
            signBundle.putString("operation", "sign");
            sign.setArguments(signBundle);
            sign.withSearch(true);
            sign.withConfirm( true );
            sign.withPrimaryConsideration(false);
            sign.withChangePerson(true);
            sign.registerCallBack( this );
            sign.withDocumentUid( settings.getUid() );
            sign.show( activity.getFragmentManager(), "SelectOshsDialogFragment");

          break;

          case R.id.menu_info_sign_next_person:

            //проверим что все образы меньше 25Мб
            if ( checkImagesSize() ){

              // настройка
              // Показывать подтверждения о действиях с документом
              if ( settings.isActionsConfirm() ){
                operation = CommandFactory.Operation.INCORRECT;
                showNextDialog(true);
              } else {
                operation = CommandFactory.Operation.SIGNING_NEXT_PERSON;
              }

            } else {



              new MaterialDialog.Builder(context)
                .title("Внимание!")
                .content("Электронный образ превышает максимально допустимый размер и не может быть подписан!")
                .positiveText("Продолжить")
                .icon(ContextCompat.getDrawable(context, R.drawable.attention))
                .show();

              operation = CommandFactory.Operation.INCORRECT;
            }

            break;
          case R.id.menu_info_sign_prev_person:

            // настройка
            // Показывать подтверждения о действиях с документом
            if ( settings.isActionsConfirm() ){
              operation = CommandFactory.Operation.INCORRECT;
              showPrevDialog(false);
            } else {
              operation = CommandFactory.Operation.SIGNING_PREV_PERSON;
            }

            break;

          case R.id.menu_info_decision_create:
            operation = CommandFactory.Operation.INCORRECT;

            settings.setDecisionActiveId(0);

            Intent create_intent = new Intent(context, DecisionConstructorActivity.class);
            activity.startActivity(create_intent);

            break;

          case R.id.menu_info_decision_edit:
            EventBus.getDefault().post( new ShowDecisionConstructor() );
            operation = CommandFactory.Operation.INCORRECT;
            break;
          case R.id.menu_info_shared_to_favorites:

            operation = CommandFactory.Operation.ADD_TO_FOLDER;
            if ( isFromFavorites() ){
             operation = CommandFactory.Operation.REMOVE_FROM_FOLDER;
            }

            String favorites = dataStore
              .select(RFolderEntity.class)
              .where(RFolderEntity.TYPE.eq("favorites"))
              .get().first().getUid();

            params.setFolder(favorites);
            params.setDocument( settings.getUid() );


            break;
          case R.id.menu_info_shared_to_control:
            // настройка
            // Показывать подтверждения о постановке на контроль документов для раздела «Обращение граждан»

            if ( settings.isControlConfirm() && settings.getUid().startsWith( Fields.Journal.CITIZEN_REQUESTS.getValue() ) ){
              operation = CommandFactory.Operation.INCORRECT;

              showToControlDialog();

            } else {
              operation = CommandFactory.Operation.CHECK_FOR_CONTROL;
              params.setDocument( settings.getUid() );
            }
            break;

          case R.id.menu_info_decision_create_with_assignment:
            // настройка
            // Показывать подтверждения о постановке на контроль документов для раздела «Обращение граждан»
            operation = CommandFactory.Operation.INCORRECT;
            settings.setDecisionWithAssignment(true);
            settings.setDecisionActiveId(0);
            Intent create_assigment_intent = new Intent(context, DecisionConstructorActivity.class);
            activity.startActivity(create_assigment_intent);
            break;

          // resolved https://tasks.n-core.ru/browse/MVDESD-13368
          // Кнопка "Отклонить" в документах "На рассмотрение" и "Первичное рассмотрение"
          case R.id.menu_info_report_dismiss:
            if ( settings.isActionsConfirm() ){
              operation = CommandFactory.Operation.INCORRECT;
              showDismissDialog();
            } else {
              operation = CommandFactory.Operation.RETURN_TO_THE_PRIMARY_CONSIDERATION;
              params.setDocument( settings.getUid() );
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

  public static int parseIntOrDefault(String value, int defaultValue) {
    int result = defaultValue;
    try {
      result = Integer.parseInt(value);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  private boolean checkImagesSize() {
    Boolean result = true;

    if ( doc.getImages() != null && doc.getImages().size() > 0 ){
      for (RImage _image: doc.getImages()) {
        RImageEntity image = (RImageEntity) _image;

        int max_size = parseIntOrDefault( settings.getMaxImageSize(), 20 )*1024*1024;

        if (max_size > 20*1024*1024){
          settings.setMaxImageSize("20");
          result = false;
        }

        Timber.tag(TAG).e("MAX: %s | CURRENT: %s", max_size, image.getSize() );
        if (image.getSize() > max_size){
          result = false;
        }
      }
    }

    return result;
  }

  public void invalidate() {
    getFirstForLenovo();

    if (doc != null){

      String code = null;

      try {
        code = settings.getStatusCode();
      } catch (Exception e) {
        e.printStackTrace();
      }

      toolbar.getMenu().clear();

      int menu;

      if (code == null){
        menu = R.menu.info_menu;
      } else {
        switch ( settings.getStatusCode() ){
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
      }
      toolbar.inflateMenu(menu);


      // Из папки обработанное
      if (isProcessed()){
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.info_menu);

        try {
          showAsProcessed(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      // Из папки избранное
      if (isFromFavoritesFolder() ){
        showAsProcessed(false);
      }

      decision_count = doc.getDecisions().size();
      switch (decision_count) {
        case 0:
          processEmptyDecisions();
          break;
        default:
          safeSetVisibility(R.id.menu_info_decision_create, false);
          safeSetVisibility(R.id.menu_info_decision_edit, false);
          break;
      }

      // Если документ обработан - то изменяем резолюции на поручения
      if ( isProcessed() ) {
        safeSetVisibility(R.id.menu_info_decision_edit, false);
        safeSetVisibility(R.id.menu_info_decision_create, true);
      }

      if (isFromProject() || isFromFavoritesFolder() ) {
        // resolved https://tasks.n-core.ru/browse/MVDESD-12765
        // убрать кнопку "К" у проектов из раздела на согласование("на подписание" её также быть не должно)
        safeSetVisibility(R.id.menu_info_shared_to_control, false);
      }


      for (int i = 0; i < toolbar.getMenu().size(); i++) {
        MenuItem item = toolbar.getMenu().getItem(i);

        switch (item.getItemId()) {
          case R.id.menu_info_shared_to_favorites:
            item.setTitle(context.getString(
              isFromFavorites() ? R.string.remove_from_favorites : R.string.to_favorites));
            break;
          case R.id.menu_info_shared_to_control:
            item.setTitle(context.getString(
              isFromControl() ? R.string.remove_from_control : R.string.to_control));
            break;
          default:
            break;
        }
      }

      // resolved https://tasks.n-core.ru/browse/MVDESD-13259
      // Кнопка "Без ответа" только на документах без резолюции

      // resolved https://tasks.n-core.ru/browse/MVDESD-13330
      // Или если нет активной резолюции
      if ( hasActiveDecision() ){
        safeSetVisibility(R.id.menu_info_to_the_approval_performance, false);
      }


      // resolved https://tasks.n-core.ru/browse/MVDESD-13343
      // Или если нет активной резолюции
      if ( isShared() ){
        clearToolbar();
      }


      if ( isProcessed() ){
        safeSetVisibility(R.id.menu_info_decision_create_with_assignment, settings.isShowCreateDecisionPost());
        safeSetVisibility(R.id.menu_info_decision_create, settings.isShowCreateDecisionPost());

        if (isFromProject()){
          safeSetVisibility(R.id.menu_info_decision_create_with_assignment, false);
        }
      }

    }
  }

  private void safeSetVisibility(int item, boolean value) {
    if (toolbar.getMenu() != null) {
      if (toolbar.getMenu().findItem(item) != null) {
        toolbar.getMenu().findItem(item).setVisible(value);
      }
    }
  }


  private boolean isFromProject() {
    return doc != null && doc.getFilter() != null && Arrays.asList( Fields.Status.APPROVAL.getValue(), Fields.Status.SIGNING.getValue() ).contains(doc.getFilter());
  }

  private void clearToolbar() {
    toolbar.getMenu().clear();
    // раскоментить в следующей версии
    // когда будет понимание как всё должно работать
//    if ( doc != null && doc.getFilter() != null && Objects.equals(doc.getFilter(), Fields.Status.PRIMARY_CONSIDERATION.getValue())){
//      toolbar.inflateMenu(R.menu.info_menu_primary_consideration);
//
//      decision_count = doc.getDecisions().size();
//      switch (decision_count) {
//        case 0:
//          processEmptyDecisions();
//          break;
//        default:
//          try {
//            toolbar.getMenu().findItem(R.id.menu_info_decision_create).setVisible(false);
//            toolbar.getMenu().findItem(R.id.menu_info_decision_edit).setVisible(false);
//          } catch (Exception e) {
//            Timber.tag(TAG).v(e);
//          }
//          break;
//      }
//    }
  }

  private void showAsProcessed(Boolean showCreateButton) {
    toolbar.getMenu().clear();
    toolbar.inflateMenu(R.menu.info_menu);

    safeSetVisibility(R.id.menu_info_decision_create, showCreateButton);
    safeSetVisibility(R.id.menu_info_decision_edit, false);
    safeSetVisibility(R.id.menu_info_shared_to_control, true);
    safeSetVisibility(R.id.menu_info_shared_to_favorites, true);

  }

  private boolean isShared() {
    return doc != null && doc.getAddressedToType() != null && Objects.equals(doc.getAddressedToType(), "group");
  }

  public boolean hasActiveDecision() {
    Boolean result = false;

    try {
      if (doc != null && doc.getDecisions().size() > 0){
        for ( RDecision _decision: doc.getDecisions() ) {
          RDecisionEntity decision = (RDecisionEntity) _decision;
          if (!decision.isApproved() && Objects.equals(decision.getSignerId(), settings.getCurrentUserId())){
            result = true;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }

  private boolean isProcessed() {
    return doc.isProcessed() != null && doc.isProcessed() || doc.isFromProcessedFolder() != null && doc.isFromProcessedFolder();
  }

  private boolean isFromControl() {
    return doc.isControl() != null && doc.isControl();
  }

  private boolean isFromFavorites() {
    return doc.isFavorites() != null && doc.isFavorites();
  }
  private boolean isFromFavoritesFolder() {
    return doc.isFromFavoritesFolder() != null && doc.isFromFavoritesFolder();
  }


  //REFACTOR переделать это
  private void processEmptyDecisions() {
    safeSetVisibility( R.id.menu_info_decision_edit  , false);
    safeSetVisibility( R.id.menu_info_decision_create, true);
  }

  public void setEditDecisionMenuItemVisible(boolean visible){
    safeSetVisibility( R.id.menu_info_decision_edit, false);
    // if (visible){
    //   safeSetVisibility( R.id.menu_info_decision_edit, false);
    // } else {
    //   safeSetVisibility( R.id.menu_info_decision_edit, false);
    // }
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
    toolbar.setSubtitleTextColor( context.getResources().getColor( R.color.md_grey_300 ) );

    toolbar.setContentInsetStartWithNavigation(250);

    toolbar.setNavigationOnClickListener(v ->{
      Activity activity = (Activity) context;
      activity.finish();
//      activity.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
//      activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
      }
    );

//    status  = Fields.Status.findStatus(STATUS_CODE.startTransactionFor());
//    journal = Fields.getJournalByUid( UID.startTransactionFor() );


    Timber.tag("MENU").e( "STATUS CODE: %s", settings.getStatusCode() );

    invalidate();

    toolbar.setTitle( String.format("%s от %s", settings.getRegNumber(), settings.getRegDate()) );
    if (doc!=null && doc.getDocumentType() != null){
      toolbar.setSubtitle( String.format("%s", Fields.getJournalName(doc.getDocumentType()) ) );
    }
  }

  public void showCreateDecisionButton() {
    if (!isFromProject()){
      safeSetVisibility( R.id.menu_info_decision_create, true);
    }
  }

  public void hideDialog() {
    if (dialog != null) {
      dialog.hide();
    }
  }

  private void showToControlDialog() {
    Boolean isControl = false;

    if (isFromControl()){
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
        params.setUser( settings.getLogin() );
        params.setDocument( settings.getUid() );

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
        operation = !isApproval ? CommandFactory.Operation.APPROVAL_NEXT_PERSON: CommandFactory.Operation.SIGNING_NEXT_PERSON;

        CommandParams params = new CommandParams();
        params.setUser( settings.getLogin() );
        params.setDocument( settings.getUid() );
        params.setSign( "SignFileCommand" );

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


        params.setUser(settings.getLogin());
        params.setSign("SignFileCommand");

        // если есть комментарий
        if (settings.getPrevDialogComment() != null && settings.isShowCommentPost() ) {
//          params.setComment("SignFileCommand");
          if ( settings.isShowCommentPost() ) {
            params.setComment(dialog1.getInputEditText().getText().toString());
          }
        }
        params.setDocument( settings.getUid() );


        operationManager.execute(operation, params);
      })
      .autoDismiss(true);

      // настройка
      // Показывать комментарий при отклонении
      if ( settings.isShowCommentPost() ){
        prev_dialog.inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES )
          .input(R.string.comment_hint, R.string.dialog_empty_value, (dialog12, input) -> {
            settings.setPrevDialogComment( input.toString() );
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
        params.setPerson( settings.getCurrentUserId() );
        params.setDocument( settings.getUid() );
        if ( settings.isShowCommentPost() ) {
          params.setComment(dialog1.getInputEditText().getText().toString());
        }

        operationManager.execute(operation, params);
      })
      .autoDismiss(true);

    // настройка
    // Показывать комментарий при отклонении
    if ( settings.isShowCommentPost() ){
      fromTheReportDialog.inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES )
        .input(R.string.comment_hint, R.string.dialog_empty_value, (dialog12, input) -> {
          settings.setPrevDialogComment( input.toString() );
          params.setComment( input.toString() );
        });
    }


    fromTheReportDialog.build().show();
  }

  // resolved https://tasks.n-core.ru/browse/MVDESD-13368
  // Подтверждение отклонения с возвратом на первичное рассмотрение
  private void showDismissDialog() {
    CommandParams params = new CommandParams();

    new MaterialDialog.Builder(context)
      .content(R.string.dialog_reject_body)
      .cancelable(true)
      .positiveText(R.string.yes)
      .negativeText(R.string.no)
      .onPositive((dialog1, which) -> {
        CommandFactory.Operation operation;
        operation = CommandFactory.Operation.RETURN_TO_THE_PRIMARY_CONSIDERATION;
        params.setDocument( settings.getUid() );
        operationManager.execute(operation, params);
      })
      .autoDismiss(true)
      .build()
      .show();
  }

  // OSHS selector
  @Override
  public void onSearchSuccess(Oshs user, CommandFactory.Operation operation, String uid) {
    Timber.tag("onSearchSuccess").i("user: %s", user.getName());
    CommandParams params = new CommandParams();
    params.setPerson( user.getId() );
    params.setDocument( uid );
    operationManager.execute( operation, params );
  }

  @Override
  public void onSearchError(Throwable error) {

  }



  /* OperationManager.Callback */
  @Override
  public void onExecuteSuccess(String command) {
    Timber.tag(TAG).w("updateFromJob %s", command );

    switch (command){
      case "check_for_control":
        EventBus.getDefault().post( new ShowSnackEvent("Отметки для постановки на контроль успешно обновлены.") );
        break;
      case "add_to_folder":
        EventBus.getDefault().post( new ShowSnackEvent("Добавление в избранное.") );
        break;
      case "remove_from_folder":
        EventBus.getDefault().post( new ShowSnackEvent("Удаление из избранного.") );
        break;
      default:

        toolbar.inflateMenu(R.menu.info_menu);
//        EventBus.getDefault().postSticky( new RemoveDocumentFromAdapterEvent( UID.startTransactionFor() ) );
//        EventBus.getDefault().post( new ShowNextDocumentEvent() );
        break;
    }

    invalidate();
  }

  @Override
  public void onExecuteError() {

  }
}

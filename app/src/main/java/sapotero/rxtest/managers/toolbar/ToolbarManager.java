package sapotero.rxtest.managers.toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
import sapotero.rxtest.events.decision.CheckDecisionVisibilityEvent;
import sapotero.rxtest.events.decision.DecisionVisibilityEvent;
import sapotero.rxtest.events.decision.ShowDecisionConstructor;
import sapotero.rxtest.events.view.ShowSnackEvent;
import sapotero.rxtest.managers.menu.OperationManager;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.managers.menu.utils.CommandParams;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.views.activities.DecisionConstructorActivity;
import sapotero.rxtest.views.dialogs.SelectOshsDialogFragment;
import timber.log.Timber;

public class ToolbarManager  implements SelectOshsDialogFragment.Callback, OperationManager.Callback {

  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject ISettings settings;
  @Inject OperationManager operationManager;
  @Inject MemoryStore store;


  private final String TAG = this.getClass().getSimpleName();

  private int decision_count;

  private Toolbar toolbar;
//  private Context context = EsdApplication.getApplication().getApplicationContext();
  private Context context;

  private RDocumentEntity doc;
  private MaterialDialog dialog;
  private String command;
  private static ToolbarManager instance;

  public ToolbarManager (Context context, Toolbar toolbar) {
    this.context = context;
    this.toolbar = toolbar;
    EsdApplication.getManagerComponent().inject(this);

    registerEvents();
    setListener();

    buildDialog();

    operationManager.registerCallBack(this);

    // FIX починить и убрать из релиза
    getFirstForLenovo();

    EventBus.getDefault().post( new CheckDecisionVisibilityEvent() );
  }

  public ToolbarManager() {
  }

  public static ToolbarManager getInstance(){
    if (instance == null) {
      instance = new ToolbarManager();
    }
    return instance;
  }

  public ToolbarManager withToolbar(Toolbar toolbar){
    this.toolbar = toolbar;
    return this;
  }
  public ToolbarManager withContext(Context context){
    this.context = context;
    return this;
  }
  public ToolbarManager build(){
    EsdApplication.getManagerComponent().inject(this);

    registerEvents();
    setListener();

    buildDialog();

    operationManager.registerCallBack(this);

    // FIX починить и убрать из релиза
    getFirstForLenovo();

    EventBus.getDefault().post( new CheckDecisionVisibilityEvent() );
    return instance;
  }

  private void registerEvents() {
    EventBus.getDefault().unregister(this);
    EventBus.getDefault().register(this);
  }

  private void getFirstForLenovo() {
    doc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(settings.getUid())).get().firstOrNull();
    registerEvents();
  }

  private void setListener() {
    final Activity activity = (Activity) context;

    toolbar.setOnMenuItemClickListener(
      item -> {

        CommandFactory.Operation operation;
        CommandParams params = new CommandParams();

        switch ( item.getItemId() ){
          // sent_to_the_report (отправлен на доклад)
//          case R.id.menu_info_from_the_report:
//            operation = CommandFactory.Operation.FROM_THE_REPORT;
//            break;
          case R.id.menu_info_to_the_primary_consideration:

            Timber.v("primary_consideration");

            showPrimaryConsiderationDialog(activity);

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
            if ( settings.isShowCommentPost() || !settings.isShowCommentPost() && settings.isActionsConfirm() ){
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
              params.setPerson( "" );
            }
//
            break;
          case R.id.menu_info_approval_prev_person:
            // настройка
            // Показывать подтверждения о действиях с документом
            if ( settings.isShowCommentPost() || !settings.isShowCommentPost() && settings.isActionsConfirm() ){
              operation = CommandFactory.Operation.INCORRECT;
              showPrevDialog(true);
            } else {
              operation = CommandFactory.Operation.APPROVAL_PREV_PERSON;
              params.setPerson( "" );
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

            //resolved https://tasks.n-core.ru/browse/MVDESD-13952
            // при подписании проекта без ЭО не подписывать
            // и не перемещать в обработанные
            if ( hasImages() ){

              //проверим что все образы меньше 25Мб
              if ( checkImagesSize() ){

                // настройка
                // Показывать подтверждения о действиях с документом
                if ( settings.isActionsConfirm() ){
                  operation = CommandFactory.Operation.INCORRECT;
                  showNextDialog(true);
                } else {
                  operation = CommandFactory.Operation.SIGNING_NEXT_PERSON;
                  params.setPerson( "" );
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
            } else {
              new MaterialDialog.Builder(context)
                .title("Внимание!")
                .content("Выбранные документы не могут быть отправлены по маршруту. Проверьте наличие чистовых электронных образов и подписавшего в маршруте.")
                .positiveText("Продолжить")
                .icon(ContextCompat.getDrawable(context, R.drawable.attention))
                .show();

              operation = CommandFactory.Operation.INCORRECT;
            }



            break;
          case R.id.menu_info_sign_prev_person:

            // настройка
            // Показывать подтверждения о действиях с документом
            if ( settings.isShowCommentPost() || !settings.isShowCommentPost() && settings.isActionsConfirm() ){
              operation = CommandFactory.Operation.INCORRECT;
              showPrevDialog(false);
            } else {
              operation = CommandFactory.Operation.SIGNING_PREV_PERSON;
              params.setPerson( "" );
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

            // resolved https://tasks.n-core.ru/browse/MPSED-2134
            // Не работает добавление/удаление в избранное, если перезайти в режимы замещения.
            // также не работает добавление в избранное в режиме замещения
            // Ищем папку favorites по логину
            String favorites = dataStore
              .select(RFolderEntity.class)
              .where(RFolderEntity.TYPE.eq("favorites"))
              .and(RFolderEntity.USER.eq(settings.getLogin()))
              .get().first().getUid();

            params.setFolder( favorites );

            break;
          case R.id.menu_info_shared_to_control:
            // настройка
            // Показывать подтверждения о постановке на контроль документов для раздела «Обращение граждан»

            if ( settings.isControlConfirm() && settings.getUid().startsWith( Fields.Journal.CITIZEN_REQUESTS.getValue() ) ){
              operation = CommandFactory.Operation.INCORRECT;

              showToControlDialog();

            } else {
              operation = !isFromControl() ? CommandFactory.Operation.CHECK_CONTROL_LABEL : CommandFactory.Operation.UNCHECK_CONTROL_LABEL;
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
    EventBus.getDefault().post( new CheckDecisionVisibilityEvent() );
  }

  public void showPrimaryConsiderationDialog(Activity activity) {
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
  }

  private static int parseIntOrDefault(String value, int defaultValue) {
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

  private boolean hasImages() {
    return doc.getImages() != null && doc.getImages().size() > 0;
  }

  public void invalidate() {
    getFirstForLenovo();

    if (doc != null){

      inflateMenu();

      // Из папки обработанное
      if (isProcessed()){
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.info_menu);
        showAsProcessed(true);
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
          safeSetVisibility(R.id.menu_info_decision_edit,   false);

//          if ( doc.isProcessed() != null && doc.isProcessed() ){
//            safeSetVisibility(R.id.menu_info_decision_edit, false);
//          }
          break;
      }

      processFavoritesAndControlIcons();

      if (isFromProject() || isFromFavoritesFolder() ) {
        // resolved https://tasks.n-core.ru/browse/MVDESD-12765
        // убрать кнопку "К" у проектов из раздела на согласование("на подписание" её также быть не должно)
        safeSetVisibility(R.id.menu_info_shared_to_control, false);
      }


      // Если документ обработан - то изменяем резолюции на поручения
      if ( isProcessed() ){
        safeSetVisibility(R.id.menu_info_decision_create_with_assignment, settings.isShowCreateDecisionPost());
        safeSetVisibility(R.id.menu_info_decision_create, settings.isShowCreateDecisionPost());

        if (isFromProject()){
          safeSetVisibility(R.id.menu_info_decision_create_with_assignment, false);
        }
      }


      // resolved https://tasks.n-core.ru/browse/MVDESD-13259
      // Кнопка "Без ответа" только на документах без резолюции

      // resolved https://tasks.n-core.ru/browse/MVDESD-13330
      // Или если нет активной резолюции
      if ( hasActiveDecision() ){
        safeSetVisibility(R.id.menu_info_to_the_approval_performance, false);
        safeSetVisibility(R.id.menu_info_decision_create, false);
//        safeSetVisibility(R.id.menu_info_decision_edit,   true);
      } else {
        safeSetVisibility(R.id.menu_info_decision_create, true);
      }

      if (isFromFavoritesFolder()){
        safeSetVisibility(R.id.menu_info_decision_create_with_assignment, settings.isShowCreateDecisionPost());

        if ( settings.isProject() ){
          safeSetVisibility(R.id.menu_info_decision_create_with_assignment, false);
          safeSetVisibility(R.id.menu_info_decision_edit, false);
          safeSetVisibility(R.id.menu_info_decision_create, false);
        }
      }

      // resolved https://tasks.n-core.ru/browse/MVDESD-13343
      // Или если нет активной резолюции
      if ( isShared() ){
        clearToolbar();
      }

      EventBus.getDefault().post( new CheckDecisionVisibilityEvent() );

    }
  }

  private void processFavoritesAndControlIcons() {
    for (int i = 0; i < toolbar.getMenu().size(); i++) {
      MenuItem item = toolbar.getMenu().getItem(i);

      switch (item.getItemId()) {
        case R.id.menu_info_shared_to_favorites:
          item.setTitle(context.getString(
            isFromFavorites() ? R.string.remove_from_favorites : R.string.to_favorites));

          //resolved https://tasks.n-core.ru/browse/MVDESD-13867
          // 5. Изменить отображение иконки в избранном (звезда)
          // сделать наоборот
          item.setIcon( ContextCompat.getDrawable(context, !isFromFavorites() ? R.drawable.to_favorites : R.drawable.star) );
          break;
        case R.id.menu_info_shared_to_control:
          item.setTitle(context.getString( isFromControl() ? R.string.remove_from_control : R.string.to_control));
          item.setIcon( ContextCompat.getDrawable(context, isFromControl() ? R.drawable.to_controle_on : R.drawable.to_controle_off ) );
          break;
        default:
          break;
      }
    }
  }

  private void inflateMenu() {

    toolbar.getMenu().clear();
    int menu = R.menu.info_menu;

    if (settings.getStatusCode() != null) {
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
  }

  private void showAsProcessed(Boolean showCreateButton) {
    Timber.tag(TAG).e("showAsProcessed");

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

  private boolean hasActiveDecision() {
    Boolean result = false;

    if (doc.getDecisions() != null && doc.getDecisions().size() > 0){
      for ( RDecision _decision: doc.getDecisions() ) {
        RDecisionEntity decision = (RDecisionEntity) _decision;
        if (!decision.isApproved() && Objects.equals(decision.getSignerId(), settings.getCurrentUserId()) || decision.isRed() && !decision.isApproved() ){
          result = true;
          break;
        }
      }
    }

    return result;
  }

  private boolean hasTemporaryDecision() {
    Boolean result = false;

    if (doc.getDecisions() != null && doc.getDecisions().size() > 0){
      for ( RDecision _decision: doc.getDecisions() ) {
        RDecisionEntity decision = (RDecisionEntity) _decision;

        if ( decision.isTemporary() != null && decision.isTemporary()){
          result = true;
          break;
        }
      }
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
    Timber.tag(TAG).e("processEmptyDecisions");
    safeSetVisibility( R.id.menu_info_decision_edit  , false);
    showCreateDecisionButton();
  }

  public void setEditDecisionMenuItemVisible(boolean visible){
    safeSetVisibility( R.id.menu_info_decision_edit, visible);
    // if (visible){
    //   safeSetVisibility( R.id.menu_info_decision_edit, false);
    // } else {
    //   safeSetVisibility( R.id.menu_info_decision_edit, false);
    // }
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
      }
    );


    Timber.tag("MENU").e( "STATUS CODE: %s", settings.getStatusCode() );

    invalidate();

    toolbar.setTitle( String.format("%s от %s", settings.getRegNumber(), settings.getRegDate()) );
    if (doc!=null && doc.getDocumentType() != null){
      toolbar.setSubtitle( String.format("%s", Fields.getJournalName(doc.getDocumentType()) ) );
    }
  }

  private void showCreateDecisionButton() {
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

    Boolean finalIsControl = isControl;
    new MaterialDialog.Builder( context )
      .title(   isControl ? R.string.dialog_to_control_negative      : R.string.dialog_to_control_positive      )
      .content( isControl ? R.string.dialog_to_control_body_negative : R.string.dialog_to_control_body_positive )
      .cancelable(true)
      .positiveText(R.string.approve)
      .negativeText(R.string.cancel)
      .onPositive((dialog1, which) -> {

        CommandFactory.Operation operation;

        operation = !finalIsControl ? CommandFactory.Operation.CHECK_CONTROL_LABEL : CommandFactory.Operation.UNCHECK_CONTROL_LABEL;

        CommandParams params = new CommandParams();

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
        params.setPerson( "" );

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

        params.setPerson( "" );

        // если есть комментарий
        if (settings.getPrevDialogComment() != null && settings.isShowCommentPost() ) {
//          params.setComment("SignFileCommand");
          if ( settings.isShowCommentPost() ) {
            params.setComment(dialog1.getInputEditText().getText().toString());
          }
        }

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
          }).cancelable(false);
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
        if ( settings.isShowCommentPost() ) {
          params.setComment(dialog1.getInputEditText().getText().toString());
        }

        operationManager.execute(operation, params);
      })
      .autoDismiss(true);

    // настройка
    // Показывать комментарий при отклонении
    if ( settings.isShowCommentPost() || !settings.isShowCommentPost() && settings.isActionsConfirm() ){
      fromTheReportDialog.inputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES )
        .input(R.string.comment_hint, R.string.dialog_empty_value, (dialog12, input) -> {
          settings.setPrevDialogComment( input.toString() );
          params.setComment( input.toString() );
        }).cancelable(false);
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
    this.command = command;
    switch (command){
      case "check_for_control":
        EventBus.getDefault().post( new ShowSnackEvent("Отметки для постановки на контроль успешно обновлены.") );
        break;
      case "uncheck_control_label":
        EventBus.getDefault().post( new ShowSnackEvent("Отметки для постановки на контроль успешно обновлены.") );
        break;

      case "add_to_folder":
        EventBus.getDefault().post( new ShowSnackEvent("Добавление в избранное.") );
        break;
      case "remove_from_folder":
        EventBus.getDefault().post( new ShowSnackEvent("Удаление из избранного.") );
        break;
    }

    invalidate();
//    EventBus.getDefault().post( new CheckActiveDecisionEvent() );
  }

  public void dropControlLabel(Boolean control){
    try {
      MenuItem item = toolbar.getMenu().findItem(R.id.menu_info_shared_to_control);
      item.setTitle(context.getString( !control ? R.string.remove_from_control : R.string.to_control));
      item.setIcon( ContextCompat.getDrawable(context, !control ? R.drawable.to_controle_on : R.drawable.to_controle_off ) );
    } catch (Exception e) {
      Timber.e(e);
    }
  }

  @Override
  public void onExecuteError() {

  }


  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(DecisionVisibilityEvent event){
    Timber.tag(TAG).e("DecisionVisibilityEvent %s", event.toString() );

    if (event.approved != null) {

      // resolved https://tasks.n-core.ru/browse/MPSED-2154
      setEditDecisionMenuItemVisible(event.approved);

      if ( store.getDocuments().get( settings.getUid() ).isProcessed() ){
        setEditDecisionMenuItemVisible(false);
      }

      if ( hasTemporaryDecision() ){
        safeSetVisibility(R.id.menu_info_decision_edit, false);
        safeSetVisibility(R.id.menu_info_decision_create, false);
      }
    }
  }
}

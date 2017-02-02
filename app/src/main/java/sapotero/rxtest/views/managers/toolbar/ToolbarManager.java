package sapotero.rxtest.views.managers.toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RFolderEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.crypto.SignDataEvent;
import sapotero.rxtest.events.rx.ShowSnackEvent;
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

  public ToolbarManager(Context context, Toolbar toolbar) {
    this.context = context;
    this.toolbar = toolbar;
    EsdApplication.getComponent(context).inject(this);


    loadSettings();

    setListener();

    buildDialog();

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
            operation = CommandFactory.Operation.TO_THE_PRIMARY_CONSIDERATION;
            break;

          // sent_to_the_report (отправлен на доклад)
          case R.id.menu_info_delegate_performance:
            operation = CommandFactory.Operation.DELEGATE_PERFORMANCE;
            params.setPerson( "USER_UD" );
            break;
          case R.id.menu_info_to_the_approval_performance:
            operation = CommandFactory.Operation.TO_THE_APPROVAL_PERFORMANCE;
            params.setPerson( "USER_UD" );
            break;

          // primary_consideration (первичное рассмотрение)

          case R.id.menu_info_approval_next_person:
            operation = CommandFactory.Operation.APPROVAL_NEXT_PERSON;
            buildDialog();
            dialog.show();

            params.setSign( SIGN );
            break;
          case R.id.menu_info_approval_prev_person:
            operation = CommandFactory.Operation.APPROVAL_PREV_PERSON;
            params.setSign( "SIGN" );
            break;


          // approval (согласование проектов документов)
          case R.id.menu_info_sign_change_person:
            operation = CommandFactory.Operation.SIGNING_CHANGE_PERSON;

            if (oshs == null){
              oshs = new SelectOshsDialogFragment();
              oshs.registerCallBack( this );
            }


            oshs.show( activity.getFragmentManager(), "SelectOshsDialogFragment");
            break;

          case R.id.menu_info_approval_change_person:
            operation = CommandFactory.Operation.APPROVAL_CHANGE_PERSON;

            if (oshs == null){
              oshs = new SelectOshsDialogFragment();
              oshs.registerCallBack( this );
            }

            oshs.show( activity.getFragmentManager(), "SelectOshsDialogFragment");
            break;
          case R.id.menu_info_sign_next_person:
            operation = CommandFactory.Operation.SIGNING_NEXT_PERSON;
            params.setSign( "SIGN" );
            break;
          case R.id.menu_info_sign_prev_person:
            operation = CommandFactory.Operation.SIGNING_PREV_PERSON;
            params.setSign( "SIGN" );
            break;

          case R.id.menu_info_decision_create:
            operation = CommandFactory.Operation.NEW_DECISION;

            Intent create_intent = new Intent(context, DecisionConstructorActivity.class);
            context.startActivity(create_intent);

            break;

          case R.id.menu_info_decision_edit:
            operation = CommandFactory.Operation.NEW_DECISION;

            Intent edit_intent = new Intent(context, DecisionConstructorActivity.class);
            context.startActivity(edit_intent);

            break;
          case R.id.menu_info_shared_to_favorites:
            operation = CommandFactory.Operation.ADD_TO_FOLDER;

//            item.setTitle(getString( doc.isFavorites() != null && doc.isFavorites() ? R.string.remove_from_favorites : R.string.to_favorites));

            String favorites = dataStore
              .select(RFolderEntity.class)
              .where(RFolderEntity.TYPE.eq("favorites"))
              .get().first().getUid();

            params.setFolder(favorites);
            params.setDocument( UID.get() );


            break;
          case R.id.menu_info_shared_to_control:

//            item.setTitle(getString( doc.isControl() != null && doc.isControl() ? R.string.remove_from_control : R.string.to_control));

            operation = CommandFactory.Operation.CHECK_FOR_CONTROL;
            params.setDocument( UID.get() );
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
    STATUS_CODE = settings.getString("activity_main_menu.start");
    REG_NUMBER = settings.getString("activity_main_menu.regnumber");
    REG_DATE = settings.getString("activity_main_menu.date");
  }

  private void invalidate() {
    RDocumentEntity doc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(UID.get())).get().first();

    Timber.tag(TAG).v("invalidate: %s", new Gson().toJson(doc) );


    // проверяем, сколько есть резолюций у документа
    // если она одна, то показываем кнопки Подписать/Отклонить
    // если несколько, то показываем редактировать - для редактирования текущей
    // если нет - то показываем кнопку создать

    decision_count = doc.getDecisions().size();

    switch ( decision_count ){
      case 0:
        processEmptyDecisions();
        break;
      default:
        try {
          toolbar.getMenu().findItem( R.id.menu_info_decision_create ).setVisible(false);
          toolbar.getMenu().findItem( R.id.menu_info_decision_edit).setVisible(true);
        } catch (Exception e) {
          Timber.tag(TAG).v(e);
        }
        break;
    }

    //FIX в обработанных - изменить резолюции на поручения
    // Если документ обработан - то изменяем резолюции на поручения
    if( doc.isProcessed() && Objects.equals(doc.getFilter(), Fields.Status.PROCESSED.getValue())){
      try {
        //настройка
        toolbar.getMenu().findItem( R.id.menu_info_decision_create).setTitle( context.getString( R.string.info_create_decision_processed ) );
      } catch (Exception e) {
        Timber.tag(TAG).v(e);
      }
    }

    for (int i = 0; i < toolbar.getMenu().size(); i++) {
      MenuItem item = toolbar.getMenu().getItem(i);

      switch ( item.getItemId() ) {
        case R.id.menu_info_shared_to_favorites:
          item.setTitle( context.getString( doc.isFavorites() != null && doc.isFavorites() ? R.string.remove_from_favorites : R.string.to_favorites));
          break;
        case R.id.menu_info_shared_to_control:
          item.setTitle( context.getString( doc.isControl() != null && doc.isControl() ? R.string.remove_from_control : R.string.to_control));
          break;
        default:
          break;
      }
    }

    try {
      if ( !settings.getBoolean("settings_view_show_create_decision_post").get() ){
        toolbar.getMenu().findItem( R.id.menu_info_decision_create).setVisible(false);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  //REFACTOR переделать это
  private void processEmptyDecisions() {
    try {
      toolbar.getMenu().findItem( R.id.menu_info_decision_sign  ).setVisible(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      toolbar.getMenu().findItem( R.id.menu_info_decision_reject).setVisible(false);
    } catch (Exception e) {
      e.printStackTrace();
    }
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

  public void update(String command){

    Timber.tag(TAG).w("add %s", command );

    if ( Objects.equals(command, "change_person") ) {
//      Toast.makeText( context.getApplicationContext(), "Операция передачи успешно завершена", Toast.LENGTH_SHORT).show();
//      Snackbar.make( , "Операция передачи успешно завершена", Snackbar.LENGTH_SHORT).show();

      EventBus.getDefault().post( new ShowSnackEvent("Операция передачи успешно завершена") );

      toolbar.getMenu().clear();
      toolbar.inflateMenu(R.menu.info_menu);
    }

    if ( Objects.equals(command, "next_person") ) {
      Toast.makeText( context.getApplicationContext(), "Операция подписания успешно завершена", Toast.LENGTH_SHORT).show();
      toolbar.getMenu().clear();
      toolbar.inflateMenu(R.menu.info_menu);
    }

    if ( Objects.equals(command, "prev_person") ) {
      Toast.makeText( context.getApplicationContext(), "Операция отклонения успешно завершена", Toast.LENGTH_SHORT).show();
      toolbar.getMenu().clear();
      toolbar.inflateMenu(R.menu.info_menu);
    }

    invalidate();

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

  public void hideDialog() {
    if (dialog != null) {
      dialog.hide();
    }
  }

  // OSHS selector
  @Override
  public void onSearchSuccess(Oshs user) {
    CommandParams params = new CommandParams();
    params.setPerson( user.getId() );
    operationManager.execute( CommandFactory.Operation.APPROVAL_CHANGE_PERSON, params );
  }

  @Override
  public void onSearchError(Throwable error) {

  }
}

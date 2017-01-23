package sapotero.rxtest.views.managers.toolbar;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RFolderEntity;
import sapotero.rxtest.events.crypto.SignDataEvent;
import sapotero.rxtest.views.activities.DecisionConstructorActivity;
import sapotero.rxtest.views.managers.menu.OperationManager;
import sapotero.rxtest.views.managers.menu.factories.CommandFactory;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;

public class ToolbarManager {

  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject RxSharedPreferences settings;
  @Inject OperationManager operationManager;

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
  private MaterialDialog dialog;
  private String SIGN;

  public ToolbarManager(Context context, Toolbar toolbar) {
    this.context = context;
    this.toolbar = toolbar;
    EsdApplication.getComponent(context).inject(this);


    loadSettings();

    initView();

    setListener();

    buildDialog();

    invalidate();

  }

  private void setListener() {
    toolbar.setOnMenuItemClickListener(
      item -> {



        CommandFactory.Operation operation;
        CommandParams params = new CommandParams();
        params.setUser( LOGIN.get() );

        switch ( item.getItemId() ){
          // sent_to_the_report (отправлен на доклад)
          case R.id.menu_info_from_the_report:
            operation = CommandFactory.Operation.FROM_THE_REPORT;
            break;
          case R.id.return_to_the_primary_consideration:
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
          case R.id.menu_info_to_the_primary_consideration:
            operation = CommandFactory.Operation.INCORRECT;

//            if (oshs == null){
//              oshs = new SelectOshsDialogFragment();
//              oshs.registerCallBack( this );
//            }
//
//            oshs.show( getFragmentManager(), "SelectOshsDialogFragment");
            break;

          // approval (согласование проектов документов)
          case R.id.menu_info_approval_change_person:
            operation = CommandFactory.Operation.INCORRECT;
//
//            if (oshs == null){
//              oshs = new SelectOshsDialogFragment();
//              oshs.registerCallBack( this );
//            }
//
//            oshs.show( getFragmentManager(), "SelectOshsDialogFragment");

            break;
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
            operation = CommandFactory.Operation.INCORRECT;

//            if (oshs == null){
//              oshs = new SelectOshsDialogFragment();
//              oshs.registerCallBack( this );
//            }
//
//            oshs.show( getFragmentManager(), "SelectOshsDialogFragment");
            break;
          case R.id.menu_info_sign_next_person:
            operation = CommandFactory.Operation.SIGNING_NEXT_PERSON;
            params.setSign( "SIGN" );
            break;
          case R.id.menu_info_sign_prev_person:
            operation = CommandFactory.Operation.SIGNING_PREV_PERSON;
            params.setSign( "SIGN" );
            break;


          case R.id.action_info_create_decision:
            operation = CommandFactory.Operation.NEW_DECISION;

            Intent intent = new Intent(context, DecisionConstructorActivity.class);
            context.startActivity(intent);

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

  private void initView() {

  }

  private void loadSettings() {
    LOGIN    = settings.getString("login");
    UID      = settings.getString("main_menu.uid");
    PASSWORD = settings.getString("password");
    TOKEN    = settings.getString("token");
    POSITION = settings.getInteger("position");
    DOCUMENT_UID = settings.getString("document.uid");
    STATUS_CODE = settings.getString("main_menu.start");
    REG_NUMBER = settings.getString("main_menu.regnumber");
    REG_DATE = settings.getString("main_menu.date");
  }

  public void invalidate() {
    RDocumentEntity doc = dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.UID.eq(UID.get())).get().first();


    for (int i = 0; i < toolbar.getMenu().size(); i++) {
      MenuItem item = toolbar.getMenu().getItem(i);

      switch ( item.getItemId() ) {
        case R.id.menu_info_shared_to_favorites:
          item.setTitle( context.getString( doc.isFavorites() != null && doc.isFavorites() ? R.string.remove_from_favorites : R.string.to_favorites));
          break;
        case R.id.menu_info_shared_to_control:
          item.setTitle( context.getString( doc.isControl() != null && doc.isControl() ? R.string.remove_from_control : R.string.to_control));
          break;
      }
    }
  }

  public void update( int id, String title ){
    try {
      toolbar.getMenu().findItem(id).setTitle(title);
    } catch (Exception e) {
      e.printStackTrace();
    }
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

}

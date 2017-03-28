package sapotero.rxtest.views.dialogs;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.query.Result;
import io.requery.query.WhereAndOr;
import io.requery.rx.SingleEntityStore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RAssistantEntity;
import sapotero.rxtest.db.requery.models.RFavoriteUserEntity;
import sapotero.rxtest.db.requery.models.RPrimaryConsiderationEntity;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.views.adapters.OshsAutoCompleteAdapter;
import sapotero.rxtest.views.adapters.PrimaryUsersAdapter;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;
import sapotero.rxtest.views.custom.DelayAutoCompleteTextView;
import timber.log.Timber;

public class SelectOshsDialogFragment extends DialogFragment implements View.OnClickListener {

  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  private String TAG = this.getClass().getSimpleName();

  @BindView(R.id.user_autocomplete_field) DelayAutoCompleteTextView title;
  @BindView(R.id.pb_loading_indicator) ProgressBar indicator;
  @BindView(R.id.oshs_wrapper) FrameLayout oshs_wrapper;

  Callback callback;
  private CommandFactory.Operation operation;
  private PrimaryUsersAdapter adapter;
  private ArrayList<String> user_ids;

  private boolean showWithAssistant = false;
  private boolean withPrimaryConsideration = false;
  private boolean withSearch = false;
  private boolean withConfirm = false;

  private PrimaryConsiderationPeople user = null;
  private OshsAutoCompleteAdapter autocomplete_adapter;
  private String documentUid = null;

  public void setIgnoreUsers(ArrayList<String> users) {
    Timber.tag("setIgnoreUsers").e("users %s", new Gson().toJson(users) );
    user_ids = new ArrayList<>();
    user_ids = users;
  }

  public void showWithAssistant(boolean show) {
    showWithAssistant = show;
  }

  public void withPrimaryConsideration(boolean primaryConsideration) {
    this.withPrimaryConsideration = primaryConsideration;
  }

  public void withSearch(boolean withSearch) {
    this.withSearch = withSearch;
  }

  public void withConfirm(boolean withConfirm) {
    this.withConfirm = withConfirm;
  }

  public void withDocumentUid(String uid) {
    this.documentUid = uid;
  }


  public interface Callback {
    void onSearchSuccess(Oshs user, CommandFactory.Operation operation, String uid);
    void onSearchError(Throwable error);
  }
  public void registerCallBack(Callback callback){
    this.callback = callback;
  }


  @RequiresApi(api = Build.VERSION_CODES.M)
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    operation = CommandFactory.Operation.INCORRECT;
    EsdApplication.getComponent( getActivity() ).inject( this );

    Bundle bundle = getArguments();
    if (bundle != null) {
      String _operation = bundle.getString("operation");
      if ( _operation != null ){
        Timber.e("OPERATION: %s", _operation);

        switch ( _operation ){
          case "approve":
            operation = CommandFactory.Operation.APPROVAL_CHANGE_PERSON;
            break;
          case "sign":
            operation = CommandFactory.Operation.SIGNING_CHANGE_PERSON;
            break;
          case "primary_consideration":
            operation = CommandFactory.Operation.TO_THE_PRIMARY_CONSIDERATION;
            break;
          default:
            break;
        }
      }
    }

    View view = inflater.inflate(R.layout.dialog_choose_oshs, container);

    view.findViewById(R.id.dialog_oshs_add).setOnClickListener( v ->{
      if ( callback != null && user != null ) {

        Oshs oshs = new Oshs();
        oshs.setId( user.getId() );
        oshs.setOrganization( user.getOrganization() );
        oshs.setAssistantId( user.getAssistantId() );
        oshs.setPosition( user.getPosition() );
        oshs.setName( user.getName() );

        Timber.e("setOnItemClickListener OPERATION: %s", operation.toString());
        callback.onSearchSuccess(oshs, operation, documentUid);
        dismiss();

      } else {
        Toast.makeText( getContext(), "Выберете исполнителя!", Toast.LENGTH_SHORT ).show();
      }
    });

    view.findViewById(R.id.dialog_oshs_cancel).setOnClickListener(v -> {
      dismiss();
    });

//    view.findViewById(R.id.user_autocomplete_field).clearFocus();

    if (!withSearch){
      ((Button) view.findViewById(R.id.dialog_oshs_add)).setText( R.string.primary_consideration_oshs_dialog_yes );
    }

    if ( showWithAssistant ){
      ((Button) view.findViewById(R.id.dialog_oshs_add)).setText( R.string.approve );
    }



    ArrayList<PrimaryConsiderationPeople> people = new ArrayList<>();

    adapter = new PrimaryUsersAdapter( getActivity(), people);

    ListView list = (ListView) view.findViewById(R.id.dialog_oshs_listview_users);
    list.setAdapter(adapter);

    list.setOnItemClickListener((parent, view12, position, id) -> {
      if ( !withSearch || withPrimaryConsideration ){
        user = adapter.getItem(position);

        if (user != null) {
          title.setAdapter(null);
          indicator.setVisibility(View.GONE);
          title.setText( user.getName() );
        }

      }

      if (withConfirm){
        user = adapter.getItem(position);
        if (user != null) {
          indicator.setVisibility(View.GONE);
          title.setText( user.getName() );
          title.cancelPendingInputEvents();
          title.hideIndicator();
        }

//        Timber.tag("withConfirm").w("%s", user.getName());

//        if (user != null) {
//          title.setAdapter(null);
//          indicator.setVisibility(View.GONE);
//          title.setText( user.getName() );
//        }

      } else {

        if (callback != null) {
          Oshs _user = adapter.getOshs(position);
          callback.onSearchSuccess(_user, operation, documentUid);
          dismiss();
        }
      }
    });

    if (showWithAssistant){
      dataStore
        .select(RAssistantEntity.class).get().toObservable()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe( user -> {
           adapter.add( new PrimaryConsiderationPeople( user.getHeadId(), user.getTitle(), "", "", user.getAssistantId() ) );
        });
    }

    if (withPrimaryConsideration){
      WhereAndOr<Result<RPrimaryConsiderationEntity>> query = dataStore
        .select(RPrimaryConsiderationEntity.class)
        .where(RPrimaryConsiderationEntity.UID.ne( settings.getString("current_user_id").get() ));

      query.get()
        .toObservable()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe( user -> {
           adapter.add( new PrimaryConsiderationPeople( user.getUid(), user.getName(), user.getPosition(), user.getOrganization(), null) );
        });
    } else {

      WhereAndOr<Result<RFavoriteUserEntity>> query =
        dataStore
          .select(RFavoriteUserEntity.class)
          .where(RFavoriteUserEntity.UID.ne(""));

      if (user_ids != null){
        query = query.and(RFavoriteUserEntity.UID.notIn(user_ids));
      }


      query.get()
        .toObservable()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe( user -> {
           adapter.add( new PrimaryConsiderationPeople( user.getUid(), user.getName(), user.getPosition(), user.getOrganization(), null) );
        });
    }


    // подпихнуть в автокомлпитер
    // обноаить превьюху

    ButterKnife.bind(this, view);


    title.setText("");

    title.setThreshold(2);

    title.setLoadingIndicator( indicator );


    if (withSearch){
      autocomplete_adapter = new OshsAutoCompleteAdapter(getActivity());
      title.setAdapter( autocomplete_adapter );
    } else {
      title.setFocusable(false);
      title.setAdapter(null);
    }

    if ( withSearch || !withPrimaryConsideration ){

      title.setOnItemClickListener(
        (adapterView, view1, position, id) -> {
          Oshs _user = (Oshs) adapterView.getItemAtPosition(position);

          Timber.tag("SEARCH").w("%s %s", _user.getName(), _user.getId());

          if ( callback != null){
            Timber.e("setOnItemClickListener OPERATION: %s", operation.toString());
//            callback.onSearchSuccess( user, operation);
          }

          user = new PrimaryConsiderationPeople( _user.getId(), _user.getName(), _user.getPosition(), _user.getOrganization(), _user.getAssistantId());
          title.setText( _user.getName() );
          title.cancelPendingInputEvents();
          title.hideIndicator();

          // dismiss();
        }
      );
    }

    return view;
  }

  public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    Timber.tag(TAG).i( "onDismiss");
  }

  public void onCancel(DialogInterface dialog) {
    super.onCancel(dialog);
    Timber.tag(TAG).i( "onCancel");
  }



  @Override
  public void onClick(View v) {

  }
}

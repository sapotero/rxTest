package sapotero.rxtest.views.dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.query.WhereAndOr;
import io.requery.rx.RxResult;
import io.requery.rx.SingleEntityStore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.mapper.AssistantMapper;
import sapotero.rxtest.db.mapper.FavoriteUserMapper;
import sapotero.rxtest.db.mapper.PerformerMapper;
import sapotero.rxtest.db.mapper.PrimaryConsiderationMapper;
import sapotero.rxtest.db.requery.models.RAssistantEntity;
import sapotero.rxtest.db.requery.models.RFavoriteUserEntity;
import sapotero.rxtest.db.requery.models.RPrimaryConsiderationEntity;
import sapotero.rxtest.managers.menu.factories.CommandFactory;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.views.adapters.OshsAutoCompleteAdapter;
import sapotero.rxtest.views.adapters.PrimaryUsersAdapter;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;
import sapotero.rxtest.views.custom.DelayAutoCompleteTextView;
import timber.log.Timber;

public class SelectOshsDialogFragment extends DialogFragment implements PrimaryUsersAdapter.PrimaryUsersAdapterFilterListener, OshsAutoCompleteAdapter.OshsAutoCompleteAdapterFilterListener {

  public static final String SEPARATOR_FAVORITES_TEXT = "Результат поиска по избранному";
  public static final String SEPARATOR_OSHS_TEXT = "Результат поиска по ОШС МВД";

  @Inject ISettings settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  private String TAG = this.getClass().getSimpleName();

  @BindView(R.id.user_autocomplete_field) DelayAutoCompleteTextView title;
  @BindView(R.id.user_autocomplete_textinputlayout) TextInputLayout textInputLayout;
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
  private boolean withChangePerson = false;

  // If true, organizations will be included in search results
  private boolean withOrganizations = false;

  private boolean withPerformers = false;

  private PrimaryConsiderationPeople user = null;
  private OshsAutoCompleteAdapter autocomplete_adapter;
  private String documentUid = null;

  private List<PrimaryConsiderationPeople> resultFromOshs = new ArrayList<>();

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

  public void withChangePerson(boolean withChangePerson) {
    this.withChangePerson = withChangePerson;
  }

  public void withOrganizations(boolean withOrganizations) {
    this.withOrganizations = withOrganizations;
  }

  public void withPerformers(boolean withPerformers) {
    this.withPerformers = withPerformers;
  }

  public interface Callback {
    void onSearchSuccess(Oshs user, CommandFactory.Operation operation, String uid);
    void onSearchError(Throwable error);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setCancelable(false);
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    operation = CommandFactory.Operation.INCORRECT;
    EsdApplication.getDataComponent().inject( this );

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
        Oshs oshs = (Oshs) new PerformerMapper().convert(user, PerformerMapper.DestinationType.OSHS);
        Timber.e("setOnItemClickListener OPERATION: %s", operation.toString());
        callback.onSearchSuccess(oshs, operation, documentUid);
        dismiss();

      } else {
        Toast.makeText( getContext(), "Выберите сотрудника", Toast.LENGTH_SHORT ).show();
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

    if ( withChangePerson ) {
      ((Button) view.findViewById(R.id.dialog_oshs_add)).setText(R.string.primary_consideration_oshs_dialog_yes);
    }

    ArrayList<PrimaryConsiderationPeople> people = new ArrayList<>();

    adapter = new PrimaryUsersAdapter( getActivity(), people);

    ListView list = (ListView) view.findViewById(R.id.dialog_oshs_listview_users);
    list.setAdapter(adapter);

    list.setOnItemClickListener((parent, view12, position, id) -> {
      if ( !Objects.equals( adapter.getItem(position).getOrganization(), SEPARATOR_FAVORITES_TEXT)
        && !Objects.equals( adapter.getItem(position).getOrganization(), SEPARATOR_OSHS_TEXT) ) {

        title.clearFocus();

        if (!withSearch || withPrimaryConsideration) {
          user = adapter.getItem(position);

          if (user != null) {
            title.setAdapter(null);
            indicator.setVisibility(View.GONE);
            title.setText(user.getName());
          }

        }

        if (withConfirm) {
          user = adapter.getItem(position);
          if (user != null) {
            indicator.setVisibility(View.GONE);
            title.setText(user.getName());
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

        if (view12 != null) {
          InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
          inputManager.hideSoftInputFromWindow(view12.getWindowToken(), 0);
        }

      }
    });

    if (showWithAssistant){
      dataStore
        .select(RAssistantEntity.class)
        .where(RAssistantEntity.USER.eq( settings.getLogin() ))
        .get().toObservable()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe( user -> {
           adapter.add( new AssistantMapper().toPrimaryConsiderationPeople(user) );
        }, Timber::e);
    }

    if (withPrimaryConsideration){
      WhereAndOr<RxResult<RPrimaryConsiderationEntity>> query = dataStore
        .select(RPrimaryConsiderationEntity.class)
        .where(RPrimaryConsiderationEntity.UID.ne( settings.getCurrentUserId() ))
        .and(  RPrimaryConsiderationEntity.USER.eq( settings.getLogin() ));

      query.get()
        .toObservable()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe( user -> {
           adapter.add( new PrimaryConsiderationMapper().toPrimaryConsiderationPeople(user) );
        }, Timber::e);
    } else {

      WhereAndOr<RxResult<RFavoriteUserEntity>> query =
        dataStore
          .select(RFavoriteUserEntity.class)
          .where(RFavoriteUserEntity.UID.ne(""))
          .and(  RFavoriteUserEntity.USER.eq( settings.getLogin() ));

      if (user_ids != null){
        query = query.and(RFavoriteUserEntity.UID.notIn(user_ids));
      }


      query.get()
        .toObservable()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe( user -> {
           adapter.add( new FavoriteUserMapper().toPrimaryConsiderationPeople(user) );
        }, Timber::e);
    }


    // подпихнуть в автокомлпитер
    // обноаить превьюху

    ButterKnife.bind(this, view);


    title.setText("");

    title.setThreshold(2);

    title.setLoadingIndicator( indicator );


    if (withSearch){
      autocomplete_adapter = new OshsAutoCompleteAdapter(getActivity(), title);
      autocomplete_adapter.withOrganizations(withOrganizations);
      autocomplete_adapter.setIgnoreUsers(user_ids);
      autocomplete_adapter.setThreshold(title.getThreshold());
      title.setAdapter( autocomplete_adapter );
    } else {
      title.setFocusable(false);
      title.setAdapter(null);
    }

    if (!withSearch && withPrimaryConsideration) {
      title.setAdapter(null);
      title.setFocusable(true);
      title.setFocusableInTouchMode(true);
      title.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
          if (charSequence != null && charSequence.length() >= 1) {
            adapter.getFilter().filter(charSequence);
          } else {
            adapter.cancelFiltering();
          }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
      });
    }

    if ( withSearch || !withPrimaryConsideration ){

      title.setOnItemClickListener(
        (adapterView, view1, position, id) -> {
          title.clearFocus();

          Oshs _user = (Oshs) adapterView.getItemAtPosition(position);

          Timber.tag("SEARCH").w("%s %s", _user.getName(), _user.getId());

          if ( callback != null){
            Timber.e("setOnItemClickListener OPERATION: %s", operation.toString());
//            callback.onSearchSuccess( user, operation);
          }

          user = (PrimaryConsiderationPeople) new PerformerMapper().convert(_user, PerformerMapper.DestinationType.PRIMARYCONSIDERATIONPEOPLE);

          title.setText( _user.getName() );
          title.cancelPendingInputEvents();
          title.hideIndicator();

          // dismiss();
        }
      );
    }

    // resolved https://tasks.n-core.ru/browse/MVDESD-14013
    // Диалог добавления исполнителей
    if ( withPerformers ) {
      title.setAdapter(autocomplete_adapter);
      title.setFocusable(true);
      title.setFocusableInTouchMode(true);
      adapter.registerListener(this);
      autocomplete_adapter.registerListener(this);
      title.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
          if (charSequence != null && charSequence.length() >= 1) {
            adapter.getFilter().filter(charSequence);
          } else {
            adapter.cancelFiltering();
          }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
      });
    }

    return view;
  }

  // resolved https://tasks.n-core.ru/browse/MVDESD-14013
  // Диалог добавления исполнителей
  // This callback is called only if withPerformers = true (listener is registered only in this case)
  @Override
  public void onPrimaryUsersAdapterFilterComplete() {
    autocomplete_adapter.clear();
    autocomplete_adapter.setIgnoreUsers(user_ids);

    PrimaryConsiderationPeople separatorFavorites = new PrimaryConsiderationPeople();
    separatorFavorites.setOrganization( SEPARATOR_FAVORITES_TEXT );
    separatorFavorites.setDelimiter(true);
    adapter.removeItem( separatorFavorites );

    if ( adapter.getCount() > 0 ) {
      adapter.addFirstResultItem( separatorFavorites );
    }

    // Do not search in OSHS people, which already present in favorites list
    for ( PrimaryConsiderationPeople people : adapter.getResultItems() ) {
      autocomplete_adapter.addIgnoreUser( people.getId() );
    }

    // Remove previous OSHS search results from list
    for ( PrimaryConsiderationPeople people : resultFromOshs ) {
      adapter.removeItem( people );
    }

    title.filter( title.getText().toString() );
  }

  // resolved https://tasks.n-core.ru/browse/MVDESD-14013
  // Диалог добавления исполнителей
  // This callback is called only if withPerformers = true (listener is registered only in this case)
  @Override
  public void onOshsAutoCompleteAdapterFilterComplete() {
    resultFromOshs.clear();

    PrimaryConsiderationPeople separatorOshs = new PrimaryConsiderationPeople();
    separatorOshs.setOrganization(SEPARATOR_OSHS_TEXT);
    separatorOshs.setDelimiter(true);

    resultFromOshs.add( separatorOshs );
    adapter.addResultItem( separatorOshs );

    for ( Oshs oshs : autocomplete_adapter.getResultList() ) {
      PrimaryConsiderationPeople people = (PrimaryConsiderationPeople) new PerformerMapper().convert(oshs, PerformerMapper.DestinationType.PRIMARYCONSIDERATIONPEOPLE);
      resultFromOshs.add(people);
      adapter.addResultItem( people );
    }

    title.dismissDropDown();
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    super.onDismiss(dialog);
    Timber.tag(TAG).i( "onDismiss");
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    super.onCancel(dialog);
    Timber.tag(TAG).i( "onCancel");
  }
}

package sapotero.rxtest.views.dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.ProgressBar;

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
import sapotero.rxtest.db.requery.models.RFavoriteUserEntity;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.views.adapters.OshsAutoCompleteAdapter;
import sapotero.rxtest.views.adapters.PrimaryUsersAdapter;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;
import sapotero.rxtest.views.custom.DelayAutoCompleteTextView;
import sapotero.rxtest.views.managers.menu.factories.CommandFactory;
import timber.log.Timber;

public class SelectOshsDialogFragment extends DialogFragment implements View.OnClickListener {


  @Inject SingleEntityStore<Persistable> dataStore;

  private String TAG = this.getClass().getSimpleName();

  @BindView(R.id.user_autocomplete_field) DelayAutoCompleteTextView title;
  @BindView(R.id.pb_loading_indicator) ProgressBar indicator;

  SelectOshsDialogFragment.Callback callback;
  private CommandFactory.Operation operation;
  private PrimaryUsersAdapter adapter;
  private ArrayList<String> user_ids;

  public void setIgnoreUsers(ArrayList<String> users) {
    Timber.tag("setIgnoreUsers").e("users %s", new Gson().toJson(users) );
    user_ids = new ArrayList<>();
    user_ids = users;
  }

  public interface Callback {
    void onSearchSuccess(Oshs user, CommandFactory.Operation operation);
    void onSearchError(Throwable error);
  }
  public void registerCallBack(SelectOshsDialogFragment.Callback callback){
    this.callback = callback;
  }


  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    operation = CommandFactory.Operation.INCORRECT;
    EsdApplication.getComponent( getActivity() ).inject( this );

    Bundle bundle = getArguments();
    if (bundle != null) {
      String _operation = bundle.getString("operation");
      if ( _operation != null ){
        switch ( _operation ){
          case "approve":
            operation = CommandFactory.Operation.APPROVAL_CHANGE_PERSON;
            break;
          case "sign":
            operation = CommandFactory.Operation.SIGNING_CHANGE_PERSON;
            break;
          case "to_the_primary_consideration":
            operation = CommandFactory.Operation.TO_THE_PRIMARY_CONSIDERATION;
            break;
          default:
            break;
        }
      }
    }

    View view = inflater.inflate(R.layout.dialog_choose_oshs, null);
    view.findViewById(R.id.dialog_oshs_add).setOnClickListener(this);
    view.findViewById(R.id.dialog_oshs_cancel).setOnClickListener(this);


    ArrayList<PrimaryConsiderationPeople> people = new ArrayList<>();

    adapter = new PrimaryUsersAdapter( getActivity(), people);

    ListView list = (ListView) view.findViewById(R.id.dialog_oshs_listview_users);
    list.setAdapter(adapter);

    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

    list.setOnItemClickListener((parent, view12, position, id) -> {
      if ( callback != null){
        Oshs user = adapter.getOshs(position);
        callback.onSearchSuccess( user, operation );
        dismiss();
      }
    });

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
        Timber.tag("FavoriteUser").e( "%s - %s", user.getId(), user.getName() );
        adapter.add( new PrimaryConsiderationPeople( user.getUid(), user.getName(), user.getPosition(), user.getOrganization() ) );
      });

    // подпихнуть в автокомлпитер
    // обноаить превьюху

    ButterKnife.bind(this, view);

    title.setText("");

    title.setThreshold(2);
    title.setAdapter( new OshsAutoCompleteAdapter(getActivity()) );
    title.setLoadingIndicator( indicator );



    title.setOnItemClickListener(
      (adapterView, view1, position, id) -> {
        title.setText("");

        if ( callback != null){
          Oshs user = (Oshs) adapterView.getItemAtPosition(position);
          callback.onSearchSuccess( user, operation);
        }
        dismiss();
      }
    );
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

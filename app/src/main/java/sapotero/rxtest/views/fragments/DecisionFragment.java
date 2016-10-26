package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.retrofit.utils.OshsService;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import timber.log.Timber;

public class DecisionFragment extends Fragment {
  private static final String LOGIN = "";
  private static final String PASSWORD = "";

  private OnFragmentInteractionListener mListener;

  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;

  @BindView(R.id.card_toolbar) Toolbar card_toolbar;

  private String TAG = this.getClass().getSimpleName();
  private Context CONTEXT;

  private String login;
  private String token;
  private OshsService oshsService;

  public DecisionFragment() {
  }

  public static DecisionFragment newInstance() {
    DecisionFragment fragment = new DecisionFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_decision, container, false);
    ButterKnife.bind(this, view);
    EsdApplication.getComponent( CONTEXT ).inject( this );

    Retrofit retrofit = new RetrofitManager( CONTEXT, Constant.HOST + "v2/", okHttpClient).process();
    oshsService = retrofit.create( OshsService.class );

    loadSettings();


    card_toolbar.inflateMenu(R.menu.card_menu);
    card_toolbar.setTitle("Блок");

    card_toolbar.setOnMenuItemClickListener( item -> {
      String operation;

      switch ( item.getItemId() ){
        case R.id.decision_card_action_delete:
          operation = "decision_card_action_delete";


          Observable<Oshs[]> oshs = oshsService.search( login, token, "Кол");

          oshs.subscribeOn( Schedulers.newThread() )
            .observeOn( AndroidSchedulers.mainThread() )
            .take(1)
            .subscribe(
              data -> {
                for (Oshs user: data ) {
                  Timber.tag(TAG).i("data: "+ user.getFirstName());
                }
              });

//          getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
          break;
        case R.id.decision_card_action_add:
          operation = "decision_card_action_add";
          break;
        default:
          operation = "incorrect";
          break;
      }

      Timber.tag(TAG).i( operation );
      getActivity().getSupportFragmentManager().popBackStack();
      return false;
    });

    return view;
  }

  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    CONTEXT = context;
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  private void loadSettings() {
    Preference<String> _username = settings.getString("login");
    login = _username.get();

    Preference<String> _token = settings.getString("token");
    token = _token.get();
  }

  public interface OnFragmentInteractionListener {
    void onFragmentInteraction(Uri uri);
  }
}

package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.jobs.bus.UpdateDecisionPreviewJob;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.utils.OshsService;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import sapotero.rxtest.views.activities.DecisionConstructorActivity;
import sapotero.rxtest.views.adapters.PrimaryConsiderationAdapter;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;
import timber.log.Timber;

public class DecisionFragment extends Fragment implements PrimaryConsiderationAdapter.Callback {
  private static final String LOGIN = "";
  private static final String PASSWORD = "";

  private OnFragmentInteractionListener mListener;

  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject JobManager jobManager;

  @BindView(R.id.card_toolbar)  Toolbar  card_toolbar;
  @BindView(R.id.decision_text) EditText decision_text;

  @BindView(R.id.fragment_decision_button_familiarization) ToggleButton button_familiarization;
  @BindView(R.id.fragment_decision_button_report) ToggleButton button_report;

  @BindView(R.id.fragment_decision_linear_people) LinearLayout people_view;





  private String TAG = this.getClass().getSimpleName();
  private Context mContext;

  private String login;
  private String token;
  private OshsService oshsService;
  private int number;
  private Block block;
  private Preference<String> HOST;
  private PrimaryConsiderationAdapter adapter;

  public DecisionFragment() {
  }

  public static DecisionFragment newInstance() {
    DecisionFragment fragment = new DecisionFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle bundle = this.getArguments();
    if (bundle != null) {
      number = bundle.getInt("number", 0);

      Gson gson = new Gson();
      block = gson.fromJson( bundle.getString("block"), Block.class );
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_decision, container, false);
    ButterKnife.bind(this, view);
    EsdApplication.getComponent(mContext).inject( this );

    loadSettings();

    Retrofit retrofit = new RetrofitManager(mContext, HOST.get() + "v2/", okHttpClient).process();
    oshsService = retrofit.create( OshsService.class );



    card_toolbar.inflateMenu(R.menu.card_menu);
    card_toolbar.setTitle("Блок " + number );
    decision_text.setText( block.getText() );
    decision_text.addTextChangedListener(new TextWatcher() {

      public void onTextChanged(CharSequence s, int start, int before, int count) {
        Timber.tag(TAG).v( "onTextChanged" );
        try {
          jobManager.addJobInBackground( new UpdateDecisionPreviewJob() );
        } catch ( Exception e){
          Timber.tag(TAG + " massInsert error").v( e );
        }
      }

      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      public void afterTextChanged(Editable s) {
      }
    });

    card_toolbar.setOnMenuItemClickListener(
      item -> {
      String operation;

      switch ( item.getItemId() ){
        case R.id.decision_card_action_delete:
          operation = "decision_card_action_delete";
          getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
          break;
        default:
          operation = "incorrect";
          break;
      }

      Timber.tag(TAG).i( operation );
      getActivity().getSupportFragmentManager().popBackStack();
      return false;
    });


    button_familiarization.setOnCheckedChangeListener(
      (buttonView, isChecked) -> {
        Timber.tag(TAG).v( "button_familiarization ++" );
        if (isChecked){
          button_report.setChecked(false);
          button_familiarization.setChecked(true);
        }
      }
    );

    button_report.setOnCheckedChangeListener(
      (buttonView, isChecked) -> {
        Timber.tag(TAG).v( "button_report ++" );
        if (isChecked){
          button_familiarization.setChecked(false);
          button_report.setChecked(true);
        }
      }
    );

    Timber.e(" ArrayList<PrimaryConsiderationPeople> people = new ArrayList<>(); ");

    ArrayList<PrimaryConsiderationPeople> people = new ArrayList<>();
    people.add( new PrimaryConsiderationPeople("123", "Казаков В.А.", "teisting", "TEST LTD") );
    people.add( new PrimaryConsiderationPeople("123", "Казаков В.А1.", "teisting", "TEST LTD") );
    people.add( new PrimaryConsiderationPeople("123", "Казаков В.А.2", "teisting", "TEST LTD") );
    people.add( new PrimaryConsiderationPeople("123", "Казаков В.А.3", "teisting", "TEST LTD") );

    adapter = new PrimaryConsiderationAdapter( getContext(), people);
    adapter.registerCallBack(this);
    updateUsers();

    return view;
  }

  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }

  private void updateUsers(){
    people_view.removeAllViews();
    for (int i = 0; i < adapter.getCount(); i++) {
      View item = adapter.getView(i, null, null);
      people_view.addView(item);
    }
  }

  private void updateUsersAttribute(){
//    for(int index=0; index<( (ViewGroup) people_view).getChildCount(); ++index) {
//      View nextChild = ( (ViewGroup) people_view).getChildAt(index);
//
//      Timber.tag("updateUsersAttribute").e("%s", nextChild.getId());
//
//      for(int i=0; i<( (ViewGroup) nextChild).getChildCount(); ++i) {
//        View field = ((ViewGroup) nextChild).getChildAt(index);
//
//        Timber.tag("list").e("recv %s", field.getId());
//      }
//    }
  }

  @OnClick(R.id.fragment_decision_button_add_people)
  public void add(){
    Timber.tag("ADD PEOPLE").e("CLICKED");

    adapter.add( new PrimaryConsiderationPeople("123", "Казаков В.А.3", "teisting", "TEST LTD") );
    updateUsers();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    mContext = context;
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    ((DecisionConstructorActivity)getActivity()).getDecisionManager().remove(this);
    mListener = null;
  }

  private void loadSettings() {
    Preference<String> _username = settings.getString("login");
    login = _username.get();

    Preference<String> _token = settings.getString("token");
    token = _token.get();

    HOST = settings.getString("settings_username_host");
  }

  @Override
  public void onRemove() {
    updateUsers();
  }

  @Override
  public void onAttrChange() {
    updateUsersAttribute();
  }

  public interface OnFragmentInteractionListener {
    void onFragmentInteraction(Uri uri);
  }

  public Block getBlock(){

    String appealText = "";
    if (button_report.isChecked()) {
      appealText = button_report.getTextOn().toString();
    } else if (button_familiarization.isChecked()) {
      appealText = button_familiarization.getTextOn().toString();
    }

    Block block = new Block();
    block.setNumber(number);
    block.setAppealText( appealText );
    block.setText( decision_text.getText().toString() );
    block.setTextBefore(true);
    block.setToFamiliarization(false);
    block.setToCopy(false);
    block.setHidePerformers(false);

//    number: 1,
//      text: "ТЕСТ",
//      appeal_text: "Прошу доложить",
//      text_before: false,
//      hide_performers: false,
//      to_copy: false,
//      to_familiarization: true,
    return block;
  }

  public void setNumber( int number){
    card_toolbar.setTitle("Блок " + number);
  }

}

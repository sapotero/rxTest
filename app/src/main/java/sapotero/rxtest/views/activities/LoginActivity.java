package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.events.bus.FileDownloadedEvent;
import sapotero.rxtest.events.bus.MarkDocumentAsChangedJobEvent;
import sapotero.rxtest.views.interfaces.DataLoaderInterface;
import sapotero.rxtest.views.services.AuthService;
import sapotero.rxtest.views.views.VerticalStepperFormLayout;
import sapotero.rxtest.views.views.utils.VerticalStepperForm;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity implements VerticalStepperForm, DataLoaderInterface.Callback {

  @BindView(R.id.stepper_form) VerticalStepperFormLayout stepper;

  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject JobManager jobManager;


  private String TAG = this.getClass().getSimpleName();

  // login view
  private View wrapper;
  private View view;
  private TextView username;
  private TextView password;
  private TextView host;
  private ProgressBar progress;

  private AppCompatButton button;
  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> PASSWORD;
  private Preference<String> HOST;

  private DataLoaderInterface dataLoader;
  private CheckBox stepper_loader_user;
  private CheckBox stepper_loader_list;
  private CheckBox stepper_loader_info;
  private ProgressBar stepper_loader_user_progressbar;
  private ProgressBar stepper_loader_list_progressbar;
  private ProgressBar stepper_loader_info_progressbar;
//  private RelativeLayout finishLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    dataLoader = new DataLoaderInterface(this);
    dataLoader.registerCallBack(this);


    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    initialize();

    String[] steps = {"Авторизация", "Загрузка данных"};
    String[] subtitles = {"введите данные", null};
    VerticalStepperFormLayout.Builder.newInstance(stepper, steps, this, this)
      .primaryColor( Color.RED )
//      .primaryColor( ContextCompat.getColor( this, R.color.md_red_800 ) )
      .primaryDarkColor( ContextCompat.getColor( this, R.color.md_blue_grey_200 ) )
      .displayBottomNavigation(false)
      .materialDesignInDisabledSteps(true)
      .showVerticalLineWhenStepsAreCollapsed(true)
      .stepsSubtitles(subtitles)
      .init();

    startService(new Intent(this, AuthService.class));

    if (!EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().register(this);
    }
  }

  private void initialize() {
    LOGIN    = settings.getString("login");
    PASSWORD = settings.getString("password");
    TOKEN    = settings.getString("token");
    HOST     = settings.getString("settings_username_host");
  }

  @Override
  public void onStop() {
    super.onStop();

    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }

  }

  public void tryToLogin() {
    getCredentials();

  }

  public void getCredentials(){

    wrapper.setAlpha(0.25f);
    progress.setVisibility(View.VISIBLE);

    saveSettings("");

    dataLoader.getAuthToken();

  }

  private void saveSettings(String authToken) {
    LOGIN.set( username.getText().toString());
    PASSWORD.set( password.getText().toString());
    HOST.set( host.getText().toString() );
    TOKEN.set( authToken );

    Preference<Integer> updated = settings.getInteger("updated");
    updated.set( (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) );
  }



  public View createStepContentView(int stepNumber) {
    View view = null;
    switch (stepNumber) {
      case 0:
        view = loginForm();
        break;
      case 1:
        view = loadData();
        break;
      case 2:
        view = showFinal();
        break;
    }
    return view;
  }

  private View showFinal() {
    view = LayoutInflater.from(this).inflate(R.layout.stepper_load_data, null);

    return view;
  }


  public void onStepOpening(int stepNumber) {
    switch (stepNumber) {
      case 0:
        checkLogin();
        break;
      case 1:
        resetLoadDataForm();
        break;
      case 2:
//        stepper.setStepAsCompleted(2);
        break;
    }
  }

  private Boolean checkLogin( ) {
    boolean isCorrect = false;

    resetLoginForm();

    if( host.length() >0 && username.length() > 0 && password.length() > 0 ) {
      isCorrect = true;

      button = (AppCompatButton) stepper.getRootView().findViewById(R.id.next_step);

      button.setOnClickListener(view ->{
        Timber.d( "BUTTON CLICK %s", view.getId() );
        tryToLogin();
      });

    } else {
      String titleError = "Введите данные!";
      stepper.setActiveStepAsUncompleted(titleError);
    }

    return isCorrect;
  }

  private void resetLoginForm() {
    progress.setVisibility(View.GONE);
    wrapper.setAlpha(1.0f);
    wrapper.setBackgroundColor( getResources().getColor(R.color.transparent) );
  }

  private void resetLoadDataForm() {
    stepper_loader_user_progressbar.setVisibility(View.INVISIBLE);
    stepper_loader_list_progressbar.setVisibility(View.INVISIBLE);
    stepper_loader_info_progressbar.setVisibility(View.INVISIBLE);

    stepper_loader_user.setChecked(false);
    stepper_loader_list.setChecked(false);
    stepper_loader_info.setChecked(false);

    stepper.setStepAsUncompleted(1, "");
  }

  private View loginForm() {

    view = LayoutInflater.from(this).inflate(R.layout.stepper_login, null);
    wrapper = ButterKnife.findById(view, R.id.wrapper);
    username = ButterKnife.findById(view, R.id.stepper_login_username);
    password = ButterKnife.findById(view, R.id.stepper_login_password);
    host     = ButterKnife.findById(view, R.id.stepper_login_host);
    progress = ButterKnife.findById(view, R.id.stepper_login_progress);

    HOST = settings.getString("settings_username_host");

    if (Objects.equals(HOST.get(), null)){
      Timber.tag(TAG).i("EMPTY HOST");
      HOST.set( Constant.HOST );
    }

    host.setText( HOST.get() );

    TextWatcher watcher = new TextWatcher(){

      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        checkLogin();
      }

      @Override
      public void afterTextChanged(Editable editable) {
      }
    };

    username.addTextChangedListener(watcher);
    password.addTextChangedListener(watcher);
    host.addTextChangedListener(watcher);

    return view;
  }

  private View loadData() {
    view = LayoutInflater.from(this).inflate(R.layout.stepper_load_data, null);

    stepper_loader_user = ButterKnife.findById(view, R.id.stepper_loader_user );
    stepper_loader_list = ButterKnife.findById(view, R.id.stepper_loader_list );
    stepper_loader_info = ButterKnife.findById(view, R.id.stepper_loader_info );

    stepper_loader_user_progressbar = ButterKnife.findById(view, R.id.stepper_loader_user_progressbar );
    stepper_loader_list_progressbar = ButterKnife.findById(view, R.id.stepper_loader_list_progressbar );
    stepper_loader_info_progressbar = ButterKnife.findById(view, R.id.stepper_loader_info_progressbar );

    stepper_loader_user_progressbar.setVisibility(View.VISIBLE);

    return view;
  }

  @Override
  public void sendData() {
    Timber.e("SendData");
    start();
  }

  private void start() {
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);

    finish();
  }

  @Override
  public void setFinalView(RelativeLayout content) {
    View finalView = LayoutInflater.from(this).inflate(R.layout.stepper_final_view, null);

    content.addView( finalView );
  }

  @Override
  public void onAuthTokenSuccess() {

    new Handler().postDelayed( () -> {
      progress.setVisibility(View.GONE);
      stepper.setActiveStepAsCompleted();
      stepper.goToNextStep();
      Timber.i( "LOGIN: %s\nTOKEN: %s", LOGIN.get(), TOKEN.get() );
      dataLoader.getUserInformation();

    }, 2000L);
  }

  @Override
  public void onAuthTokenError(Throwable error) {
    Toast.makeText( this, String.format( "onError: Error %s", error.getMessage() ), Toast.LENGTH_SHORT).show();
    resetLoginForm();
  }

  @Override
  public void onGetUserInformationSuccess() {
//    new Handler().postDelayed( () -> {
//
//      stepper_loader_user_progressbar.setVisibility(View.INVISIBLE);
//      stepper_loader_list_progressbar.setVisibility(View.VISIBLE);
//      stepper_loader_user.setChecked(true);

      dataLoader.getFolders();

//    }, 2000L);
  }

  @Override
  public void onGetUserInformationError(Throwable error) {
    Toast.makeText( this, String.format( "onError: Error %s", error.getMessage() ), Toast.LENGTH_SHORT).show();
    stepper.setStepAsUncompleted(1);
    stepper.goToPreviousStep();
  }

  @Override
  public void onGetDocumentsCountSuccess() {
    new Handler().postDelayed( () -> {

      stepper_loader_list_progressbar.setVisibility(View.INVISIBLE);
      stepper_loader_info_progressbar.setVisibility(View.VISIBLE);
      stepper_loader_list.setChecked(true);

      dataLoader.getDocumentsInfo();

    }, 2000L);
  }

  @Override
  public void onGetDocumentsCountError(Throwable error) {
    Toast.makeText( this, String.format( "onError: Error %s", error.getMessage() ), Toast.LENGTH_SHORT).show();
    stepper.setStepAsUncompleted(1);
    stepper.goToPreviousStep();
  }

  @Override
  public void onGetDocumentsInfoSuccess() {
    dataLoader.getFavorites();
  }

  @Override
  public void onGetDocumentsInfoError(Throwable error) {
    Toast.makeText( this, String.format( "onError: Error %s", error.getMessage() ), Toast.LENGTH_SHORT).show();
    stepper.setStepAsUncompleted(1);
    stepper.goToPreviousStep();
  }


  @Override
  public void onGetFoldersInfoSuccess() {
    dataLoader.getTemplates();
  }

  @Override
  public void onGetFoldersInfoError(Throwable error) {
    Toast.makeText( this, String.format( "onError: Error %s", error.getMessage() ), Toast.LENGTH_SHORT).show();
    stepper.setStepAsUncompleted(1);
    stepper.goToPreviousStep();
  }

  @Override
  public void onGetTemplatesInfoSuccess() {
    new Handler().postDelayed( () -> {

      stepper_loader_user_progressbar.setVisibility(View.INVISIBLE);
      stepper_loader_list_progressbar.setVisibility(View.VISIBLE);
      stepper_loader_user.setChecked(true);


      if ( Constant.DEBUG ) {
        start();
      } else {
        dataLoader.getDocumentsCount();
      }

    }, 2000L);
  }

  @Override
  public void onGetTemplatesInfoError(Throwable error) {
    Toast.makeText( this, String.format( "onError: Error %s", error.getMessage() ), Toast.LENGTH_SHORT).show();
    stepper.setStepAsUncompleted(1);
    stepper.goToPreviousStep();
  }

  @Override
  public void onGetFavoritesInfoSuccess() {
    dataLoader.getProcessed();
  }

  @Override
  public void onGetFavoritesInfoError(Throwable error) {
    Toast.makeText( this, String.format( "onError: Error %s", error.getMessage() ), Toast.LENGTH_SHORT).show();
    stepper.setStepAsUncompleted(1);
    stepper.goToPreviousStep();
  }

  @Override
  public void onGetProcessedInfoSuccess() {
    new Handler().postDelayed( () -> {

      stepper_loader_info_progressbar.setVisibility(View.INVISIBLE);
      stepper_loader_info.setChecked(true);

      stepper.setActiveStepAsCompleted();
      new Handler().postDelayed( () -> {
        stepper.goToNextStep();
      }, 500L);

    }, 2000L);
  }

  @Override
  public void onGetProcessedInfoError(Throwable error) {
    Toast.makeText( this, String.format( "onError: Error %s", error.getMessage() ), Toast.LENGTH_SHORT).show();
    stepper.setStepAsUncompleted(1);
    stepper.goToPreviousStep();
  }

  private void printJobStat() {
    Timber.tag(TAG).v( "JOB TOTAL: %s/%s [ %s ]", jobManager.getJobManagerExecutionThread().getState(), jobManager.countReadyJobs(), jobManager.getActiveConsumerCount() );
  }

  @Subscribe(threadMode = ThreadMode.POSTING)
  public void onMessageEvent(MarkDocumentAsChangedJobEvent event) {
    Timber.tag(TAG).v( "JOB TOTAL %s", event.uid );
  }

  @Subscribe(threadMode = ThreadMode.POSTING)
  public void onMessageEvent(FileDownloadedEvent event) {
    printJobStat();
  }

}

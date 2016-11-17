package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ernestoyaquello.com.verticalstepperform.VerticalStepperFormLayout;
import ernestoyaquello.com.verticalstepperform.interfaces.VerticalStepperForm;
import okhttp3.OkHttpClient;
import rx.Observable;
import rx.Subscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.retrofit.models.AuthToken;
import sapotero.rxtest.views.interfaces.DataLoaderInterface;
import sapotero.rxtest.views.services.AuthService;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity implements VerticalStepperForm, DataLoaderInterface.Callback {

  @BindView(R.id.stepper_form) VerticalStepperFormLayout stepper;

  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;

  private Observable<AuthToken> user;
  private Subscription subscription = null;
  private String TAG = this.getClass().getSimpleName();
  private EditText name;

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
  private NumberProgressBar load_progress;
  private LinearLayout load_wrapper;

  private DataLoaderInterface DataLoader;
  private CheckBox stepper_loader_user;
  private CheckBox stepper_loader_list;
  private CheckBox stepper_loader_info;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    DataLoader = new DataLoaderInterface(this);
    DataLoader.registerCallBack(this);


    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    initialize();

    String[] steps = {"Авторизация", "Загрузка данных"};
    String[] subtitles = {"введите данные", null};
    VerticalStepperFormLayout.Builder.newInstance(stepper, steps, this, this)
      .primaryColor( Color.RED )
      .primaryDarkColor( Color.GRAY )
      .displayBottomNavigation(false)
      .materialDesignInDisabledSteps(true)
      .showVerticalLineWhenStepsAreCollapsed(true)
      .stepsSubtitles(subtitles)
      .init();

    startService(new Intent(this, AuthService.class));
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

  }

  public void tryToLogin() {
    getCredentials();

  }

  public void getCredentials(){

    wrapper.setAlpha(0.25f);
    progress.setVisibility(View.VISIBLE);

    saveSettings("");

    DataLoader.getAuthToken();

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
        view = loadData();
        break;
    }
    return view;
  }

  public void onStepOpening(int stepNumber) {
    switch (stepNumber) {
      case 0:
        checkLogin();
        break;
      case 1:
        stepper.setActiveStepAsUncompleted();
        break;
      case 2:
        stepper.setStepAsCompleted(2);
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

    load_progress = ButterKnife.findById(view, R.id.number_progress_bar);
    load_progress.setMax(100);

    stepper_loader_user = ButterKnife.findById(view, R.id.stepper_loader_user );
    stepper_loader_list = ButterKnife.findById(view, R.id.stepper_loader_list );
    stepper_loader_info = ButterKnife.findById(view, R.id.stepper_loader_info );

    new Handler().postDelayed( () -> DataLoader.getUserInformation(), 2000L);

//    TextView text_test = ButterKnife.findById(view, R.id.text_test);
//    Button button_test = ButterKnife.findById(view, R.id.button_test);

//    Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
//    Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
//
//    button_test.setOnClickListener(v -> {
//      Timber.e("button_test");
//      load_progress.setProgress((int) (Math.random()*20));
//
////      text_test.startAnimation(slide_down);
//    });
//    password = ButterKnife.findById(view, R.id.stepper_login_password);
//    host     = ButterKnife.findById(view, R.id.stepper_login_host);

    return view;
  }

  private boolean checkName( String name) {
    boolean titleIsCorrect = false;
    if(name.length() >= 3 && name.length() <= 40) {
      titleIsCorrect = true;
      stepper.setActiveStepAsCompleted();
    } else {
      // This error message is optional (use null if you don't want to display an error message)
      String errorMessage = "The name must have between 3 and 40 characters";
      stepper.setActiveStepAsUncompleted(errorMessage);
    }
    return titleIsCorrect;
  }

  @Override
  public void sendData() {

  }

  @Override
  public void onAuthTokenSuccess() {

    new Handler().postDelayed( () -> {
      progress.setVisibility(View.GONE);
      stepper.setActiveStepAsCompleted();
      stepper.goToNextStep();
      Timber.i( "LOGIN: %s\nTOKEN: %s", LOGIN.get(), TOKEN.get() );
    }, 2000L);
  }

  @Override
  public void onAuthTokenError(Throwable error) {
    Toast.makeText( this, String.format( "onError: Error %s", error.getMessage() ), Toast.LENGTH_SHORT).show();
    resetLoginForm();
  }

  @Override
  public void onGetUserInformationSuccess() {
    stepper_loader_user.setChecked(true);
  }

  @Override
  public void onGetUserInformationError(Throwable error) {
    Toast.makeText( this, String.format( "onError: Error %s", error.getMessage() ), Toast.LENGTH_SHORT).show();
    stepper.setStepAsUncompleted(1);
    stepper.goToPreviousStep();
  }
}

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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import retrofit2.Retrofit;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.retrofit.AuthTokenService;
import sapotero.rxtest.retrofit.models.AuthToken;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import sapotero.rxtest.views.services.AuthService;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity implements VerticalStepperForm {

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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);


    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    loadSettings();

    String[] steps = {"Авторизация", "Загрузка данных пользователя"};
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

  @Override
  public void onStop() {
    super.onStop();

  }

  private void loadSettings() {
    LOGIN    = settings.getString("login");
    PASSWORD = settings.getString("password");
    TOKEN    = settings.getString("token");
    HOST     = settings.getString("settings_username_host");
  }

  public void tryToLogin() {
    getCredentials();

  }

  public void getCredentials(){

    wrapper.setAlpha(0.25f);
    username.setCursorVisible(false);
    progress.setVisibility(View.VISIBLE);

    Retrofit retrofit = new RetrofitManager( this, HOST.get(), okHttpClient).process();
    AuthTokenService authTokenService = retrofit.create( AuthTokenService.class );

    if (button != null){
      stepper.setActiveStepAsUncompleted();
    }
    user = authTokenService.getAuth( LOGIN.get(), PASSWORD.get() );

    if (subscription != null){
      subscription.unsubscribe();
    }

    subscription = user.subscribeOn( Schedulers.newThread() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data -> {


          saveSettings( data.getAuthToken() );

          new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
              stepper.setActiveStepAsCompleted();
              stepper.goToNextStep();
              Timber.i( "LOGIN: %s\nTOKEN: %s", LOGIN.get(), TOKEN.get() );
            }
          }, 2000L);

//          Intent intent = new Intent(this, MainActivity.class);
//          startActivity(intent);
//
//          finish();
        },
        error -> {
          Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
        }
      );
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
//
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

    progress.setVisibility(View.GONE);
    wrapper.setAlpha(1.0f);
    wrapper.setBackgroundColor( getResources().getColor(R.color.transparent) );

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
//    username = ButterKnife.findById(view, R.id.stepper_login_username);
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
}

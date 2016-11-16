package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import javax.inject.Inject;

import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import rx.Observable;
import rx.Subscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.retrofit.models.AuthToken;
import sapotero.rxtest.views.services.AuthService;

public class LoginActivity extends AppCompatActivity {

//  @BindView(R.id.stepper_form) VerticalStepperFormLayout stepper;

  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;

  private Preference<String> HOST;

  private Observable<AuthToken> user;
  private Subscription subscription = null;
  private String TAG = this.getClass().getSimpleName();
  private EditText name;

  // login view
  private View view;
  private TextView username;
  private TextView password;
  private TextView host;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);


    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    String[] steps = {"Авторизация", "Загрузка данных пользователя", "Загрузка задач"};
    String[] subtitles = {"введите данные", "subtitle", "subtitle"};

//    VerticalStepperFormLayout.Builder.newInstance(stepper, steps, this, this)
//      .primaryColor( Color.RED )
//      .primaryDarkColor( Color.GRAY )
//      .displayBottomNavigation(false)
//      .materialDesignInDisabledSteps(true)
//      .showVerticalLineWhenStepsAreCollapsed(true)
//      .stepsSubtitles(subtitles)
//      .init();

    startService(new Intent(this, AuthService.class));
  }

  @Override
  public void onStop() {
    super.onStop();

  }

  public void tryToLogin(View view) {
//    LOADER.setVisibility(ProgressBar.VISIBLE);
    getCredentials();

  }

  public void getCredentials(){
//    Retrofit retrofit = new RetrofitManager( this, HOST.get(), okHttpClient).process();
//    AuthTokenService authTokenService = retrofit.create( AuthTokenService.class );
//
//    user = authTokenService.getAuth( LOGIN.getText().toString(), PASSWORD.getText().toString() );
//
//    if (subscription != null){
//      subscription.unsubscribe();
//    }
//
//    subscription = user.subscribeOn( Schedulers.newThread() )
//      .observeOn( AndroidSchedulers.mainThread() )
//      .subscribe(
//        data -> {
//          String _token = data.getAuthToken();
//          LOADER.setVisibility(ProgressBar.INVISIBLE);
//
//          saveSettings(_token);
//
//          Intent intent = new Intent(this, MainActivity.class);
//          startActivity(intent);
//
//          finish();
//        },
//        error -> {
//          Log.d( "_ERROR", error.getMessage() );
//          LOADER.setVisibility(ProgressBar.INVISIBLE);
//          Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//      );
  }

  private void saveSettings(String TOKEN) {
//    Preference<String> username = settings.getString("login");
//    username.set(LOGIN.getText().toString());
//
//    Preference<String> password = settings.getString("password");
//    password.set(PASSWORD.getText().toString());
//
//    Preference<String> token = settings.getString("token");
//    token.set(TOKEN);
//
//    HOST.set( HOST_INPUT.getText().toString() );
//
//    Preference<Integer> updated = settings.getInteger("date");
//    updated.set( (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) );
  }
//
//
//  public View createStepContentView(int stepNumber) {
//    View view = null;
//    switch (stepNumber) {
//      case 0:
//        view = loginForm();
//        break;
//      case 1:
//        view = createNameStep();
//        break;
//      case 2:
//        view = createNameStep();
//        break;
//    }
//    return view;
//  }
//
//  public void onStepOpening(int stepNumber) {
//    switch (stepNumber) {
//      case 0:
//        checkLogin();
//        break;
//      case 1:
//        checkName( name.getText().toString() );
//        break;
//      case 2:
//        stepper.setStepAsCompleted(2);
//        break;
//    }
//  }
//
//  private Boolean checkLogin( ) {
//    boolean isCorrect = false;
//
//    if( host.length() >0 && username.length() > 0 && password.length() > 0 ) {
//      isCorrect = true;
//      stepper.setActiveStepAsCompleted();
//    } else {
//      String titleError = "Введите данные!";
//      stepper.setActiveStepAsUncompleted(titleError);
//    }
//
//    return isCorrect;
//  }
//
//  private View loginForm() {
//
//    view = LayoutInflater.from(this).inflate(R.layout.stepper_login, null);
//    username = ButterKnife.findById(view, R.id.username);
//    password = ButterKnife.findById(view, R.id.password);
//    host     = ButterKnife.findById(view, R.id.host);
//
//
//    HOST = settings.getString("settings_username_host");
//
//    if (Objects.equals(HOST.get(), null)){
//      Timber.tag(TAG).i("EMPTY HOST");
//      HOST.set( Constant.HOST );
//    }
//
//    host.setText( HOST.get() );
//
//    TextWatcher watcher = new TextWatcher(){
//
//      @Override
//      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//      }
//
//      @Override
//      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//        checkLogin();
//      }
//
//      @Override
//      public void afterTextChanged(Editable editable) {
//      }
//    };
//
//    username.addTextChangedListener(watcher);
//    password.addTextChangedListener(watcher);
//    host.addTextChangedListener(watcher);
//
//    return view;
//  }
//
//  private View createNameStep() {
//    // Here we generate programmatically the view that will be added by the system to the step content layout
//    name = new EditText(this);
//    name.setSingleLine(true);
//    name.setHint("Your name");
//    name.addTextChangedListener(new TextWatcher() {
//      @Override
//      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//      @Override
//      public void onTextChanged(CharSequence s, int start, int before, int count) {
//        Timber.d( String.valueOf( s ) );
//        checkName( s.toString() );
//      }
//
//      @Override
//      public void afterTextChanged(Editable s) {}
//    });
//    name.setOnEditorActionListener(
//      (v, actionId, event) -> {
//        Timber.d( String.valueOf(checkName(name.getText().toString())) );
//        if(checkName(name.getText().toString())) {
//          stepper.goToNextStep();
//        }
//        return false;
//      });
//    return name;
//  }
//
//  private boolean checkName( String name) {
//    boolean titleIsCorrect = false;
//    if(name.length() >= 3 && name.length() <= 40) {
//      titleIsCorrect = true;
//      stepper.setActiveStepAsCompleted();
//    } else {
//      // This error message is optional (use null if you don't want to display an error message)
//      String errorMessage = "The name must have between 3 and 40 characters";
//      stepper.setActiveStepAsUncompleted(errorMessage);
//    }
//    return titleIsCorrect;
//  }
//
//  @Override
//  public void sendData() {
//
//  }
}

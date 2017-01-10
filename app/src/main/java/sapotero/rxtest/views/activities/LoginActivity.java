package sapotero.rxtest.views.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
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
import sapotero.rxtest.events.service.AuthServiceAuthEvent;
import sapotero.rxtest.jobs.service.AuthServiceCheckSignJob;
import sapotero.rxtest.utils.ProviderType;
import sapotero.rxtest.views.interfaces.DataLoaderInterface;
import sapotero.rxtest.views.services.AuthService;
import sapotero.rxtest.views.views.LoginView;
import sapotero.rxtest.views.views.utils.VerticalStepperForm;
import timber.log.Timber;


public class LoginActivity extends Activity implements VerticalStepperForm, DataLoaderInterface.Callback ,AdapterView.OnItemSelectedListener {


  private static int REQUEST_READWRITE_STORAGE = 0;

//  @BindView(R.id.spExamplesList) Spinner spExamplesList;
//  @BindView(R.id.spExamplesClientList) Spinner spClientList;
//  @BindView(R.id.spExamplesServerList) Spinner spServerList;
//  @BindView(R.id.etExamplesClientPassword) EditText etClientPin;
//  @BindView(R.id.cbExamplesInstallCA) CheckBox cbInstallCA;

//  @BindView(R.id.stepper_auth_choose_cert) Button stepper_auth_choose_cert;
//  @BindView(R.id.stepper_auth_choose_password) Button stepper_auth_choose_password;

  @BindView(R.id.stepper_form) LoginView stepper;


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

  private String[] examplesRequireWrittenPin;
  private String[] examplesRequireServerContainer;
  private String[] exampleClassesToBeExecuted;
  private ArrayAdapter<String> containerAliasAdapter;
  private List<String> aliasesList;
  private String secret_password;
  private MaterialDialog root;
  private MaterialDialog.Builder root1;
  private boolean isCorrect;

//  private RelativeLayout finishLayout;

  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    startService(new Intent(this, AuthService.class));

    dataLoader = new DataLoaderInterface(this);
    dataLoader.registerCallBack(this);


    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    initialize();
    check_permissions();

    String[] steps = {"Выбор способа авторизации", "Авторизация", "Загрузка данных"};

//    String[] subtitles = {null, "введите данные", null};
    LoginView.Builder.newInstance(stepper, steps, this, this)
      .primaryColor( Color.RED )
      .primaryDarkColor( ContextCompat.getColor( this, R.color.md_blue_grey_200 ) )
      .displayBottomNavigation(false)
      .materialDesignInDisabledSteps(true)
      .showVerticalLineWhenStepsAreCollapsed(true)
//      .stepsSubtitles(subtitles)
      .init();

      EventBus.getDefault().register(this);

      AuthService.setCSP();


    examplesRequireWrittenPin = getResources().getStringArray(R.array.ExampleRequireWrittenPin);
    examplesRequireServerContainer = getResources().getStringArray(R.array.ExampleRequireServerContainer);
    exampleClassesToBeExecuted = getResources().getStringArray(R.array.ExampleClasses);

  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode == REQUEST_READWRITE_STORAGE) {
      if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//        addKey();
      }
    }
  }

//  @Nullable
//  @OnClick(R.id.stepper_auth_choose_cert)
//  void initAuthServiceSign(View view) {
//    Timber.tag(TAG).i("by cert");
//  }
//
//  @Nullable
//  @OnClick(R.id.stepper_auth_choose_password)
//  void initAuthServiceLogin(View view) {
//    Timber.tag(TAG).i("by password");
//  }


  private static final String EXAMPLE_PACKAGE = "sapotero.rxtest.utils.";

  private void check_permissions(){
    // Here, thisActivity is the current activity
    if (ContextCompat.checkSelfPermission( this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

      // Should we show an explanation?
      if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

        // Show an explanation to the user *asynchronously* -- don't block
        // this thread waiting for the user's response! After the user
        // sees the explanation, try again to request the permission.

      } else {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READWRITE_STORAGE);
      }
    }
  }

  @Override
  public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    ProviderType.currentProviderType();

    switch (adapterView.getId()) {

//      case R.id.spExamplesList: {
//
//        // Выбранный пример.
//        int selectedExample = adapterView.getSelectedItemPosition();
//        String exampleClass = exampleClassesToBeExecuted[selectedExample];
//
//        // Если пример не требует ввода пина, то блокируем поле ввода пина.
//        // Большинство примеров вообще не нуждается в указании пароля, т.к.
//        // он будет запрошен в специальном окне. Только TLS примеры требуют
//        // клиентский пароль.
//        List<String> exampleRWPClasses = Arrays.asList(examplesRequireWrittenPin);
//
//        if (!exampleRWPClasses.contains(exampleClass)) {
////          etClientPin.setText("");
////          etClientPin.setEnabled(false);
//        } // if
//        else {
////          etClientPin.setEnabled(true);
////          etClientPin.setSelected(true);
////          etClientPin.requestFocus();
//        } // else
//
//        // Если пример требует указания алиаса и пина получателя, то
//        // разблокируем поле ввода алиаса и пина получателя. Например,
//        // примеры шифрования нуждаются в них.
//
////        List<String> exampleRSCClasses = Arrays.asList(examplesRequireServerContainer);
////        if (!exampleRSCClasses.contains(exampleClass)) {
////          spServerList.setEnabled(false);
////        } // if
////        else {
////          spServerList.setEnabled(true);
////        } // else
//
//        // Флаг, означающий, что выполняется CAdES пример.
//        boolean isCAdESExampleExecuting =
//          exampleClass.contains("CAdESBES") ||
//            exampleClass.contains("CAdESXLT1");
//
//        // Выполняем установку корневых сертификатов для CAdES
//        // примеров, если она не была уже произведена. Фактически,
//        // перед каждым первым выполнением CAdES примера после запуска
//        // приложения.
//
//        if (cbInstallCA.isChecked() && isCAdESExampleExecuting) {
////          checkCAdESCACertsAndInstall();
//        } // if
//
//      }
//      break;
//
//      case R.id.spExamplesClientList: {
//        etClientPin.setText(""); // очистка
//      }
//      break;

    } // switch

  }

  @Override
  public void onNothingSelected(AdapterView<?> adapterView) {
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
        view = authTypeCheck();
        break;
      case 1:
        view = loginForm();
        break;
      case 2:
        view = loadData();
        break;
      case 3:
        view = showFinal();
        break;
    }
    return view;
  }

  private View showFinal() {
    view = LayoutInflater.from(this).inflate(R.layout.stepper_load_data, null);

    return view;
  }


  private View authTypeCheck() {
    view = LayoutInflater.from(this).inflate(R.layout.stepper_auth_chooser, null);

    AppCompatButton choose_cert = ButterKnife.findById(view, R.id.stepper_auth_choose_cert);
    AppCompatButton choose_pass = ButterKnife.findById(view, R.id.stepper_auth_choose_password);

    choose_cert.setOnClickListener(v -> {
      Timber.e("cert");

      root = new MaterialDialog.Builder(this)
        .title(R.string.dialog_cert_pass_title)
        .content(R.string.dialog_cert_description)
        .positiveText(R.string.dialog_cert_positive)
        .negativeText(R.string.dialog_cert_negative)
        .cancelable(false)
        .autoDismiss(false)


        .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
        .input(R.string.dialog_cert_pass_hint, R.string.dialog_cert_pass_prefill, (dialog, input) -> {
          jobManager.addJobInBackground( new AuthServiceCheckSignJob( input.toString() ) );
        })

        .checkBoxPromptRes(R.string.dialog_cert_pass_checkbox_save, false, null)

        .onPositive((dialog, which) -> {

        })
        .onNegative((dialog, which) -> dialog.hide())
        .show();

    });

    choose_pass.setOnClickListener(v -> {
      Timber.e("pass");
      stepper.setActiveStepAsCompleted();
      stepper.goToNextStep();
    });

    try {
      stepper.findViewById(R.id.next_step).setVisibility(View.GONE);
    } catch (Exception e){
      Timber.d(e);
    }

    return view;
  }


  public void onStepOpening(int stepNumber) {
    switch (stepNumber) {
      case 0:
        break;
      case 1:
        checkLogin();
        break;
      case 2:
        resetLoadDataForm();
        break;
      case 3:
        break;
    }
  }


  private Boolean checkLogin( ) {
//    isCorrect = false;


    resetLoginForm();

    if( host.length() >= 0 && username.length() >= 0 && password.length() >= 0 ) {
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

    Timber.e("checkLogin result %s", isCorrect);


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
        Timber.e("checkLogin");
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
    progress.setVisibility(View.GONE);
    stepper.setActiveStepAsCompleted();
    stepper.goToNextStep();
    Timber.i( "LOGIN: %s\nTOKEN: %s", LOGIN.get(), TOKEN.get() );
  }


  @Override
  public void onGetUserInformationSuccess() {
    new Handler().postDelayed( () -> {
      stepper_loader_user_progressbar.setVisibility(View.INVISIBLE);
      stepper_loader_list_progressbar.setVisibility(View.VISIBLE);
      stepper_loader_user.setChecked(true);
    }, 2000L);
  }

  @Override
  public void onGetDocumentsCountSuccess() {
    Timber.tag(TAG).i("LOADED onGetDocumentsCountSuccess");
//    new Handler().postDelayed( () -> {
//
//      stepper_loader_list_progressbar.setVisibility(View.INVISIBLE);
//      stepper_loader_info_progressbar.setVisibility(View.VISIBLE);
//      stepper_loader_list.setChecked(true);
//
//      dataLoader.getDocumentsInfo();
//
//    }, 2000L);
  }

  @Override
  public void onGetDocumentsInfoSuccess() {
    Timber.tag(TAG).i("LOADED onGetDocumentsInfoSuccess");
//    dataLoader.getFavorites();
  }


  @Override
  public void onGetFoldersInfoSuccess() {
    Timber.tag(TAG).i("LOADED onGetFoldersInfoSuccess");

//    dataLoader.getTemplates();
  }

  @Override
  public void onGetTemplatesInfoSuccess() {
    Timber.tag(TAG).i("LOADED onGetTemplatesInfoSuccess");

//    new Handler().postDelayed( () -> {
//
//      stepper_loader_user_progressbar.setVisibility(View.INVISIBLE);
//      stepper_loader_list_progressbar.setVisibility(View.VISIBLE);
//      stepper_loader_user.setChecked(true);
//
      if ( Constant.DEBUG ) {
        start();
      }
//
//    }, 2000L);
  }


  @Override
  public void onGetFavoritesInfoSuccess() {
    Timber.tag(TAG).i("LOADED onGetFavoritesInfoSuccess");

//    dataLoader.getProcessed();
  }

  @Override
  public void onGetProcessedInfoSuccess() {
    Timber.tag(TAG).i("LOADED onGetProcessedInfoSuccess");

//    new Handler().postDelayed( () -> {
//
    stepper_loader_info_progressbar.setVisibility(View.INVISIBLE);
    stepper_loader_info.setChecked(true);

//    stepper.goToNextStep();
    stepper.setStepAsCompleted(0);
    stepper.setStepAsCompleted(1);
    stepper.setStepAsCompleted(2);
    stepper.goToStep(3, false);
//
//    }, 2000L);
  }

  @Override
  public void onError(Throwable error) {
    Toast.makeText( this, String.format( "onError: Error %s", error.getMessage() ), Toast.LENGTH_SHORT).show();
    stepper.setStepAsUncompleted(0);
    stepper.setStepAsUncompleted(1);
    stepper.setStepAsUncompleted(2);
    stepper.goToStep(0, false);
  }



  private void printJobStat() {
    Timber.tag(TAG).v( "JOB TOTAL: %s/%s [ %s ]", jobManager.getJobManagerExecutionThread().getState(), jobManager.countReadyJobs(), jobManager.getActiveConsumerCount() );
  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(MarkDocumentAsChangedJobEvent event) {
    Timber.tag(TAG).v( "JOB TOTAL %s", event.uid );
  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(FileDownloadedEvent event) {
    printJobStat();
  }

  @Subscribe(threadMode = ThreadMode.POSTING)
  public void onMessageEvent(AuthServiceAuthEvent event) {
    Timber.tag(TAG).i("RECV AuthServiceAuthEvent: %s %s", event.success, event.success_string );

    root.dismiss();
    isCorrect = true;

    stepper.setStepAsCompleted(0);
    stepper.setStepAsCompleted(1);
    tryToLogin();
    stepper.goToStep(2, true);


//      stepper.setActiveStepAsCompleted();
//      stepper.goToNextStep();
//      stepper.setActiveStepAsCompleted();
//      stepper.goToNextStep();

//    root.set("DONE");
  }




}

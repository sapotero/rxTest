package sapotero.rxtest.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import ru.CryptoPro.JCSP.support.BKSTrustStore;
import ru.cprocsp.ACSP.tools.common.Constants;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.events.bus.FileDownloadedEvent;
import sapotero.rxtest.events.bus.MarkDocumentAsChangedJobEvent;
import sapotero.rxtest.events.service.AuthServiceAuthEvent;
import sapotero.rxtest.events.service.AuthServiceAuthSignInEvent;
import sapotero.rxtest.utils.ContainerAdapter;
import sapotero.rxtest.utils.IHashData;
import sapotero.rxtest.utils.ProviderType;
import sapotero.rxtest.views.interfaces.DataLoaderInterface;
import sapotero.rxtest.views.services.AuthService;
import sapotero.rxtest.views.views.VerticalStepperFormLayout;
import sapotero.rxtest.views.views.utils.VerticalStepperForm;
import timber.log.Timber;


public class LoginActivity extends Activity implements VerticalStepperForm, DataLoaderInterface.Callback ,AdapterView.OnItemSelectedListener {


  private static int REQUEST_READWRITE_STORAGE = 0;

  @BindView(R.id.spExamplesList) Spinner spExamplesList;
  @BindView(R.id.spExamplesClientList) Spinner spClientList;
  @BindView(R.id.spExamplesServerList) Spinner spServerList;
  @BindView(R.id.etExamplesClientPassword) EditText etClientPin;
  @BindView(R.id.cbExamplesInstallCA) CheckBox cbInstallCA;

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

  private String[] examplesRequireWrittenPin;
  private String[] examplesRequireServerContainer;
  private String[] exampleClassesToBeExecuted;
  private ArrayAdapter<String> containerAliasAdapter;
  private List<String> aliasesList;
  private String secret_password;

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

    String[] steps = {"Авторизация", "Загрузка данных"};
    String[] subtitles = {"введите данные", null};
    VerticalStepperFormLayout.Builder.newInstance(stepper, steps, this, this)
      .primaryColor( Color.RED )
      .primaryDarkColor( ContextCompat.getColor( this, R.color.md_blue_grey_200 ) )
      .displayBottomNavigation(false)
      .materialDesignInDisabledSteps(true)
      .showVerticalLineWhenStepsAreCollapsed(true)
      .stepsSubtitles(subtitles)
      .init();

      EventBus.getDefault().register(this);

//    getSystemService( AuthService.class ).setCSP();
      AuthService.setCSP();
//
//    // 2. Инициализация провайдеров: CSP и java-провайдеров (Обязательная часть).
//
//    if (!initCSPProviders()) {
//      Log.i(Constants.APP_LOGGER_TAG, "Couldn't initialize CSP.");
//    }
//    initJavaProviders();
//    initLogger();
//
////    installContainers();
//
//    // 4. Инициируем объект для управления выбором типа
//    // контейнера (Настройки).
//
//    KeyStoreType.init(this);
//
//    // 5. Инициируем объект для управления выбором типа
//    // провайдера (Настройки).
//
//    ProviderType.init(this);
//
//    logJCspServices(defaultKeyStoreProvider = new JCSP());

    examplesRequireWrittenPin = getResources().getStringArray(R.array.ExampleRequireWrittenPin);
    examplesRequireServerContainer = getResources().getStringArray(R.array.ExampleRequireServerContainer);
    exampleClassesToBeExecuted = getResources().getStringArray(R.array.ExampleClasses);


    // Создаем ArrayAdapter для использования строкового массива
    // и способа отображения объекта.
//    ArrayAdapter<CharSequence> examplesAdapter = ArrayAdapter.createFromResource( this, R.array.ExamplesDescription, android.R.layout.simple_spinner_item);
//
//    // Способ отображения.
//
//    examplesAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
//
//    spExamplesList.setAdapter(examplesAdapter);
//    spExamplesList.setOnItemSelectedListener(this);
//    // Для логирования: CSPConfig.setNeedLogBioStatistics(true);
//
//    aliasesList = aliases( KeyStoreType.currentType(), ProviderType.currentProviderType());
//    containerAliasAdapter = new ArrayAdapter<String>( this, android.R.layout.simple_spinner_item, aliasesList);
//
//
//    spServerList.setAdapter(containerAliasAdapter);
//    spServerList.setOnItemSelectedListener(this);
//    // Способ отображения.
//
//    containerAliasAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
//
//    spClientList.setAdapter(containerAliasAdapter);
//    spClientList.setOnItemSelectedListener(this);
//
//
//    int permissionCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
//    int permissionCheck2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//    if (permissionCheck1 != PackageManager.PERMISSION_GRANTED || permissionCheck2 != PackageManager.PERMISSION_GRANTED) {
//      ActivityCompat.requestPermissions(this,
//        new String[]{
//          Manifest.permission.WRITE_EXTERNAL_STORAGE,
//          Manifest.permission.READ_EXTERNAL_STORAGE
//        },
//        REQUEST_READWRITE_STORAGE);
//    }
//


  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode == REQUEST_READWRITE_STORAGE) {
      if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//        addKey();
      }
    }
  }

  @OnClick(R.id.AuthServiceLogin)
  void initAuthServiceLogin(View view) {
    Timber.tag(TAG).i("initAuthServiceLogin");
    EventBus.getDefault().post( new AuthServiceAuthSignInEvent() );

//    int exampleIndex = spExamplesList.getSelectedItemPosition();
//    executeExample(exampleIndex, null);
  }

  @OnClick(R.id.AuthServiceSign)
  void initAuthServiceSign(View view) {
//    int exampleIndex = spExamplesList.getSelectedItemPosition();
//    executeExample(exampleIndex, null);
  }


  private static final String EXAMPLE_PACKAGE = "sapotero.rxtest.utils.";
  private void executeExample(int i, String secret_password) {
    try {

      Class exampleClass = Class.forName(EXAMPLE_PACKAGE + "SignIn");
      Constructor exampleConstructor = exampleClass.getConstructor(ContainerAdapter.class);

      // Клиентский контейнер (подписант, отправитель, TLS).
      String clientAlias = (String) spClientList.getSelectedItem();
      CharSequence clientPasswordSequence = etClientPin.getText();
      char[] clientPassword = null;

      if (clientPasswordSequence != null) {
        clientPassword = clientPasswordSequence.toString().toCharArray();
      } // if

      // Контейнер получателя.
      String serverAlias = (String) spServerList.getSelectedItem();

      // Настройки примера.

      ContainerAdapter adapter = new ContainerAdapter(clientAlias, secret_password != null ? secret_password.toCharArray() : clientPassword, serverAlias, null);

      adapter.setProviderType(ProviderType.currentProviderType());
      adapter.setResources(getResources()); // для примера установки сертификатов

      // Используется общее для всех хранилище корневых
      // сертификатов cacerts.

      final String trustStorePath = this.getApplicationInfo().dataDir + File.separator + BKSTrustStore.STORAGE_DIRECTORY + File.separator + BKSTrustStore.STORAGE_FILE_TRUST;

      Timber.e("Example trust store: " + trustStorePath);

      adapter.setTrustStoreProvider(BouncyCastleProvider.PROVIDER_NAME);
      adapter.setTrustStoreType(BKSTrustStore.STORAGE_TYPE);

      adapter.setTrustStoreStream(new FileInputStream(trustStorePath));
      adapter.setTrustStorePassword(BKSTrustStore.STORAGE_PASSWORD);

      // Выполнение примера.
      IHashData exampleImpl = (IHashData) exampleConstructor.newInstance(adapter);
//      exampleImpl.getResult(ca);



//      logCallback.log("Prepare client thread.");
//
//      ClientThread clientThread = new ClientThread(logCallback, task);
//      clientThread.setPriority(Thread.NORM_PRIORITY);
//
//      logCallback.log("Start client thread.");
//
//      clientThread.start();
//      clientThread.join(MAX_THREAD_TIMEOUT);
//
//      logCallback.log("Client thread finished job.");


    } catch (Exception e) {
      Log.e(Constants.APP_LOGGER_TAG, e.getMessage(), e);
    }

  }



  @Override
  public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    ProviderType.currentProviderType();

    switch (adapterView.getId()) {

      case R.id.spExamplesList: {

        // Выбранный пример.
        int selectedExample = adapterView.getSelectedItemPosition();
        String exampleClass = exampleClassesToBeExecuted[selectedExample];

        // Если пример не требует ввода пина, то блокируем поле ввода пина.
        // Большинство примеров вообще не нуждается в указании пароля, т.к.
        // он будет запрошен в специальном окне. Только TLS примеры требуют
        // клиентский пароль.
        List<String> exampleRWPClasses = Arrays.asList(examplesRequireWrittenPin);

        if (!exampleRWPClasses.contains(exampleClass)) {
          etClientPin.setText("");
          etClientPin.setEnabled(false);
        } // if
        else {
          etClientPin.setEnabled(true);
          etClientPin.setSelected(true);
          etClientPin.requestFocus();
        } // else

        // Если пример требует указания алиаса и пина получателя, то
        // разблокируем поле ввода алиаса и пина получателя. Например,
        // примеры шифрования нуждаются в них.

//        List<String> exampleRSCClasses = Arrays.asList(examplesRequireServerContainer);
//        if (!exampleRSCClasses.contains(exampleClass)) {
//          spServerList.setEnabled(false);
//        } // if
//        else {
//          spServerList.setEnabled(true);
//        } // else

        // Флаг, означающий, что выполняется CAdES пример.
        boolean isCAdESExampleExecuting =
          exampleClass.contains("CAdESBES") ||
            exampleClass.contains("CAdESXLT1");

        // Выполняем установку корневых сертификатов для CAdES
        // примеров, если она не была уже произведена. Фактически,
        // перед каждым первым выполнением CAdES примера после запуска
        // приложения.

        if (cbInstallCA.isChecked() && isCAdESExampleExecuting) {
//          checkCAdESCACertsAndInstall();
        } // if

      }
      break;

      case R.id.spExamplesClientList: {
        etClientPin.setText(""); // очистка
      }
      break;

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
    Timber.e("checkLogin");
    boolean isCorrect = false;

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
//    dataLoader.getFavorites();
  }


  @Override
  public void onGetFoldersInfoSuccess() {
//    dataLoader.getTemplates();
  }

  @Override
  public void onGetTemplatesInfoSuccess() {
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
//    dataLoader.getProcessed();
  }

  @Override
  public void onGetProcessedInfoSuccess() {
//    new Handler().postDelayed( () -> {
//
    stepper_loader_info_progressbar.setVisibility(View.INVISIBLE);
    stepper_loader_info.setChecked(true);

    stepper.setActiveStepAsCompleted();
    stepper.goToNextStep();
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

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(AuthServiceAuthEvent event) {
    Timber.tag(TAG).i("RECV AuthServiceAuthEvent: %s %s", event.success, event.success_string );
  }




}

package sapotero.rxtest.views.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.events.bus.FileDownloadedEvent;
import sapotero.rxtest.events.bus.MarkDocumentAsChangedJobEvent;
import sapotero.rxtest.events.stepper.StepperNextStepEvent;
import sapotero.rxtest.utils.ProviderType;
import sapotero.rxtest.views.interfaces.DataLoaderInterface;
import sapotero.rxtest.views.services.AuthService;
import sapotero.rxtest.views.views.LoginView;
import sapotero.rxtest.views.views.stepper.StepperLayout;
import sapotero.rxtest.views.views.stepper.VerificationError;
import sapotero.rxtest.views.views.stepper.build.StepperAdapter;
import timber.log.Timber;


public class LoginActivity extends AppCompatActivity implements DataLoaderInterface.Callback ,AdapterView.OnItemSelectedListener, StepperLayout.StepperListener {


  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject JobManager jobManager;

  @BindView(R.id.stepper_form) LoginView stepper;

  private String TAG = this.getClass().getSimpleName();

  private static int REQUEST_READWRITE_STORAGE = 0;

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> PASSWORD;
  private Preference<String> HOST;

  private DataLoaderInterface dataLoader;

  private StepperLayout stepperLayout;
  private StepperAdapter adapter;

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

//    String[] steps = {"Выбор способа авторизации", "Авторизация", "Загрузка данных"};
//
//    LoginView.Builder.newInstance(stepper, steps, this, this)
//      .primaryColor( Color.RED )
//      .primaryDarkColor( ContextCompat.getColor( this, R.color.md_blue_grey_200 ) )
//      .displayBottomNavigation(false)
//      .materialDesignInDisabledSteps(true)
//      .showVerticalLineWhenStepsAreCollapsed(true)
//      .init();

    EventBus.getDefault().register(this);

    AuthService.setCSP();

    stepperLayout = (StepperLayout) findViewById(R.id.stepperLayout);
    adapter = new StepperAdapter( getSupportFragmentManager() );
    stepperLayout.setAdapter( adapter );
    stepperLayout.setListener(this);

//    new Handler().postDelayed( () -> {
//      stepperLayout.getmNextNavigationButton().performClick();
//    }, 5000L);

  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode == REQUEST_READWRITE_STORAGE) {
      if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//        addKey();
      }
    }
  }


  private void check_permissions(){
    if (ContextCompat.checkSelfPermission( this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

      if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

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


  private void start() {
    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);

    finish();
  }


  @Override
  public void onAuthTokenSuccess() {
//    progress.setVisibility(View.GONE);
    stepper.setActiveStepAsCompleted();
    stepper.goToNextStep();
    Timber.i( "LOGIN: %s\nTOKEN: %s", LOGIN.get(), TOKEN.get() );
  }


  @Override
  public void onGetUserInformationSuccess() {
    new Handler().postDelayed( () -> {
//      stepper_loader_user_progressbar.setVisibility(View.INVISIBLE);
//      stepper_loader_list_progressbar.setVisibility(View.VISIBLE);
//      stepper_loader_user.setChecked(true);
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
//    stepper_loader_info_progressbar.setVisibility(View.INVISIBLE);
//    stepper_loader_info.setChecked(true);

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

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(StepperNextStepEvent event) {
    stepperLayout.getmNextNavigationButton().performClick();
  }



  @Override
  public void onCompleted(View completeButton) {

  }

  @Override
  public void onError(VerificationError verificationError) {

  }

  @Override
  public void onStepSelected(int newStepPosition) {

  }

  @Override
  public void onReturn() {

  }
}

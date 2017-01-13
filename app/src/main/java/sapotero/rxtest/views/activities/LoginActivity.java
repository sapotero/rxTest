package sapotero.rxtest.views.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.events.bus.FileDownloadedEvent;
import sapotero.rxtest.events.bus.MarkDocumentAsChangedJobEvent;
import sapotero.rxtest.events.stepper.shared.StepperNextStepEvent;
import sapotero.rxtest.views.interfaces.DataLoaderInterface;
import sapotero.rxtest.views.services.MainService;
import sapotero.rxtest.views.views.stepper.StepperLayout;
import sapotero.rxtest.views.views.stepper.VerificationError;
import sapotero.rxtest.views.views.stepper.build.StepperAdapter;
import timber.log.Timber;


public class LoginActivity extends AppCompatActivity implements StepperLayout.StepperListener {


  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject JobManager jobManager;

//  @BindView(R.id.stepper_form) LoginView stepper;

  private String TAG = this.getClass().getSimpleName();

  private static int REQUEST_READWRITE_STORAGE = 0;

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> PASSWORD;
  private Preference<String> HOST;

  private DataLoaderInterface dataLoader;

  private StepperLayout stepperLayout;
  private StepperAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    EsdApplication.getComponent(this).inject(this);

    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);

    startService(new Intent(this, MainService.class));

    initialize();
    check_permissions();

    stepperLayout = (StepperLayout) findViewById(R.id.stepperLayout);
    adapter = new StepperAdapter( getSupportFragmentManager() );
    stepperLayout.setAdapter( adapter );
    stepperLayout.setListener(this);

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

  /* Stepper */
  @Override
  public void onCompleted(View completeButton) {
    Toast.makeText( getApplicationContext(), "onCompleted", Toast.LENGTH_SHORT ).show();

    Intent intent = new Intent(this, MainActivity.class);
    startActivity(intent);
    finish();
  }

  @Override
  public void onError(VerificationError verificationError) {
    Toast.makeText( getApplicationContext(), "onError", Toast.LENGTH_SHORT ).show();
  }

  @Override
  public void onStepSelected(int newStepPosition) {
    Toast.makeText( getApplicationContext(), "onStepSelected", Toast.LENGTH_SHORT ).show();
  }

  @Override
  public void onReturn() {
    Toast.makeText( getApplicationContext(), "onReturn", Toast.LENGTH_SHORT ).show();
  }

  /* Events */
  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(MarkDocumentAsChangedJobEvent event) {
    Timber.tag(TAG).v( "JOB TOTAL %s", event.uid );
  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(FileDownloadedEvent event) {
//    printJobStat();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(StepperNextStepEvent event) {
    stepperLayout.getmNextNavigationButton().performClick();
  }
}

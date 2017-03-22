package sapotero.rxtest.views.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
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
import sapotero.rxtest.events.stepper.shared.StepperNextStepEvent;
import sapotero.rxtest.utils.queue.QueueManager;
import sapotero.rxtest.views.custom.stepper.StepperLayout;
import sapotero.rxtest.views.custom.stepper.VerificationError;
import sapotero.rxtest.views.custom.stepper.build.StepperAdapter;
import sapotero.rxtest.services.MainService;


public class LoginActivity extends AppCompatActivity implements StepperLayout.StepperListener {


  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject JobManager jobManager;


  // test
  @Inject QueueManager queue;

  private String TAG = this.getClass().getSimpleName();

  private static int REQUEST_READWRITE_STORAGE = 0;

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> PASSWORD;
  private Preference<String> HOST;

  private StepperLayout stepperLayout;
  private StepperAdapter adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    EsdApplication.getComponent(this).inject(this);


    if( appInstalled("ru.cprocsp.ACSP") ) {

      startService(new Intent(this, MainService.class));

      initialize();
      check_permissions();

      initView();

    } else {

      new MaterialDialog.Builder(this)
        .title(R.string.error_csp_not_installed)
        .content(R.string.error_csp_not_installed_body)
        .positiveText(R.string.yes)
        .autoDismiss(false)
        .onPositive((dialog, which) -> {
          finish();
        })
        .show();
    }



//    queue.clean();

  }

  private boolean appInstalled(String uri) {
    PackageManager pm = getPackageManager();

    Boolean result = false;
    try {
      pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
      result = true;
    } catch (PackageManager.NameNotFoundException e) {
    }

    return result;
  }

  private void initView() {
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

    PreferenceManager.setDefaultValues(this, R.xml.settings_view, false);
  }

  @Override
  public void onStop() {
    super.onStop();

    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }

  }

  @Override
  public void onResume() {
    super.onResume();

    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);

  }



  /* Stepper */
  @Override
  public void onCompleted(View completeButton) {
    Toast.makeText( getApplicationContext(), "onCompleted", Toast.LENGTH_SHORT ).show();


    Intent intent = new Intent(this, MainActivity.class);

    Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
    startActivity(intent, bundle);

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


  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(FileDownloadedEvent event) {
//    printJobStat();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(StepperNextStepEvent event) {
    stepperLayout.getmNextNavigationButton().performClick();
  }
}

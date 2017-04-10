package sapotero.rxtest.views.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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

  private static final int PERM_REQUEST_CODE = 0;
  private static final int PERM_REQUEST_CODE_DRAW_OVERLAYS = 1;

  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject JobManager jobManager;


  // test
  @Inject QueueManager queue;

  private String TAG = this.getClass().getSimpleName();

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
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == PERM_REQUEST_CODE_DRAW_OVERLAYS) {
      if (android.os.Build.VERSION.SDK_INT >= 23) {   //Android M Or Over
        if (!Settings.canDrawOverlays(this)) {
          showScreenOverlayRationale();
        } else {
          checkStorageAndAudioPermissions();
        }
      }
    }
  }

  // Called, when permissions request response received
  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    if (requestCode == PERM_REQUEST_CODE) {

      boolean isAllGranted = true;

      if (grantResults.length <= 0) {
        isAllGranted = false;
      }

      for (int i = 0; i < grantResults.length; i++) {
        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
          isAllGranted = false;
          break;
        }
      }

      if (!isAllGranted) {
        // User denied some permissions, show rationale
        boolean rationaleShown = showStorageAndAudioRationale();

        if (rationaleShown) {
          // Rationale shown, request permissions again
          requestStorageAndAudioPermissions();
        } else {
          // Rationale not shown (this means, user denied permission and opted Don't show again)
          // Notify user to grant permissions in the system settings
          Toast.makeText(this, R.string.request_permission_denied_notification, Toast.LENGTH_SHORT).show();
        }
      }
    }
  }

  private void check_permissions(){
    // Check permission for screen overlay
    if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
      // No permission for screen overlay, show appropriate system settings activity
      showScreenOverlayRationale();
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
      startActivityForResult(intent, PERM_REQUEST_CODE_DRAW_OVERLAYS);
      // Result will be passed to onActivityResult() method
    } else {
      // Permission for screen overlay granted, check storage and audio permissions
      checkStorageAndAudioPermissions();
    }
  }

  private void checkStorageAndAudioPermissions() {
    boolean hasAllPermissions =
            ContextCompat.checkSelfPermission( this, Manifest.permission.READ_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission( this, Manifest.permission.RECORD_AUDIO ) == PackageManager.PERMISSION_GRANTED;

    if (!hasAllPermissions) {
      showStorageAndAudioRationale();
      requestStorageAndAudioPermissions();
    }
  }

  private void requestStorageAndAudioPermissions() {
    String[] permissions = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    ActivityCompat.requestPermissions(this, permissions, PERM_REQUEST_CODE);
    // Result is passed to onRequestPermissionsResult() method
  }

  // Shows request permission rationale.
  // Returns true if shown.
  private boolean showStorageAndAudioRationale() {
    boolean shouldShowRationale =
            ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.READ_EXTERNAL_STORAGE )
            || ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.WRITE_EXTERNAL_STORAGE )
            || ActivityCompat.shouldShowRequestPermissionRationale( this, Manifest.permission.RECORD_AUDIO );

    if (shouldShowRationale) {
      Toast.makeText(this, R.string.request_permission_rationale, Toast.LENGTH_SHORT).show();
    }

    // False if the app runs for the first time or the user denied permissions and opted Don't show again
    return shouldShowRationale;
  }

  private void showScreenOverlayRationale() {
    Toast.makeText(this, R.string.request_permission_screen_overlay, Toast.LENGTH_SHORT).show();
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

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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.events.bus.FileDownloadedEvent;
import sapotero.rxtest.events.crypto.AddKeyEvent;
import sapotero.rxtest.events.crypto.SelectKeyStoreEvent;
import sapotero.rxtest.events.crypto.SelectKeysEvent;
import sapotero.rxtest.events.stepper.shared.StepperNextStepEvent;
import sapotero.rxtest.services.MainService;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.views.custom.stepper.StepperLayout;
import sapotero.rxtest.views.custom.stepper.VerificationError;
import sapotero.rxtest.views.custom.stepper.build.StepperAdapter;
import timber.log.Timber;


public class LoginActivity extends AppCompatActivity implements StepperLayout.StepperListener {

  private static final int PERM_REQUEST_CODE = 0;
  private static final int PERM_SYSTEM_SETTINGS_REQUEST_CODE = 1;

  @Inject sapotero.rxtest.utils.Settings settings;
  @Inject MemoryStore store;

  private String TAG = this.getClass().getSimpleName();

  private StepperLayout stepperLayout;
  private StepperAdapter adapter;

  private boolean cryptoProInstalled = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    EsdApplication.getManagerComponent().inject(this);

    initialize();

    cryptoProInstalled = appInstalled("ru.cprocsp.ACSP");

    if( cryptoProInstalled ) {

      if (null == savedInstanceState) {
        check_permissions();
      }

      initView();


    } else {

      new MaterialDialog.Builder(this)
        .title(R.string.error_csp_not_installed)
        .content(R.string.error_csp_not_installed_body)
        .positiveText(R.string.yes)
        .cancelable(false)
        .onPositive((dialog, which) -> {
          finish();
        })
        .show();
    }



//    queue.clean();

  }

  private void showSelectDialog(List<String> keyStoreTypeList) {


    Timber.tag("KEYS").e("%s", keyStoreTypeList);

    if ( keyStoreTypeList.size() > 0 ){

      new MaterialDialog.Builder(this)
        .title(R.string.container_title)
        .items(keyStoreTypeList)
        .itemsCallbackSingleChoice(-1, (dialog, view, which, text) -> {

//          KeyStoreType.saveCurrentType(keyStoreTypeList.startTransactionFor(which));

          EventBus.getDefault().post( new SelectKeyStoreEvent(keyStoreTypeList.get(which)));

          return true;
        })
        .positiveText(R.string.vertical_form_stepper_form_continue)
        .onPositive((dialog, which) -> {
          EventBus.getDefault().post( new AddKeyEvent());
        })
        .show();

    }
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
        boolean rationaleShown = showRationale();

        if (rationaleShown) {
          // Rationale shown, request permissions again
          requestPermissions();
        } else {
          // Rationale not shown (this means, user denied permission and opted Don't show again).
          // Explicitly open system settings, so that user could grant permissions.
          openSystemSettings();
        }
      }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == PERM_SYSTEM_SETTINGS_REQUEST_CODE) {
      if (!checkHasPermissions()) {
        openSystemSettings();
      }
    }
  }

  private void check_permissions(){
    if (!checkHasPermissions()) {
      showRationale();
      requestPermissions();
    }
  }

  private boolean checkHasPermissions() {
    boolean isPermissionsGranted =
            ContextCompat.checkSelfPermission( this, Manifest.permission.READ_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission( this, Manifest.permission.WRITE_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission( this, Manifest.permission.RECORD_AUDIO ) == PackageManager.PERMISSION_GRANTED;
    return isPermissionsGranted;
  }

  private void requestPermissions() {
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
  private boolean showRationale() {
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

  private void openSystemSettings() {
    // Notify user to grant permissions in the system settings
    Toast.makeText(this, R.string.request_permission_denied_notification, Toast.LENGTH_SHORT).show();
    // Open system settings
    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
    startActivityForResult(intent, PERM_SYSTEM_SETTINGS_REQUEST_CODE);
    // Result will be passed to onActivityResult() method
  }

  private void initialize() {
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

    Intent serviceIntent = new Intent(this, MainService.class);
    startService(serviceIntent);

    // If not first run and CryptoPro installed, immediately move to main activity
    if ( !settings.isFirstRun() && cryptoProInstalled ) {
      onCompleted(null);
    }
  }

  /* Stepper */
  @Override
  public void onCompleted(View completeButton) {
    Intent intent = new Intent(this, MainActivity.class);

    Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
    startActivity(intent, bundle);

    finish();

  }

  @Override
  public void onError(VerificationError verificationError) {
//    Toast.makeText( getApplicationContext(), "onError", Toast.LENGTH_SHORT ).show();
  }

  @Override
  public void onStepSelected(int newStepPosition) {
//    Toast.makeText( getApplicationContext(), "onStepSelected", Toast.LENGTH_SHORT ).show();
  }

  @Override
  public void onReturn() {
//    Toast.makeText( getApplicationContext(), "onReturn", Toast.LENGTH_SHORT ).show();
  }

  @Subscribe(threadMode = ThreadMode.BACKGROUND)
  public void onMessageEvent(FileDownloadedEvent event) {
//    printJobStat();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(StepperNextStepEvent event) {
    stepperLayout.getmNextNavigationButton().performClick();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(SelectKeysEvent event) {
    showSelectDialog(event.list);
  }


}

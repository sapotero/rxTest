package sapotero.rxtest.views.views.stepper.build.steps;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import rx.Subscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.events.stepper.StepperAuthDcCheckEvent;
import sapotero.rxtest.events.stepper.StepperAuthDcCheckFailEvent;
import sapotero.rxtest.events.stepper.StepperAuthDcCheckSuccessEvent;
import sapotero.rxtest.views.views.stepper.BlockingStep;
import sapotero.rxtest.views.views.stepper.StepperLayout;
import sapotero.rxtest.views.views.stepper.VerificationError;
import sapotero.rxtest.views.views.stepper.util.AuthType;
import timber.log.Timber;

public class StepperAuthFragment extends Fragment implements BlockingStep {

  @Inject RxSharedPreferences settings;

  final String TAG = this.getClass().getSimpleName();
  private FrameLayout stepper_auth_password_wrapper;
  private FrameLayout stepper_auth_dc_wrapper;
  private Subscription auth_type_subscription;
  private AuthType authType = AuthType.PASSWORD;
  private VerificationError error;
  private MaterialDialog loadingDialog;
  private StepperLayout.OnNextClickedCallback callback;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    EsdApplication.getComponent( getContext() ).inject(this);

    View view = inflater.inflate(R.layout.stepper_auth, container, false);

    stepper_auth_password_wrapper = (FrameLayout) view.findViewById(R.id.stepper_auth_password_wrapper);
    stepper_auth_dc_wrapper       = (FrameLayout) view.findViewById(R.id.stepper_auth_dc_wrapper);

    error = new VerificationError("error");

    hideAllFields();

    attachSettings();

    prepareDialog();

    if ( EventBus.getDefault().isRegistered(this) ) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);

    return view;
  }

  private void prepareDialog() {
    if (loadingDialog == null){
      loadingDialog = new MaterialDialog.Builder( getContext() )
        .title(R.string.app_name)
        .content(R.string.action_settings)
        .cancelable(false)
        .progress(true, 0).build();
    }

  }

  @Override
  public void onDestroy(){
    super.onDestroy();
    if (auth_type_subscription != null) {
      auth_type_subscription.unsubscribe();
    }

  }

  @Override
  @StringRes
  public int getName() {
    //return string resource ID for the tab title used when StepperLayout is in tabs mode
    return R.string.stepper_auth;
  }

  @Override
  public VerificationError verifyStep() {

    switch ( authType ){
      case DS:
        EditText password = (EditText) stepper_auth_dc_wrapper.findViewById(R.id.stepper_auth_dc_password);
        EventBus.getDefault().post( new StepperAuthDcCheckEvent( password.getText().toString() ) );
        break;
      case PASSWORD:
        break;
    }
//    error = new VerificationError("error");
    error = null;

    return error;
  }

  @Override
  public void onSelected() {
  }

  @Override
  public void onError(@NonNull VerificationError error) {
    Toast.makeText( getContext(), "Errror", Toast.LENGTH_SHORT ).show();
  }

  private void attachSettings() {
    Preference<AuthType> auth_type = settings.getEnum("stepper.auth_type", AuthType.class);

    auth_type_subscription = auth_type.asObservable().subscribe(type -> {
      switch ( type ){
        case DS:
          authType = AuthType.DS;
          showDc();
          break;
        case PASSWORD:
          authType = AuthType.PASSWORD;
          showPassword();
          break;
      }
    });
  }

  private void hideAllFields(){
    stepper_auth_password_wrapper.setVisibility(View.GONE);
    stepper_auth_dc_wrapper.setVisibility(View.GONE);
  }

  private void showPassword(){
    stepper_auth_password_wrapper.setVisibility(View.VISIBLE);
    stepper_auth_dc_wrapper.setVisibility(View.GONE);
  }

  private void showDc(){
    stepper_auth_password_wrapper.setVisibility(View.GONE);
    stepper_auth_dc_wrapper.setVisibility(View.VISIBLE);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(StepperAuthDcCheckSuccessEvent event) throws Exception {
    if (callback != null) {
      Timber.tag(TAG).d("Sign success");
      loadingDialog.hide();
      callback.goToNextStep();
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(StepperAuthDcCheckFailEvent event) throws Exception {
    Timber.tag(TAG).d("Sign fail");
    loadingDialog.hide();
  }


  @Override
  @UiThread
  public void onNextClicked(StepperLayout.OnNextClickedCallback callback) {
    loadingDialog.show();
    this.callback = callback;
  }

  @Override
  @UiThread
  public void onBackClicked(StepperLayout.OnBackClickedCallback callback) {
    Toast.makeText(this.getContext(), "Your custom back action. Here you should cancel currently running operations", Toast.LENGTH_SHORT).show();
    callback.goToPrevStep();
  }
}
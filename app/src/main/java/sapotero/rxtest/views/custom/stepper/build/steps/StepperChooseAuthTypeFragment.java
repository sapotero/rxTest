package sapotero.rxtest.views.custom.stepper.build.steps;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.events.stepper.shared.StepperNextStepEvent;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.views.activities.SettingsActivity;
import sapotero.rxtest.views.custom.stepper.Step;
import sapotero.rxtest.views.custom.stepper.VerificationError;
import sapotero.rxtest.views.custom.stepper.util.AuthType;
import timber.log.Timber;

public class StepperChooseAuthTypeFragment extends Fragment implements Step, View.OnClickListener {
  @Inject ISettings settings;

  private MaterialDialog dialog;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    EsdApplication.getDataComponent().inject(this);

    View view = inflater.inflate(R.layout.stepper_auth_chooser, container, false);

    AppCompatButton ds       = (AppCompatButton) view.findViewById( R.id.stepper_auth_choose_cert );
    AppCompatButton password = (AppCompatButton) view.findViewById( R.id.stepper_auth_choose_password );

    TextView settings = (TextView) view.findViewById( R.id.stepper_auth_settings );
    settings.setOnClickListener(v -> {
      Intent intent = new Intent(getContext(), SettingsActivity.class);
      startActivity(intent);
    });

    ds.setOnClickListener(this);
    password.setOnClickListener(this);

//    if ( EventBus.getDefault().isRegistered(this) ) {
//      EventBus.getDefault().unregister(this);
//    }
//    EventBus.getDefault().register(this);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    setAuthTypeDc();
  }

  @Override
  public void onDestroy(){
    super.onDestroy();
//    if ( EventBus.getDefault().isRegistered(this) ) {
//      EventBus.getDefault().unregister(this);
//    }

  }

  @Override
  @StringRes
  public int getName() {
    return R.string.stepper_choose_auth;
  }

  @Override
  public VerificationError verifyStep() {
    //return null if the user can go to the next step, create a new VerificationError instance otherwise
    return null;
  }

  @Override
  public void onSelected() {
  }

  @Override
  public void onError(@NonNull VerificationError error) {
    //handle error inside of the fragment, e.g. show error on EditText
  }

  @Override
  public void onClick(View v) {
    switch ( v.getId() ){

      case R.id.stepper_auth_choose_cert:
        signWithDc();
        break;

      case R.id.stepper_auth_choose_password:
        signWithLogin();
        break;

      default:
        break;
    }
  }

  private void signWithDc() {
    Timber.tag("StepperAuthFragment").d( "stepper_auth_choose_cert" );
    setAuthTypeDc();
    EventBus.getDefault().post( new StepperNextStepEvent() );
  }

  private void signWithLogin() {
    Timber.tag("StepperAuthFragment").d( "stepper_auth_choose_password" );
    setAuthTypeLogin();
    EventBus.getDefault().post( new StepperNextStepEvent() );
  }

  private void setAuthType( AuthType type ) {
    settings.setAuthType( type );
  }

  private void setAuthTypeDc() {
    setAuthType( AuthType.DS );
    settings.setSignedWithDc( true );
  }

  private void setAuthTypeLogin() {
    setAuthType( AuthType.PASSWORD );
    settings.setSignedWithDc( false );
  }
}
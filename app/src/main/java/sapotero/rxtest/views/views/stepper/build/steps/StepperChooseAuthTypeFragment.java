package sapotero.rxtest.views.views.stepper.build.steps;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.events.stepper.StepperNextStepEvent;
import sapotero.rxtest.views.views.stepper.Step;
import sapotero.rxtest.views.views.stepper.VerificationError;
import sapotero.rxtest.views.views.stepper.util.AuthType;
import timber.log.Timber;

public class StepperChooseAuthTypeFragment extends Fragment implements Step, View.OnClickListener {
  @Inject RxSharedPreferences settings;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    EsdApplication.getComponent( getContext() ).inject(this);

    View view = inflater.inflate(R.layout.stepper_auth_chooser, container, false);

    AppCompatButton ds       = (AppCompatButton) view.findViewById( R.id.stepper_auth_choose_cert );
    AppCompatButton password = (AppCompatButton) view.findViewById( R.id.stepper_auth_choose_password );

    ds.setOnClickListener(this);
    password.setOnClickListener(this);

    return view;
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
    //update UI when selected
  }

  @Override
  public void onError(@NonNull VerificationError error) {
    //handle error inside of the fragment, e.g. show error on EditText
  }

  @Override
  public void onClick(View v) {
    switch ( v.getId() ){
      case R.id.stepper_auth_choose_cert:
        Timber.tag("StepperAuthFragment").d( "stepper_auth_choose_cert" );
        setAuthType( AuthType.DS );
        break;
      case R.id.stepper_auth_choose_password:
        Timber.tag("StepperAuthFragment").d( "stepper_auth_choose_password" );
        setAuthType( AuthType.PASSWORD );
        break;
      default:
        break;
    }
    EventBus.getDefault().post( new StepperNextStepEvent() );
  }

  private void setAuthType( AuthType type ){
    settings.getEnum("stepper.auth_type", AuthType.class).set( type );
  }
}
package sapotero.rxtest.views.views.stepper.build.steps;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import sapotero.rxtest.R;
import sapotero.rxtest.views.views.stepper.Step;
import sapotero.rxtest.views.views.stepper.VerificationError;

public class SecondStepFragment extends Fragment implements Step {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.stepper_test_step, container, false);

    Button button = (Button) view.findViewById(R.id.button4);
    button.setOnClickListener(v -> {

    });

    return view;
  }

  @Override
  @StringRes
  public int getName() {
    //return string resource ID for the tab title used when StepperLayout is in tabs mode
    return R.string.username_hinf;
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

}
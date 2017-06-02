package sapotero.rxtest.views.custom.stepper.build.steps;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.views.custom.stepper.Step;
import sapotero.rxtest.views.custom.stepper.VerificationError;

public class StepperFinishFragment extends Fragment implements Step {

  @Inject
  MemoryStore store;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.stepper_final_view, container, false);

    EsdApplication.getManagerComponent().inject(this);
    store.invalidate();

    return view;
  }

  @Override
  @StringRes
  public int getName() {
    //return string resource ID for the tab title used when StepperLayout is in tabs mode
    return R.string.stepper_end;
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

}
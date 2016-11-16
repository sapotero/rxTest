package sapotero.rxtest.views.adapters;

//import android.support.annotation.StringRes;
//import android.support.v4.app.FragmentManager;
//
//import com.stepstone.stepper.adapter.AbstractStepAdapter;
//
//import sapotero.rxtest.R;
//import sapotero.rxtest.views.fragments.stepper.LoadInfoFragment;
//import sapotero.rxtest.views.fragments.stepper.LoginFragment;
//
//public class LoginStepperAdapter extends AbstractStepAdapter {
//
//  public LoginStepperAdapter(FragmentManager fragmentManager) {
//    super(fragmentManager);
//  }
//
//  @Override
//  public android.support.v4.app.Fragment createStep(int position) {
//    switch (position) {
//      case 0:
//        return new LoginFragment();
//      case 1:
//        return new LoadInfoFragment();
//      default:
//        throw new IllegalArgumentException("Unsupported position: " + position);
//    }
//  }
//
//  @Override
//  public int getCount() {
//    return 3;
//  }
//
//  @StringRes
//  @Override
//  public int getNextButtonText(int position) {
//    switch (position) {
//      case 0:
//        return R.string.stepper_login_username;
//      case 1:
//        return R.string.stepper_login_username;
//      default:
//        throw new IllegalArgumentException("Unsupported position: " + position);
//    }
//  }
//}
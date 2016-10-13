package sapotero.rxtest.application.utils;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

public class Keyboard {

  public static void show(Activity activity){
    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
  }

  public static void hide(Activity activity){
    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
  }

  public static void toggle(Activity activity){
    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
    if (imm.isActive()){
      hide(activity);
    } else {
      show(activity);
    }
  }
}

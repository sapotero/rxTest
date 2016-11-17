package sapotero.rxtest.views.views.utils;


import android.view.View;

public interface VerticalStepperForm {
  View createStepContentView(int stepNumber);
  void onStepOpening(int stepNumber);
  void sendData();
}
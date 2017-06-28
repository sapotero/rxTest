package sapotero.rxtest.views.dialogs;

import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;

import sapotero.rxtest.R;

public class DecisionTextDialog {

  private MaterialDialog.Builder dialogBuilder;
  private MaterialDialog dialog;
  private EditText textInput;

  public DecisionTextDialog(Context context, EditText parentEditText, CharSequence title, CharSequence hint) {
    if (context != null && parentEditText != null && title != null && hint != null) {
      dialogBuilder = new MaterialDialog.Builder(context)
        .title(title)
        .customView(R.layout.dialog_decision_text, false)
        .positiveText(R.string.constructor_save)
        .negativeText(R.string.constructor_close)
        .neutralText(R.string.constructor_clear)
        .showListener(dialog -> {
          DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
          float screenWidth = displayMetrics.widthPixels;
          int marginInPixels = 200;
          int width = Math.round(screenWidth - (marginInPixels * 2));
          ((MaterialDialog) dialog).getWindow().setLayout(width, 500);

          textInput = (EditText) ((MaterialDialog) dialog)
            .getCustomView().findViewById(R.id.dialog_decision_text_input);
          textInput.setHint(hint);
          textInput.setText(parentEditText.getText());
        })
        .onPositive((dialog1, which) -> {
          parentEditText.setText(textInput.getText());
          clearReferences();
          dialog1.dismiss();
        })
        .onNeutral((dialog12, which) -> {
          textInput = (EditText) dialog.getCustomView().findViewById(R.id.dialog_decision_text_input);
          textInput.setText("");
        })
        .onNegative((dialog1, which) -> {
          dialog1.dismiss();
        })
      .autoDismiss(false);
//        .dismissListener(dialog1 -> {
//          parentEditText.setText(textInput.getText());
//          clearReferences();
//        });
    }
  }

  public void show() {
    if (dialogBuilder != null) {
      dialog = dialogBuilder.show();
    }
  }

  private void clearReferences() {
    textInput = null;
    dialogBuilder = null;
    dialog = null;
  }
}

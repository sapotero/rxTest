package sapotero.rxtest.views.dialogs;

import android.content.Context;
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
        .autoDismiss(false)
        .cancelable(false)
        .customView(R.layout.dialog_decision_text, true)
        .positiveText("OK")
        .negativeText("Отмена")
        .showListener(dialog -> {
          textInput = (EditText) ((MaterialDialog) dialog)
                  .getCustomView().findViewById(R.id.dialog_decision_text_input);
          textInput.setHint(hint);
          textInput.setText(parentEditText.getText());
        })
        .onPositive((dialog, which) -> {
          parentEditText.setText(textInput.getText());
          dismiss();
        })
        .onNegative((dialog, which) -> {
          dismiss();
        });
    }
  }

  public void show() {
    if (dialogBuilder != null) {
      dialog = dialogBuilder.show();
    }
  }

  private void dismiss() {
    if (dialog != null) {
      dialog.dismiss();
    }

    textInput = null;
    dialogBuilder = null;
    dialog = null;
  }
}

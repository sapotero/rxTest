package sapotero.rxtest.views.dialogs;

import android.content.Context;
import android.content.DialogInterface;
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
        .customView(R.layout.dialog_decision_text, true)
        .positiveText("OK")
        .showListener(dialog -> {
          textInput = (EditText) ((MaterialDialog) dialog)
                  .getCustomView().findViewById(R.id.dialog_decision_text_input);
          textInput.setHint(hint);
          textInput.setText(parentEditText.getText());
        })
        .dismissListener(dialog1 -> {
          parentEditText.setText(textInput.getText());
          clearReferences();
        });
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

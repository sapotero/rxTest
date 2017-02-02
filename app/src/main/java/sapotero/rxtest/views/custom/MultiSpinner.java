package sapotero.rxtest.views.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import sapotero.rxtest.R;

public class MultiSpinner extends TextView implements DialogInterface.OnMultiChoiceClickListener {

  private SpinnerAdapter mAdapter;
  private boolean[] mOldSelection;
  private boolean[] mSelected;
  private String mDefaultText = "Параметры поиска";
  private String mAllText = "Параметры поиска";
  private boolean mAllSelected;
  private MultiSpinnerListener mListener;

  public MultiSpinner(Context context) {
    super(context);
  }

  public MultiSpinner(Context context, AttributeSet attr) {
    this(context, attr, R.attr.spinnerStyle);
  }

  public MultiSpinner(Context context, AttributeSet attr, int defStyle) {
    super(context, attr, defStyle);
  }

  public void onClick(DialogInterface dialog, int which, boolean isChecked) {
    mSelected[which] = isChecked;
  }

  private OnClickListener onClickListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

      String choices[] = new String[mAdapter.getCount()];

      for (int i = 0; i < choices.length; i++) {
        choices[i] = mAdapter.getItem(i).toString();
      }

      System.arraycopy(mSelected, 0, mOldSelection, 0, mSelected.length);
      builder.setMultiChoiceItems(choices, mSelected, MultiSpinner.this);

      builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
        System.arraycopy(mOldSelection, 0, mSelected, 0, mSelected.length);

        dialog.dismiss();
      });

      builder.setTitle("Параметры поиска");

      builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
        refreshSpinner();
        mListener.onItemsSelected(mSelected);
        dialog.dismiss();
      });

      builder.show();
    }
  };

  public SpinnerAdapter getAdapter() {
    return this.mAdapter;
  }

  DataSetObserver dataSetObserver = new DataSetObserver() {
    @Override
    public void onChanged() {
      mOldSelection = new boolean[mAdapter.getCount()];
      mSelected = new boolean[mAdapter.getCount()];
      for (int i = 0; i < mSelected.length; i++) {
        mOldSelection[i] = false;
        mSelected[i] = mAllSelected;
      }
    }
  };


  public void setAdapter(SpinnerAdapter adapter, boolean allSelected, MultiSpinnerListener listener) {
    SpinnerAdapter oldAdapter = this.mAdapter;

    setOnClickListener(null);

    this.mAdapter = adapter;
    this.mListener = listener;
    this.mAllSelected = allSelected;

    if (oldAdapter != null) {
      oldAdapter.unregisterDataSetObserver(dataSetObserver);
    }

    if (mAdapter != null) {
      mAdapter.registerDataSetObserver(dataSetObserver);

      mOldSelection = new boolean[mAdapter.getCount()];
      mSelected = new boolean[mAdapter.getCount()];
      for (int i = 0; i < mSelected.length; i++) {
        mOldSelection[i] = false;
        mSelected[i] = allSelected;
      }

      setOnClickListener(onClickListener);
    }

    setText(mAllText);
  }

  public void setOnItemsSelectedListener(MultiSpinnerListener listener) {
    this.mListener = listener;
  }

  public interface MultiSpinnerListener {
    public void onItemsSelected(boolean[] selected);
  }

  public boolean[] getSelected() {
    return this.mSelected;
  }

  public void setSelected(boolean[] selected) {
    if (this.mSelected.length != selected.length)
      return;

    this.mSelected = selected;

    refreshSpinner();
  }

  private void refreshSpinner() {
    StringBuilder spinnerBuffer = new StringBuilder();
    boolean someUnselected = false;
    boolean allUnselected = true;

    for (int i = 0; i < mAdapter.getCount(); i++) {
      if (mSelected[i]) {
        spinnerBuffer.append(mAdapter.getItem(i).toString());
        spinnerBuffer.append(", ");
        allUnselected = false;
      } else {
        someUnselected = true;
      }
    }

    String spinnerText;

    if (!allUnselected) {
      if (someUnselected && !(mAllText != null && mAllText.length() > 0)) {
        spinnerText = spinnerBuffer.toString();
        if (spinnerText.length() > 2)
          spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
      } else {
        spinnerText = mAllText;
      }
    } else {
      spinnerText = mDefaultText;
    }

    setText(spinnerText);
  }

  public String getDefaultText() {
    return mDefaultText;
  }

  public void setDefaultText(String defaultText) {
    this.mDefaultText = defaultText;
  }

  public String getAllText() {
    return mAllText;
  }

  public void setAllText(String allText) {
    this.mAllText = allText;
  }
}
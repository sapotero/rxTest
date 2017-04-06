package sapotero.rxtest.views.custom;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import sapotero.rxtest.R;
import sapotero.rxtest.views.adapters.OrganizationAdapter;
import sapotero.rxtest.views.adapters.models.OrganizationItem;

public class OrganizationSpinner extends TextView implements DialogInterface.OnMultiChoiceClickListener {

  private final LayoutInflater inflater;
  //  private SpinnerAdapter mAdapter;
  private OrganizationAdapter mAdapter;
  private boolean[] mOldSelection;
  private boolean[] mSelected;
  private String mDefaultText = "Выберите организацию";
  private String mAllText = "Выберите организацию";
  private boolean mAllSelected;
  private MultiSpinnerListener mListener;

  Button neutralButton;

  public OrganizationSpinner(Context context) {
    super(context);
    inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
  }

  public OrganizationSpinner(Context context, AttributeSet attr, int defStyle) {
    super(context, attr, defStyle);
    inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
  }

  public OrganizationSpinner(Context context, AttributeSet attr) {
    this(context, attr, R.attr.spinnerStyle);
  }

  public void onClick(DialogInterface dialog, int which, boolean isChecked) {
    mSelected[which] = isChecked;
    updateNeutralButtonText();
  }

  private OnClickListener onClickListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

      String choices[] = new String[mAdapter.getCount()];

      for (int i = 0; i < choices.length; i++) {
//        choices[i] = mAdapter.getMainMenuItem(i).toString();
        choices[i] = mAdapter.getItem(i).getTitleForDialog();
      }

      System.arraycopy(mSelected, 0, mOldSelection, 0, mSelected.length);
      builder.setMultiChoiceItems(choices, mSelected, OrganizationSpinner.this);

      builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
        System.arraycopy(mOldSelection, 0, mSelected, 0, mSelected.length);
        dialog.dismiss();
      });

      builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
        refreshSpinner();
        mListener.onItemsSelected(mSelected);
        dialog.dismiss();
      });

      builder.setNeutralButton(android.R.string.selectAll, (dialog, which) -> {
        // This button is overriden later to change close behaviour
      });

      final AlertDialog dialog = builder.create();

      dialog.setOnShowListener(dialogInterface -> {
        neutralButton = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_NEUTRAL);
        updateNeutralButtonText();
      });

      dialog.show();

      // Override neutral button handler immediately after show to prevent dialog from closing
      neutralButton.setOnClickListener(view -> {
        ListView organizationList = dialog.getListView();
        mOldSelection = new boolean[mAdapter.getCount()];

        if ( isCheckedAll() ) {
          // Deselect all
          for (int i = 0; i < mOldSelection.length; i++) {
            mOldSelection[i] = false;
            mSelected[i] = false;
            organizationList.setItemChecked(i, false);
          }
        } else {
          // Select all
          for (int i = 0; i < mOldSelection.length; i++) {
            mOldSelection[i] = true;
            mSelected[i] = true;
            organizationList.setItemChecked(i, true);
          }
        }

        updateNeutralButtonText();

        // System.arraycopy(mOldSelection, 0, mSelected, 0, mSelected.length);
        // refreshSpinner();
      });
    }
  };

  private boolean isCheckedAll() {
    boolean isCheckedAll = true;
    for (int i = 0; i < mSelected.length; i++) {
      if ( !mSelected[i] ) {
        isCheckedAll = false;
        break;
      }
    }
    return isCheckedAll;
  }

  private void updateNeutralButtonText() {
    if (neutralButton != null) {
      if ( isCheckedAll() ) {
        neutralButton.setText("Снять выделение");
      } else {
        neutralButton.setText("Выделить все");
      }
    }
  }

  public SpinnerAdapter getAdapter() {
    return this.mAdapter;
  }

  DataSetObserver dataSetObserver = new DataSetObserver() {
    @Override
    public void onChanged() {
      // all selected by default
      mOldSelection = new boolean[mAdapter.getCount()];
      mSelected = new boolean[mAdapter.getCount()];
      for (int i = 0; i < mSelected.length; i++) {
        mOldSelection[i] = false;
        mSelected[i] = mAllSelected;
      }
    }
  };


  public void setAdapter(OrganizationAdapter adapter, boolean allSelected, MultiSpinnerListener listener) {
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
        mOldSelection[i] = true;
        mSelected[i] = allSelected;
      }

      setOnClickListener(onClickListener);
    }

    setText(mAllText);
  }

  public void setOnItemsSelectedListener(MultiSpinnerListener listener) {
    this.mListener = listener;
  }

  public void clear() {
    mSelected = new boolean[mOldSelection.length];

    for (int i = 0; i < mSelected.length; i++) {
      mOldSelection[i] = true;
      mSelected[i] = true;
    }

    refreshSpinner();
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

  public void add(){

  }

  private void refreshSpinner() {
    int selected = 0;
    OrganizationItem item = null;

    for (int i = 0; i < mAdapter.getCount(); i++) {
      if (mSelected[i]) {
        item = mAdapter.getItem(i);
        selected++;
      }
    }

    CharSequence spinnerText;

    switch( selected ){
      case 0:
        spinnerText = "Организации";
        break;
      case 1:

        final ForegroundColorSpan grey  = new ForegroundColorSpan( getResources().getColor(R.color.md_grey_600) );
        final ForegroundColorSpan black = new ForegroundColorSpan( getResources().getColor(R.color.md_grey_900) );

        final SpannableStringBuilder title = new SpannableStringBuilder( item.getTitle());
        final SpannableStringBuilder count = new SpannableStringBuilder( String.format("%-4s ", String.valueOf(item.getCount()) ) );

        title.setSpan(black, 0, item.getTitle().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        count.setSpan(grey, 0, String.valueOf(item.getCount()).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        spinnerText = TextUtils.concat(count, title);
        break;
      default:
        spinnerText = String.format("Выбрано организаций: %s", selected);
        break;
    }
    if ( selected == mAdapter.getCount() ){
      spinnerText = "Все организации";
    }


//    setText(String.format(spinnerText, selected));

    if (spinnerText.length() > 80){
      spinnerText = spinnerText.toString().substring(0,77) + "...";
    }

    setText( spinnerText );
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
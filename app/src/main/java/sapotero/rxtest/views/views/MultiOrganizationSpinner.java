package sapotero.rxtest.views.views;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import sapotero.rxtest.R;
import sapotero.rxtest.views.adapters.OrganizationAdapter;
import sapotero.rxtest.views.adapters.models.OrganizationItem;

public class MultiOrganizationSpinner extends TextView implements DialogInterface.OnMultiChoiceClickListener {

//  private SpinnerAdapter mAdapter;
  private OrganizationAdapter mAdapter;
  private boolean[] mOldSelection;
  private boolean[] mSelected;
  private String mDefaultText;
  private String mAllText = "Организации";
  private boolean mAllSelected;
  private MultiSpinnerListener mListener;

  public MultiOrganizationSpinner(Context context) {
    super(context);
  }

  public MultiOrganizationSpinner(Context context, AttributeSet attr) {
    this(context, attr, R.attr.spinnerStyle);
  }

  public MultiOrganizationSpinner(Context context, AttributeSet attr, int defStyle) {
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
//        choices[i] = mAdapter.getMainMenuItem(i).toString();
        choices[i] = mAdapter.getItem(i).getTitle();
      }

      System.arraycopy(mSelected, 0, mOldSelection, 0, mSelected.length);
      builder.setMultiChoiceItems(choices, mSelected, MultiOrganizationSpinner.this);

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
        mOldSelection = new boolean[mAdapter.getCount()];
        for (int i = 0; i < mOldSelection.length; i++) {
          mOldSelection[i] = true;
          mSelected[i] = true;
        }

        System.arraycopy(mOldSelection, 0, mSelected, 0, mSelected.length);
        refreshSpinner();
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

  public void clear() {
    mSelected = new boolean[mAdapter.getCount()];
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

        final SpannableStringBuilder title = new SpannableStringBuilder( item.getName());
        final SpannableStringBuilder count = new SpannableStringBuilder( String.format("%-4s ", String.valueOf(item.getCount()) ) );

        title.setSpan(black, 0, item.getName().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
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

    if (spinnerText.length() > 40){
      spinnerText = spinnerText.toString().substring(0,37) + "...";
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
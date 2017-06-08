package sapotero.rxtest.views.custom;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

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
  private Context context;

  List<DialogListItem> choices;
  DialogListAdapter dialogListAdapter;
  Button neutralButton;

  public OrganizationSpinner(Context context) {
    super(context);
    this.context = context;
    inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
  }

  public OrganizationSpinner(Context context, AttributeSet attr, int defStyle) {
    super(context, attr, defStyle);
    this.context = context;
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
      System.arraycopy(mSelected, 0, mOldSelection, 0, mSelected.length);

      choices = new ArrayList<>();

      for (int i = 0; i < mAdapter.getCount(); i++) {
        DialogListItem dialogListItem = new DialogListItem(
                mSelected[i],
                mAdapter.getItem(i).getCountForDialog(),
                mAdapter.getItem(i).getTitleForDialog() );
        choices.add(dialogListItem);
      }

      dialogListAdapter = new DialogListAdapter(getContext(), choices);

      MaterialDialog dialog = new MaterialDialog.Builder(context)
        .title("Фильтр организаций")
        .autoDismiss(false)
        .cancelable(true)
        .adapter(dialogListAdapter, null)
        .positiveText(android.R.string.ok)
        .positiveColor(Color.BLACK)
        .onPositive((dialog1, which) -> dialog1.dismiss())
        .build();

      dialog.show();

//      AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//
//      View dialogView = inflater.inflate(R.layout.filter_organizations_dialog, null);
//      builder.setView(dialogView);
//
//      final AlertDialog dialog = builder.create();
//
//      Button negativeButton = (Button) dialogView.findViewById(R.id.filter_organization_negative);
//      negativeButton.setText(android.R.string.cancel);
//      negativeButton.setOnClickListener(v1 -> {
//        System.arraycopy(mOldSelection, 0, mSelected, 0, mSelected.length);
//        dialog.dismiss();
//      });
//
//      Button positiveButton = (Button) dialogView.findViewById(R.id.filter_organization_positive);
//      positiveButton.setText(android.R.string.ok);
//      positiveButton.setOnClickListener(v1 -> {
//        for (int i = 0; i < choices.size(); i++) {
//          mSelected[i] = choices.get(i).isChecked();
//        }
//        refreshSpinner();
//        mListener.onItemsSelected(mSelected);
//        dialog.dismiss();
//      });
//
//      neutralButton = (Button) dialogView.findViewById(R.id.filter_organization_neutral);
//      updateNeutralButtonText();
//      neutralButton.setOnClickListener(v12 -> {
//        if ( isCheckedAll() ) {
//          // Deselect all
//          for (int i = 0; i < mOldSelection.length; i++) {
//            choices.get(i).setChecked(false);
//          }
//        } else {
//          // Select all
//          for (int i = 0; i < mOldSelection.length; i++) {
//            choices.get(i).setChecked(true);
//          }
//        }
//        dialogListAdapter.notifyDataSetChanged();
//        updateNeutralButtonText();
//      });
//
//      dialogListAdapter = new DialogListAdapter(getContext(), choices);
//
//      ListView listView = (ListView) dialogView.findViewById(R.id.filter_organization_list);
//      listView.setAdapter(dialogListAdapter);
//      listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//      listView.setOnItemClickListener((parent, view, position, id) -> {
//        choices.get(position).setChecked(!choices.get(position).isChecked());
//        dialogListAdapter.notifyDataSetChanged();
//        updateNeutralButtonText();
//      });
//
//      dialog.show();
    }
  };

  private boolean isCheckedAll() {
    boolean isCheckedAll = true;
    for (int i = 0; i < choices.size(); i++) {
      if ( !choices.get(i).isChecked() ) {
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

  public void refreshSpinner() {

    if (mAdapter.getCount() == 0) {
      // No organizations in adapter, disable organization spinner
      setText("Нет организаций");
      setEnabled(false);

    } else {
      // Otherwise enable spinner and set spinner text depending on organization filter selection
      setEnabled(true);

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
          final StyleSpan bold = new StyleSpan(Typeface.BOLD);

          final SpannableStringBuilder title = new SpannableStringBuilder( item.getTitle());
          final SpannableStringBuilder count = new SpannableStringBuilder( String.format("%-4s ", String.valueOf(item.getCount()) ) );

          title.setSpan(black, 0, item.getTitle().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
          count.setSpan(bold, 0, String.valueOf(item.getCount()).length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
          spinnerText = TextUtils.concat(count, title);
          break;

        default:
          spinnerText = String.format("Выбрано организаций: %s", selected);
          break;
      }

      if ( selected == mAdapter.getCount() ){
        spinnerText = "Все организации";
      }

      if (spinnerText.length() > 80){
        spinnerText = spinnerText.toString().substring(0,77) + "...";
      }

      setText( spinnerText );
    }
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


  private class DialogListItem {
    private boolean checked;
    private CharSequence count;
    private CharSequence title;

    public DialogListItem(boolean checked, CharSequence count, CharSequence title) {
      this.checked = checked;
      this.count = count;
      this.title = title;
    }

    public boolean isChecked() {
      return checked;
    }

    public void setChecked(boolean checked) {
      this.checked = checked;
    }

    public CharSequence getCount() {
      return count;
    }

    public void setCount(CharSequence count) {
      this.count = count;
    }

    public CharSequence getTitle() {
      return title;
    }

    public void setTitle(CharSequence title) {
      this.title = title;
    }
  }


  private class DialogListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private DialogListItem item;

    private CheckBox checkBox;
    private TextView count;
    private TextView title;

    public DialogListHolder(View itemView) {
      super(itemView);
      itemView.setOnClickListener(this);
      checkBox = (CheckBox) itemView.findViewById(R.id.filter_organization_checkbox);
      count = (TextView) itemView.findViewById(R.id.filter_organization_count);
      title = (TextView) itemView.findViewById(R.id.filter_organization_name);
    }

    public void bindItem(DialogListItem item) {
      this.item = item;
      checkBox.setChecked( item.isChecked() );
      count.setText( item.getCount() );
      title.setText( item.getTitle() );
    }

    @Override
    public void onClick(View v) {
      item.setChecked( !item.isChecked() );
      dialogListAdapter.notifyDataSetChanged();
//      updateNeutralButtonText();
    }
  }


  private class DialogListAdapter extends RecyclerView.Adapter<DialogListHolder> {

    List<DialogListItem> itemList;
    Context context;

    public DialogListAdapter(Context context, List<DialogListItem> itemList) {
      this.context = context;
      this.itemList = itemList;
    }

    @Override
    public DialogListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      LayoutInflater layoutInflater = LayoutInflater.from(context);
      View view = layoutInflater.inflate(R.layout.filter_organizations_dialog_item, parent, false);
      return new DialogListHolder(view);
    }

    @Override
    public void onBindViewHolder(DialogListHolder holder, int position) {
      holder.bindItem( itemList.get( position ) );
    }

    @Override
    public int getItemCount() {
      return itemList == null ? 0 : itemList.size();
    }
  }
}
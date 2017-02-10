package sapotero.rxtest.views.custom;


import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

public class HintSpinnerAdapter implements SpinnerAdapter, ListAdapter {

  protected static final int PIVOT = 1;

  protected SpinnerAdapter adapter;

  protected Context context;

  protected int hintLayout;

  protected int hintDropdownLayout;

  protected LayoutInflater layoutInflater;

  public HintSpinnerAdapter( SpinnerAdapter spinnerAdapter, int hintLayout, Context context) {

    this(spinnerAdapter, hintLayout, -1, context);
  }

  public HintSpinnerAdapter(SpinnerAdapter spinnerAdapter, int hintLayout, int hintDropdownLayout, Context context) {
    this.adapter = spinnerAdapter;
    this.context = context;
    this.hintLayout = hintLayout;
    this.hintDropdownLayout = hintDropdownLayout;
    layoutInflater = LayoutInflater.from(context);
  }
  @Override
  public final View getView(int position, View convertView, ViewGroup parent) {
    
    
    if (position == 0) {
      return getHintView(parent);
    }
    return adapter.getView(position - PIVOT, null, parent); 
    
  }

  
  protected View getHintView(ViewGroup parent) {
    return layoutInflater.inflate(hintLayout, parent, false);
  }

  @Override
  public View getDropDownView(int position, View convertView, ViewGroup parent) {
    
    
    if (position == 0) {
      return hintDropdownLayout == -1 ?
        new View(context) :
        getHintDropdownView(parent);
    }

    
    return adapter.getDropDownView(position - PIVOT, null, parent);
  }

  
  protected View getHintDropdownView(ViewGroup parent) {
    return layoutInflater.inflate(hintDropdownLayout, parent, false);
  }

  @Override
  public int getCount() {
    int count = adapter.getCount();
    return count == 0 ? 0 : count + PIVOT;
  }

  @Override
  public Object getItem(int position) {
    return position == 0 ? null : adapter.getItem(position - PIVOT);
  }

  @Override
  public int getItemViewType(int position) {
    return 0;
  }

  @Override
  public int getViewTypeCount() {
    return 1;
  }

  @Override
  public long getItemId(int position) {
    return position >= PIVOT ? adapter.getItemId(position - PIVOT) : position - PIVOT;
  }

  @Override
  public boolean hasStableIds() {
    return adapter.hasStableIds();
  }

  @Override
  public boolean isEmpty() {
    return adapter.isEmpty();
  }

  @Override
  public void registerDataSetObserver(DataSetObserver observer) {
    adapter.registerDataSetObserver(observer);
  }

  @Override
  public void unregisterDataSetObserver(DataSetObserver observer) {
    adapter.unregisterDataSetObserver(observer);
  }

  @Override
  public boolean areAllItemsEnabled() {
    return false;
  }

  @Override
  public boolean isEnabled(int position) {
    return position != 0; 
  }

}
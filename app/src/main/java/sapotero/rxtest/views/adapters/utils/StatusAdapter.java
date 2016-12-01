package sapotero.rxtest.views.adapters.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import sapotero.rxtest.R;
import sapotero.rxtest.views.adapters.models.FilterItem;

public class StatusAdapter extends BaseAdapter {
  private int template;
  private List<FilterItem> filters;
  private Context context;
  private int filterCount[];
  private String[] filterName;
  private LayoutInflater inflter;

  private TextView count;
  private TextView name;
  private View mainView;
  private int mPos;

  private Filter statusFilter = new CustomFilter();
  private ArrayList<FilterItem> suggestions = new ArrayList<>();

  public StatusAdapter(Context context, List<FilterItem> filters) {

    this.context = context;
    this.filters = filters;
    this.inflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount() {
    return filters.size();
  }

  @Override
  public FilterItem getItem(int position) {
    return filters.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  // пункт списка
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    View view = convertView;
    if (view == null) {
      view = inflter.inflate(R.layout.filter_spinner_items, parent, false);
    }

    mPos = position;

    FilterItem filterItem = getFilterItem(position);

    ( (TextView) view.findViewById(R.id.decision_name)  ).setText( filterItem.getName()  );
    ( (TextView) view.findViewById(R.id.decision_date) ).setText( filterItem.getCount() );

    return view;
  }

  private FilterItem getFilterItem(int position) {
    return ((FilterItem) getItem(position));
  }

  ArrayList<FilterItem> getBox() {
    ArrayList<FilterItem> box = new ArrayList<FilterItem>();
    return box;
  }

  public int getPosition() {
    return mPos;
  }

  public int prev() {

    if (filters == null || filters.size() == 0){
      return 0;
    }

    int position = mPos - 1;

    if ( position < 0 ){
      return filters.size() - 1;
    } else {
      return position;
    }

  }

  public int next() {
    if (filters == null || filters.size() == 0){
      return 0;
    }

    int position = mPos + 1;

    if ( position >= filters.size() ){
      return 0;
    } else {
      return position;
    }
  }

  public void updateByValue(String filter_type, Integer count) {
    for (int i = 0; i < filters.size(); i++) {
      if (Objects.equals(filters.get(i).getValue(), filter_type)){
        filters.get(i).setCount( String.valueOf(count) );
        notifyDataSetChanged();
        break;
      }
    }
  }

//  @Override
//  public Filter getFilter() {
//    return statusFilter;
//  }

  private class CustomFilter extends Filter {
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      suggestions.clear();

      if (filters != null && constraint != null) { // Check if the Original List and Constraint aren't null.
        for (int i = 0; i < filters.size(); i++) {
          if (filters.get(i).getName().toLowerCase().contains(constraint)) { // Compare item in original list if it contains constraints.
            suggestions.add(filters.get(i));
          }
        }
      }
      FilterResults results = new FilterResults(); // Create new Filter Results and return this to publishResults;
      results.values = suggestions;
      results.count = suggestions.size();

      return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
      if (results.count > 0) {
        notifyDataSetChanged();
      } else {
        notifyDataSetInvalidated();
      }
    }
  }
}
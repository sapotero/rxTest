package sapotero.rxtest.views.adapters.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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

    FilterItem filterItem = getFilterItem(position);

    ( (TextView) view.findViewById(R.id.filter_name)  ).setText( filterItem.getName()  );
    ( (TextView) view.findViewById(R.id.filter_count) ).setText( filterItem.getCount() );

//    CheckBox cbBuy = (CheckBox) view.findViewById(R.id.cbBox);
//    cbBuy.setOnCheckedChangeListener(myCheckChangeList);
//    cbBuy.setTag(position);
//    cbBuy.setChecked(filterItem.box);
    return view;
  }

  private FilterItem getFilterItem(int position) {
    return ((FilterItem) getItem(position));
  }

  ArrayList<FilterItem> getBox() {
    ArrayList<FilterItem> box = new ArrayList<FilterItem>();
    // for (FilterItem p : filters) {
    // if p.getName() == 1
    //   box.add(p)
    // }
    return box;
  }

//  CompoundButton.OnCheckedChangeListener myCheckChangeList = new CompoundButton.OnCheckedChangeListener() {
//    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//      FilterItem item = getFilterItem((Integer) buttonView.getTag());
//      item.setName("isChecked");
//    }
//  };
}
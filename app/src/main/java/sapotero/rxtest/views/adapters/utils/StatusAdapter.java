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
  private List<FilterItem> statuses;
  private Context context;
  private int filterCount[];
  private String[] filterName;
  private LayoutInflater inflter;

  private TextView count;
  private TextView name;
  private View mainView;
  private int mPos;

  public StatusAdapter(Context context, List<FilterItem> statuses) {

    this.context = context;
    this.statuses = statuses;
    this.inflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount() {
    return statuses.size();
  }

  @Override
  public FilterItem getItem(int position) {
    return statuses.get(position);
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

    ( (TextView) view.findViewById(R.id.filter_name)  ).setText( filterItem.getName()  );
    ( (TextView) view.findViewById(R.id.filter_count) ).setText( filterItem.getCount() );

    return view;
  }

  private FilterItem getFilterItem(int position) {
    return ((FilterItem) getItem(position));
  }

  ArrayList<FilterItem> getBox() {
    ArrayList<FilterItem> box = new ArrayList<FilterItem>();
    // for (FilterItem p : statuses) {
    // if p.getName() == 1
    //   box.add(p)
    // }
    return box;
  }

  public int getPosition() {
    return mPos;
  }

  public int prev() {

    if (statuses == null || statuses.size() == 0){
      return 0;
    }

    int position = mPos - 1;

    if ( position < 0 ){
      return statuses.size() - 1;
    } else {
      return position;
    }

  }

  public int next() {
    if (statuses == null || statuses.size() == 0){
      return 0;
    }

    int position = mPos + 1;

    if ( position >= statuses.size() ){
      return 0;
    } else {
      return position;
    }
  }

//  CompoundButton.OnCheckedChangeListener myCheckChangeList = new CompoundButton.OnCheckedChangeListener() {
//    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//      FilterItem item = getFilterItem((Integer) buttonView.getTag());
//      item.setName("isChecked");
//    }
//  };
}
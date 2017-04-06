package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;

public class PrimaryUsersAdapter extends BaseAdapter implements Filterable {

  private Context context;
  private final ArrayList<PrimaryConsiderationPeople> sourceItems;
  private ArrayList<PrimaryConsiderationPeople> resultItems;
  private String TAG = DecisionAdapter.class.getSimpleName();

  public PrimaryUsersAdapter(Context context, ArrayList<PrimaryConsiderationPeople> items) {
    this.sourceItems = items;
    this.resultItems = items;
    this.context = context;
  }


  @Override
  public View getView(int position, View view, ViewGroup parent) {
    ViewHolder viewHolder;

    if (view == null){
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = inflater.inflate(R.layout.primary_user, parent, false);

      viewHolder = new ViewHolder();
      viewHolder.name = (TextView) view.findViewById(R.id.primary_user__name);
      viewHolder.title = (TextView) view.findViewById(R.id.primary_user__title);


      view.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) view.getTag();
    }
    PrimaryConsiderationPeople user = getItem(position);

    viewHolder.name.setText( user.getName() );
    viewHolder.title.setText( user.getOrganization() );

    return view;
  }

  public void add(PrimaryConsiderationPeople user) {
    sourceItems.add( user );
    notifyDataSetChanged();
  }

  public void addAll(ArrayList<PrimaryConsiderationPeople> users) {
    sourceItems.addAll(users);
    notifyDataSetChanged();
  }

  public Oshs getOshs(int position){

    PrimaryConsiderationPeople item = resultItems.get(position);

    Oshs oshs = new Oshs();
    oshs.setOrganization( item.getOrganization());
    oshs.setName( item.getName());
    oshs.setPosition( item.getPosition());
    oshs.setId( item.getId());

    return oshs;
  }

  @Override
  public Filter getFilter() {
    return new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults();
        if (constraint != null) {
          List<PrimaryConsiderationPeople> results = new ArrayList<>();
          for (PrimaryConsiderationPeople item : sourceItems) {
            if (item.getName().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
              results.add(item);
            }
          }
          filterResults.values = results;
          filterResults.count = results.size();
        }
        return filterResults;
      }

      @Override
      protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        if (filterResults != null && filterResults.count > 0) {
          resultItems = (ArrayList<PrimaryConsiderationPeople>) filterResults.values;
          notifyDataSetChanged();
        } else {
          resultItems = new ArrayList<>();
          notifyDataSetChanged();
        }
      }
    };
  }

  public void cancelFiltering() {
    resultItems = sourceItems;
    notifyDataSetChanged();
  }

  private static class ViewHolder {
    public TextView name;
    public TextView title;
  }

  @Override
  public int getCount() {
    return resultItems.size();
  }

  public PrimaryConsiderationPeople getItem(int i){
    return resultItems.get(i);
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

  @Override
  public void notifyDataSetChanged() {
    Collections.sort(resultItems, new Comparator<PrimaryConsiderationPeople>() {
      @Override
      public int compare(PrimaryConsiderationPeople a, PrimaryConsiderationPeople b)
      {

        return  a.getName().compareTo( b.getName() );
      }
    });

    super.notifyDataSetChanged();
  }

}

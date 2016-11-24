package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import sapotero.rxtest.R;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;

public class PrimaryUsersAdapter extends BaseAdapter {

  private Context context;
  private final ArrayList<PrimaryConsiderationPeople> items;
  private String TAG = DecisionAdapter.class.getSimpleName();

  public PrimaryUsersAdapter(Context context, ArrayList<PrimaryConsiderationPeople> items) {
    this.items = items;
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
    items.add( user );
    notifyDataSetChanged();
  }


  private static class ViewHolder {
    public TextView name;
    public TextView title;
  }

  @Override
  public int getCount() {
    return items.size();
  }

  public PrimaryConsiderationPeople getItem(int i){
    return items.get(i);
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

}

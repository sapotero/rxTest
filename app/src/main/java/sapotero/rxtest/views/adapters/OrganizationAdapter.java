package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.views.adapters.models.OrganizationItem;

public class OrganizationAdapter extends BaseAdapter {
  private List<OrganizationItem> organizations;
  private Context context;
  private LayoutInflater inflter;

  private int mPos;

  public OrganizationAdapter(Context context, List<OrganizationItem> organizations) {

    this.context = context;
    this.organizations = organizations;
    this.inflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount() {
    return organizations.size();
  }

  @Override
  public OrganizationItem getItem(int position) {
    return organizations.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    View view = convertView;
    if (view == null) {
      view = inflter.inflate(R.layout.organizations_spinner_item, parent, false);
    }

    mPos = position;

    OrganizationItem filterItem = getOrganizationItem(position);

    ( (TextView) view.findViewById(R.id.organization_name)  ).setText( filterItem.getName()  );
    ( (TextView) view.findViewById(R.id.organization_count) ).setText( filterItem.getCount() );

    return view;
  }

  private OrganizationItem getOrganizationItem(int position) {
    return getItem(position);
  }


  public int getPosition() {
    return mPos;
  }

  public void add(OrganizationItem organizationItem) {
    this.organizations.add(organizationItem);
    notifyDataSetChanged();
  }

  public void clear() {
    this.organizations = new ArrayList<>();
  }
}
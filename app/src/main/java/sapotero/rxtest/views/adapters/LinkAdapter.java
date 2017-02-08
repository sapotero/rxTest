package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import sapotero.rxtest.R;
import sapotero.rxtest.views.adapters.models.Link;

public class LinkAdapter extends BaseAdapter {
  private Context mContext;
  private LayoutInflater layoutInflater;
  private ArrayList<Link> links;

  public LinkAdapter(Context context, ArrayList<Link> links) {
    mContext = context;
    this.links = links;
    layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount() {
    return links.size();
  }

  @Override
  public Link getItem(int position) {
    return links.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = convertView;
    if (view == null) {
      view = layoutInflater.inflate(R.layout.adapter_link_item, parent, false);
    }
    Link image = getImage(position);
    ((TextView) view.findViewById(R.id.link_adapter_title)).setText( image.getTitle() );

    return view;
  }

  private Link getImage(int position) {
    return ((Link) getItem(position));
  }

  public void add(Link image) {
    links.add( image );
    notifyDataSetChanged();
  }

  public ArrayList<Link> getItems() {
    return links;
  }
}
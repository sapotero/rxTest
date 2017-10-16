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
import sapotero.rxtest.retrofit.models.document.Image;

public class DocumentLinkAdapter extends BaseAdapter {

  private LayoutInflater layoutInflater;
  private ArrayList<Image> images;

  public DocumentLinkAdapter(Context context, ArrayList<Image> products) {
    images = products;
    layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount() {
    return images.size();
  }

  @Override
  public Image getItem(int position) {
    return images.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = convertView;

    if (view == null) {
      view = layoutInflater.inflate(R.layout.adapter_document_link_item, parent, false);
    }

    Image image = getItem(position);
    ((TextView) view.findViewById(R.id.document_link_adapter_title)).setText( image.getTitle() );

    return view;
  }

  public void addAll(List<Image> imageList) {
    images.addAll( imageList );
    notifyDataSetChanged();
  }

  public ArrayList<Image> getItems() {
    return images;
  }

  public void clear() {
    images.clear();
    notifyDataSetChanged();
  }
}
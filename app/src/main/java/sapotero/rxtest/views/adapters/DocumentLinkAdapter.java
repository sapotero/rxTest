package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import sapotero.rxtest.R;
import sapotero.rxtest.db.requery.models.images.RImageEntity;
import sapotero.rxtest.retrofit.models.document.Image;

public class DocumentLinkAdapter extends BaseAdapter {
  private Context mContext;
  private LayoutInflater layoutInflater;
  private ArrayList<Image> images;

  public DocumentLinkAdapter(Context context, ArrayList<Image> products) {
    mContext = context;
    images = products;
    layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    Image image = getImage(position);
    ((TextView) view.findViewById(R.id.document_link_adapter_title)).setText( image.getTitle() );

    return view;
  }

  private Image getImage(int position) {
    return ((Image) getItem(position));
  }

  public void add(RImageEntity img) {
    Image image = new Image();
    image.setTitle(img.getTitle());
    image.setPath(img.getPath());
    image.setMd5(img.getMd5());
    image.setContentType(img.getContentType());
    image.setNumber(img.getNumber());
    image.setSize(img.getSize());
    images.add( image );
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
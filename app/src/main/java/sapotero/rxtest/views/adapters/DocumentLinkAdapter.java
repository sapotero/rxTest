package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import sapotero.rxtest.R;
import sapotero.rxtest.retrofit.models.document.Image;

public class DocumentLinkAdapter extends BaseAdapter {
  private Context mContext;
  private LayoutInflater layoutInflater;
  private ArrayList<Image> mImages;

  public DocumentLinkAdapter(Context context, ArrayList<Image> products) {
    mContext = context;
    mImages  = products;
    layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount() {
    return mImages.size();
  }

  @Override
  public Object getItem(int position) {
    return mImages.get(position);
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

}
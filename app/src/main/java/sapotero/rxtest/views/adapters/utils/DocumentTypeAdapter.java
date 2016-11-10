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
import sapotero.rxtest.views.adapters.models.DocumentTypeItem;

public class DocumentTypeAdapter extends BaseAdapter {
  private List<DocumentTypeItem> organizations;
  private Context context;
  private LayoutInflater inflter;

  private int mPos;

  public DocumentTypeAdapter(Context context, List<DocumentTypeItem> organizations) {

    this.context = context;
    this.organizations = organizations;
    this.inflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount() {
    return organizations.size();
  }

  @Override
  public DocumentTypeItem getItem(int position) {
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
      view = inflter.inflate(R.layout.filter_documents_type_item, parent, false);
    }

    mPos = position;

    DocumentTypeItem item = getOrganizationItem(position);



    ( (TextView) view.findViewById(R.id.document_type_name)  ).setText( item.getName()  );
    ( (TextView) view.findViewById(R.id.document_type_count) ).setText( item.getCount() );

    return view;
  }

  private DocumentTypeItem getOrganizationItem(int position) {
    return getItem(position);
  }


  public int getPosition() {
    return mPos;
  }

  public void add(DocumentTypeItem organizationItem) {
    this.organizations.add(organizationItem);
    notifyDataSetChanged();
  }

  public void clear() {
    this.organizations = new ArrayList<>();
  }
}

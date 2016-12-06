package sapotero.rxtest.views.adapters.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import sapotero.rxtest.R;
import sapotero.rxtest.views.adapters.models.DocumentTypeItem;

public class DocumentTypeAdapter extends BaseAdapter {
  private List<DocumentTypeItem> documents;
  private Context context;
  private LayoutInflater inflter;

  private int mPos;

  public DocumentTypeAdapter(Context context, List<DocumentTypeItem> organizations) {

    this.context = context;
    this.documents = organizations;
    this.inflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount() {
    return documents.size();
  }

  @Override
  public DocumentTypeItem getItem(int position) {
    return documents.get(position);
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

    DocumentTypeItem item = getItem(position);



    ( (TextView) view.findViewById(R.id.document_type_name)  ).setText( item.getName()  );
//    ( (TextView) view.findViewById(R.id.document_type_count) ).setText( item.getValue() );

    return view;
  }

  private DocumentTypeItem getOrganizationItem(int position) {
    return getItem(position);
  }


  public int getPosition() {
    return mPos;
  }

  public void add(DocumentTypeItem organizationItem) {
    this.documents.add(organizationItem);
    notifyDataSetChanged();
  }

  public void clear() {
    this.documents.clear();
    notifyDataSetChanged();
  }

  public Integer findByValue(String value) {

    int index = Arrays.asList((context.getResources().getStringArray(R.array.settings_view_start_page_values))).indexOf(String.valueOf(value));
    List<String> names = Arrays.asList((context.getResources().getStringArray(R.array.settings_view_start_page)));

//    Timber.tag("findByValue index").i( String.valueOf(index) );
//    Timber.tag("findByValue value").i( value );

    for (int i = 0; i < documents.size(); i++) {
      if ( Objects.equals(documents.get(i).getName(), names.get(index)) ){
        index = i;
        break;
      }
    }

//    Timber.tag("findByValue result").i( documents.get(index).getName() );
    return index;
  }

  public void updateCountByType(String uid) {
    String type = String.format("%.2s", uid);


//    for (int i = 0; i < this.documents.size(); i++) {
//      int _int = Integer.valueOf(documents.get(i).getType());
//
////      Timber.tag("updateCountByType").e("%s == %s", String.format("%02d", _int ) , Integer.valueOf(type));
//      if( Objects.equals( Integer.valueOf(documents.get(i).getType()) , Integer.valueOf(type)) ){
//        documents.get(i).setValue( Integer.parseInt(documents.get(i).getValue()) + 1 );
//        documents.get(0).setValue( Integer.parseInt(documents.get(0).getValue()) + 1 );
//        notifyDataSetChanged();
//        break;
//      }
//    }
  }

}

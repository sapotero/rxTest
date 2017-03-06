package sapotero.rxtest.views.adapters;

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
  private View view;
  private TextView text;

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

    view = convertView;
    if (view == null) {
      view = inflter.inflate(R.layout.filter_documents_type_item, parent, false);
    }

    mPos = position;

    DocumentTypeItem item = getItem(position);


    text = ( (TextView) view.findViewById(R.id.document_type_name)  );
    text.setText( item.getName()  );
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

    for (int i = 0; i < documents.size(); i++) {
      if ( Objects.equals(documents.get(i).getName(), names.get(index)) ){
        index = i;
        break;
      }
    }

    return index;
  }

  public void updateCountByType(String uid) {
    String type = String.format("%.2s", uid);
  }

  public int prev() {
    if (documents == null || documents.size() == 0){
      return 0;
    }

    int position = mPos - 1;

    if ( position < 0 ){
      return documents.size() - 1;
    } else {
      return position;
    }

  }
  public int next() {
    if (documents == null || documents.size() == 0){
      return 0;
    }

    int position = mPos + 1;

    if ( position >= documents.size() ){
      return 0;
    } else {
      return position;
    }
  }

  public void invalidate() {
    clear();
    addAll(documents);
    notifyDataSetChanged();
  }

  private void addAll(List<DocumentTypeItem> documents) {
    this.documents.clear();
    this.documents = documents;
  }
}

package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.views.adapters.models.DocumentTypeItem;

public class DocumentTypeAdapter extends BaseAdapter {

  @Inject Settings settings;

  private List<DocumentTypeItem> documents;
  private Context context;
  private LayoutInflater inflter;

  private int mPos;
  private View view;
  private TextView text;
  private LinearLayout wrapper;

  public DocumentTypeAdapter(Context context, List<DocumentTypeItem> organizations) {


    this.context = context;
    this.documents = organizations;
    this.inflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    EsdApplication.getDataComponent().inject(this);

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
    item.setText(text);

    return view;
  }

  @Override
  public View getDropDownView(int position, View convertView, ViewGroup parent) {
    View v;

    Set<String> visible_journals = settings.getJournals();
    int index =  documents.get(position).getMainMenuItem().getIndex();

    if ( !Arrays.asList(0,8,9,10,11).contains(index) ){

      assert visible_journals != null;
      if ( !visible_journals.contains( String.valueOf( index ) ) ){
        TextView tv = new TextView(context);
        tv.setVisibility(View.GONE);
        tv.setHeight(0);
        v = tv;
        v.setVisibility(View.GONE);
      } else {
        v = super.getDropDownView(position, null, parent);
      }
    } else {
      v = super.getDropDownView(position, null, parent);
    }

    return v;
  }


  public int getPosition() {
    return mPos;
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
    notifyDataSetChanged();
  }

}

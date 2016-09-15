package sapotero.rxtest.views.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.models.documents.Document;

public class DocumentsAdapter extends RecyclerView.Adapter<DocumentsAdapter.DocumentViewHolder> {

  public static class DocumentViewHolder extends RecyclerView.ViewHolder {

    CardView cv;
    TextView title;
    TextView date;
    TextView from;
    TextView md5;

    DocumentViewHolder(View itemView) {
      super(itemView);
      cv    = (CardView)itemView.findViewById(R.id.cv);

      title = (TextView)itemView.findViewById(R.id.title);
      from  = (TextView)itemView.findViewById(R.id.from);
      md5   = (TextView)itemView.findViewById(R.id.md5);
      date   = (TextView)itemView.findViewById(R.id.date);
    }
  }

  List<Document> documents;

  public DocumentsAdapter(List<Document> documents){
    this.documents = documents;
  }

  @Override
  public void onAttachedToRecyclerView(RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
  }

  @Override
  public DocumentViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
    View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.document_item, viewGroup, false);
    DocumentViewHolder documentViewHolder = new DocumentViewHolder(v);
    return documentViewHolder;
  }

  @Override
  public void onBindViewHolder(DocumentViewHolder documentViewHolder, int i) {
    documentViewHolder.title.setText(documents.get(i).getTitle() );
    documentViewHolder.from.setText(documents.get(i).getSigner().getOrganisation());
    documentViewHolder.md5.setText(documents.get(i).getMd5());
    documentViewHolder.date.setText(documents.get(i).getRegistrationDate());
  }

  @Override
  public int getItemCount() {
    return documents.size();
  }

  public Document getItem(int position) {
    return this.documents.get(position);
  }
}
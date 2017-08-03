package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.views.activities.InfoActivity;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
  private final Context context;
  private final List<RDocumentEntity> mDataset;

  @Inject ISettings settings;
  @Inject MemoryStore store;

  public SearchResultAdapter(Context context, List<RDocumentEntity> mDataset) {
    this.context = context;
    this.mDataset = mDataset;

    EsdApplication.getManagerComponent().inject(this);
  }

  @Override
  public SearchResultAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_query_item, parent, false);
    ViewHolder vh = new ViewHolder(v);
    return vh;
  }

  @Override
  public void onBindViewHolder(SearchResultAdapter.ViewHolder holder, int position) {
    RDocumentEntity _doc = mDataset.get(position);

    InMemoryDocument doc = store.getDocuments().get(_doc.getUid()  );

    holder.mNumber.setText( _doc.getRegistrationNumber() );
    holder.mTitle.setText(  _doc.getShortDescription() );

    if (doc != null) {

      holder.mCard.setOnClickListener(v -> {
        settings.setUid( doc.getUid() );
        settings.setIsProject( doc.isProject() );
        settings.setRegNumber( doc.getDocument().getRegistrationNumber() );
        settings.setStatusCode( doc.getFilter() );
        settings.setRegDate( doc.getDocument().getRegistrationDate() );
        settings.setLoadFromSearch( true );

        Intent intent = new Intent(context, InfoActivity.class);
        context.startActivity(intent);
      });
    }
  }

  @Override
  public int getItemCount() {
    return mDataset.size();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView mNumber;
    private final TextView mTitle;
    private final CardView mCard;

    ViewHolder(View v) {
      super(v);
      mCard   = (CardView) v.findViewById(R.id.query_card);
      mNumber = (TextView) v.findViewById(R.id.query_number);
      mTitle  = (TextView) v.findViewById(R.id.query_title);
    }
  }
}

package sapotero.rxtest.views.adapters.pagination;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;

import java.util.ArrayList;
import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.retrofit.models.documents.Document;

public class PagingRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int MAIN_VIEW = 0;

  private List<Document> listElements = new ArrayList<>();
  // after reorientation test this member
  // or one extra request will be sent after each reorientation
  private boolean allItemsLoaded;

  static class MainViewHolder extends RecyclerView.ViewHolder {

    private TextView badge;
    private TextView control_label;
    private TextView favorite_label;
    private SwipeLayout swipeLayout;
    private TextView to_favorites;
    private TextView to_contol;
    private ImageButton to_actiob;

    private CardView cv;
    private TextView title;
    private TextView date;
    private TextView from;

    public MainViewHolder(View itemView) {
      super(itemView);
      swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);

      to_contol = (TextView) itemView.findViewById(R.id.swipe_layout_card_to_control);
      to_favorites = (TextView) itemView.findViewById(R.id.swipe_layout_card_to_favorites);
      to_actiob = (ImageButton) itemView.findViewById(R.id.swipe_layout_card_to_action);

      cv    = (CardView)itemView.findViewById(R.id.swipe_layout_cv);
      title = (TextView)itemView.findViewById(R.id.swipe_layout_title);
      badge = (TextView)itemView.findViewById(R.id.swipe_layout_urgency_badge);
      from  = (TextView)itemView.findViewById(R.id.swipe_layout_from);
      date   = (TextView)itemView.findViewById(R.id.swipe_layout_date);
      favorite_label   = (TextView)itemView.findViewById(R.id.favorite_label);
      control_label   = (TextView)itemView.findViewById(R.id.control_label);

    }
  }

  public void addNewItems(List<Document> items) {
    if (items.size() == 0) {
      allItemsLoaded = true;
      return;
    }
    listElements.addAll(items);
  }

  public boolean isAllItemsLoaded() {
    return allItemsLoaded;
  }

  @Override
  public long getItemId(int position) {
    return getItem(position).getSortKey();
  }

  public Document getItem(int position) {
    return listElements.get(position);
  }

  @Override
  public int getItemCount() {
    return listElements.size();
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == MAIN_VIEW) {
      View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.documents_adapter_item_layout, parent, false);
      return new MainViewHolder(v);
    }
    return null;
  }

  @Override
  public int getItemViewType(int position) {
    return MAIN_VIEW;
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    switch (getItemViewType(position)) {
      case MAIN_VIEW:
        onBindTextHolder(holder, position);
        break;
    }
  }

  private void onBindTextHolder(RecyclerView.ViewHolder holder, int position) {
    MainViewHolder viewHolder = (MainViewHolder) holder;
    Document item = getItem(position);

    viewHolder.title.setText(item.getTitle() );
    viewHolder.from.setText( item.getSigner().getOrganisation());
    viewHolder.date.setText( item.getExternalDocumentNumber() + " от " + item.getRegistrationDate());

    viewHolder.date.setText( item.getExternalDocumentNumber() + " от " + item.getRegistrationDate());

    if ( item.getUrgency() != null ){
      viewHolder.badge.setText( item.getUrgency() );
    } else {
      viewHolder.badge.setVisibility(View.INVISIBLE);
    }
  }

}
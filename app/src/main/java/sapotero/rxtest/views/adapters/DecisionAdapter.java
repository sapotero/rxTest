package sapotero.rxtest.views.adapters;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.views.activities.DecisionConstructorActivity;
import sapotero.rxtest.views.adapters.holders.DecisionViewHolder;
import timber.log.Timber;

public class DecisionAdapter extends RecyclerView.Adapter<DecisionViewHolder> {

  private final RecyclerView recycler_view;
  private List<Decision> decisions = Collections.emptyList();
  private Context context;

  private String TAG = DecisionAdapter.class.getSimpleName();

  public DecisionAdapter(List<Decision> decisions, Context context, RecyclerView recyclerView) {
    this.decisions = decisions;
    this.context = context;
    this.recycler_view = recyclerView;
  }

  @Override
  public DecisionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.decision_item, parent, false);

    view.setOnClickListener(listener -> {

      int pos = recycler_view.getChildAdapterPosition(listener);



      if (pos >= 0 && pos < getItemCount()) {
        Timber.tag(TAG).v( "COMMENT " + decisions.get(pos).getSigner() );
      }
    });

    view.setOnLongClickListener(v -> {
      int pos = recycler_view.getChildAdapterPosition(v);

      if (pos >= 0 && pos < getItemCount()) {


        Gson gson = new Gson();
        Decision data = decisions.get(pos);
        String json = gson.toJson(data, Decision.class);

        Timber.tag("LONG CLICK").i( json );


        Intent intent = new Intent(context, DecisionConstructorActivity.class);
        intent.putExtra("decision", json);
        context.startActivity(intent);


      }
      return false;
    });

    final DecisionViewHolder viewHolder = new DecisionViewHolder(view);

    return viewHolder;

  }

  @Override
  public void onBindViewHolder(DecisionViewHolder holder, int position) {

    Decision item = decisions.get(position);

    holder.title.setText( item.getSignerBlankText() );
    holder.date.setText( item.getDate() );

//    holder. .setSelected(mSelectedRows.contains(i));

  }

  @Override
  public int getItemCount() {
    return decisions.size();
  }
  public Decision getItem(int i){
    return decisions.get(i);
  }

  @Override
  public void onAttachedToRecyclerView(RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
  }

  public void insert(int position, Decision data) {
    decisions.add(position, data);
    notifyItemInserted(position);
  }

  public void remove(Decision data) {
    int position = decisions.indexOf(data);
    decisions.remove(position);
    notifyItemRemoved(position);
  }
}
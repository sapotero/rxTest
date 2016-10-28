package sapotero.rxtest.views.adapters;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.birbit.android.jobqueue.JobManager;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.jobs.rx.SetActiveDecisionJob;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.views.activities.DecisionConstructorActivity;
import sapotero.rxtest.views.adapters.holders.DecisionViewHolder;
import timber.log.Timber;

public class DecisionAdapter extends RecyclerView.Adapter<DecisionViewHolder> {
  @Inject JobManager jobManager;

  private final RecyclerView recycler_view;
  private List<Decision> list = Collections.emptyList();
  private Context context;

  private String TAG = DecisionAdapter.class.getSimpleName();

  public DecisionAdapter(List<Decision> decisions, Context context, RecyclerView recyclerView) {
    this.list = decisions;
    this.context = context;
    this.recycler_view = recyclerView;
    EsdApplication.getComponent(context).inject(this);
  }

  @Override
  public DecisionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.decision_item, parent, false);

    view.setOnClickListener(listener -> {

      int pos = recycler_view.getChildAdapterPosition(listener);

      if (pos >= 0 && pos < getItemCount()) {
        Timber.tag(TAG).v( "COMMENT " + list.get(pos).getSigner() );

        try {
          jobManager.addJobInBackground( new SetActiveDecisionJob( pos ) );
        } catch ( Exception e){
          Timber.tag(TAG + " massInsert error").v( e );
        }
      }
    });

    view.setOnLongClickListener(v -> {
      int pos = recycler_view.getChildAdapterPosition(v);

      if (pos >= 0 && pos < getItemCount()) {


        Gson gson = new Gson();
        Decision data = list.get(pos);
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

    Decision item = list.get(position);

    holder.title.setText( item.getSignerBlankText() );
    holder.date.setText( item.getDate() );

  }

  @Override
  public int getItemCount() {
    return list.size();
  }
  public Decision getItem(int i){
    return list.get(i);
  }

  @Override
  public void onAttachedToRecyclerView(RecyclerView recyclerView) {
    super.onAttachedToRecyclerView(recyclerView);
  }

  public void insert(int position, Decision data) {
    list.add(position, data);
    notifyItemInserted(position);
  }

  public void remove(Decision data) {
    int position = list.indexOf(data);
    list.remove(position);
    notifyItemRemoved(position);
  }
}
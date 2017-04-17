package sapotero.rxtest.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.db.requery.models.RRejectionTemplateEntity;
import sapotero.rxtest.views.fragments.DecisionRejectionTemplateFragment;

public class DecisionRejectionTemplateRecyclerAdapter extends RecyclerView.Adapter<DecisionRejectionTemplateRecyclerAdapter.ViewHolder> {

  private final List<RRejectionTemplateEntity> mValues;
  private final DecisionRejectionTemplateFragment.OnListFragmentInteractionListener mListener;

  public DecisionRejectionTemplateRecyclerAdapter(List<RRejectionTemplateEntity> items, DecisionRejectionTemplateFragment.OnListFragmentInteractionListener listener) {
    mValues = items;
    mListener = listener;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_decision_rejection_template_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    RRejectionTemplateEntity item = mValues.get(position);

    holder.mItem = item;
    holder.mIdView.setText( item.getId() );
    holder.mContentView.setText( item.getTitle() );

    holder.mView.setOnClickListener(v -> {
      if (null != mListener) {
        mListener.onListFragmentInteraction(holder.mItem);
      }
    });
  }

  @Override
  public int getItemCount() {
    return mValues.size();
  }

  public void addItem(RRejectionTemplateEntity tmp) {
    mValues.add(tmp);
    notifyItemInserted(mValues.size());
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mIdView;
    public final TextView mContentView;
    public RRejectionTemplateEntity mItem;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mIdView = (TextView) view.findViewById(R.id.id);
      mContentView = (TextView) view.findViewById(R.id.content);
    }

    @Override
    public String toString() {
      return super.toString() + " '" + mContentView.getText() + "'";
    }
  }
}

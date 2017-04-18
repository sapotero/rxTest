package sapotero.rxtest.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.views.fragments.DecisionRejectionTemplateFragment;
import sapotero.rxtest.views.fragments.dummy.DummyContent.DummyItem;

public class DecisionRejectionTemplateRecyclerViewAdapter extends RecyclerView.Adapter<DecisionRejectionTemplateRecyclerViewAdapter.ViewHolder> {

  private final List<DummyItem> mValues;
  private final DecisionRejectionTemplateFragment.OnListFragmentInteractionListener mListener;

  public DecisionRejectionTemplateRecyclerViewAdapter(List<DummyItem> items, DecisionRejectionTemplateFragment.OnListFragmentInteractionListener listener) {
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
    holder.mItem = mValues.get(position);
    holder.mIdView.setText(mValues.get(position).id);
    holder.mContentView.setText(mValues.get(position).content);

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

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mIdView;
    public final TextView mContentView;
    public DummyItem mItem;

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

package sapotero.rxtest.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.views.fragments.DecisionTemplateFragment;

public class DecisionTemplateRecyclerAdapter extends RecyclerView.Adapter<DecisionTemplateRecyclerAdapter.ViewHolder> {

  private final List<RTemplateEntity> mValues;
  private final DecisionTemplateFragment.OnListFragmentInteractionListener mListener;

  public DecisionTemplateRecyclerAdapter(List<RTemplateEntity> items, DecisionTemplateFragment.OnListFragmentInteractionListener mListener) {
    this.mValues = items;
    this.mListener = mListener;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_decision_template_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {

    RTemplateEntity item = mValues.get(position);

    if (item != null) {
      holder.mItem = item;
      holder.mContentView.setText(item.getTitle());

      holder.mView.setOnLongClickListener(view -> {
        if (null != mListener) {
          mListener.onListFragmentInteraction(holder.mItem);
        }
        return false;
      });
    }

  }

  @Override
  public int getItemCount() {
    return mValues.size();
  }

  public void addItem(RTemplateEntity tmp) {
    mValues.add(tmp);
    notifyItemInserted(mValues.size());
  }

  public void addList(List<RTemplateEntity> list) {
    mValues.clear();
    notifyDataSetChanged();
    for (RTemplateEntity tmp: list) {
      addItem(tmp);
    }

  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    RTemplateEntity mItem;
    final View mView;
    final TextView mContentView;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mContentView = (TextView) view.findViewById(R.id.content);
    }

    @Override
    public String toString() {
      return super.toString() + " '" + mContentView.getText() + "'";
    }
  }
}

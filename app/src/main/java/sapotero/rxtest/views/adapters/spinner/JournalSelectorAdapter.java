package sapotero.rxtest.views.adapters.spinner;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.views.menu.fields.MainMenuItem;


public class JournalSelectorAdapter extends RecyclerView.Adapter<JournalSelectorAdapter.ViewHolder> {


  private final Context context;
  private final List<MainMenuItem> items;

  public JournalSelectorAdapter(Context context, List<MainMenuItem> items) {
    this.context = context;
    this.items = items;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_journal_selector_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    final MainMenuItem item = items.get(position);

    holder.title.setText( item.getName() );
  }

  @Override
  public int getItemCount() {
    return items.size();
  }


  class ViewHolder extends RecyclerView.ViewHolder {

    private final TextView title;

    public ViewHolder(View itemView) {
      super(itemView);
      title = (TextView) itemView.findViewById(R.id.journal_selector_adapter_item_title);

    }

  }
}

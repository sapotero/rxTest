package sapotero.rxtest.views.adapters.spinner;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.googlecode.totallylazy.Sequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.Filter;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import sapotero.rxtest.views.menu.fields.MainMenuButton;
import sapotero.rxtest.views.menu.fields.MainMenuItem;
import timber.log.Timber;

import static com.googlecode.totallylazy.Sequences.sequence;


public class JournalSelectorAdapter extends RecyclerView.Adapter<JournalSelectorAdapter.ViewHolder> {
  @Inject MemoryStore store;
  private List<String> items;

  private ItemCallback itemCallback;

  public JournalSelectorAdapter() {
    EsdApplication.getManagerComponent().inject(this);
    process();
  }

  private void process() {
    Sequence<InMemoryDocument> imd = sequence( store.getDocuments().values() );
    this.items = sequence( MainMenuItem.values() )
      .mapConcurrently( item-> {

        String result = "noop";

        result = item.getIndex() == 0 ? getAllJournalCount(imd, item) : getJournalCount(item, imd);

        return result;
      }).toList();
  }

  private String getJournalCount(MainMenuItem item, Sequence<InMemoryDocument> imd) {
    ArrayList<ConditionBuilder> conditions = new ArrayList<>();
    Collections.addAll( conditions, item.getCountConditions() );
    Filter filter = new Filter(conditions);

    int count = getCount(imd, filter);

    String result = "fake";

    try {
      result = String.format( item.getName(), count );
    } catch (Exception e) {
      Timber.e(e);
    }

    return result;
  }

  private int getCount(Sequence<InMemoryDocument> imd, Filter filter) {
    return imd
      .filter( filter::byYear)
      .filter( filter::byType)
      .filter( filter::byStatus)
      .filter( filter::isProcessed )
      .filter( filter::isFavorites )
      .filter( filter::isControl )
      .toList().size();
  }

  private String getAllJournalCount(Sequence<InMemoryDocument> _docs, MainMenuItem item) {

    ArrayList<ConditionBuilder> _projects  = new ArrayList<ConditionBuilder>();
    ArrayList<ConditionBuilder> _primary = new ArrayList<ConditionBuilder>();
    ArrayList<ConditionBuilder> _report  = new ArrayList<ConditionBuilder>();

    Collections.addAll( _projects, MainMenuItem.APPROVE_ASSIGN.getCountConditions() );
    Collections.addAll( _primary,  MainMenuButton.PRIMARY_CONSIDERATION.getConditions());
    Collections.addAll( _report,   MainMenuButton.PERFORMANCE.getConditions() );

    List<ArrayList<ConditionBuilder>> conditions = Arrays.asList(_projects, _primary, _report);

    List<Integer> total_count_list = sequence(conditions)
      .mapConcurrently(conds -> {
        Filter filter = new Filter(conds);
        return getCount(_docs, filter);
      }).toList();

    String result = "";

    try {
      result = String.format( item.getName(), total_count_list.get(1) + total_count_list.get(2), total_count_list.get(0) );
    } catch (Exception e) {
      Timber.e(e);
    }

    return result;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_journal_selector_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    holder.title.setText( items.get(position) );
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  public String getItem(int position) {
    return items.get(position);
  }

  public interface ItemCallback {
    void onItemClicked(int itemIndex);
  }
  public void setCallback(ItemCallback itemCallback) {
    this.itemCallback = itemCallback;
  }

  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final TextView title;

    public ViewHolder(View itemView) {
      super(itemView);
      title = (TextView) itemView.findViewById(R.id.journal_selector_adapter_item_title);
      title.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
      itemCallback.onItemClicked(getAdapterPosition());
    }
  }
}

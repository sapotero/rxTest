package sapotero.rxtest.views.adapters.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.googlecode.totallylazy.Sequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.Filter;
import sapotero.rxtest.views.adapters.DocumentsAdapter;
import sapotero.rxtest.views.adapters.decorators.GridSpacingItemDecoration;
import sapotero.rxtest.views.menu.builders.ButtonBuilder;
import timber.log.Timber;

import static com.googlecode.totallylazy.Sequences.sequence;

public class FragmentBuilder extends Fragment {

  @Inject MemoryStore store;
  @BindView(R.id.fragment_builder_recycle_view) RecyclerView recyclerView;

  private ButtonBuilder button;

  public FragmentBuilder setButton(ButtonBuilder button) {
    this.button = button;
    return this;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_builder, container, false);
    ButterKnife.bind(this, view);
    EsdApplication.getManagerComponent().inject( this );

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    populateAdapter();
  }

  private void populateAdapter() {

    int columnCount = 2;
    int spacing = 32;

    GridLayoutManager gridLayoutManager = new GridLayoutManager( getContext(), columnCount, GridLayoutManager.VERTICAL, false);

    DocumentsAdapter adapter = new DocumentsAdapter( getContext() , new ArrayList<>());

    recyclerView.addItemDecoration(new GridSpacingItemDecoration(columnCount, spacing, true));
    recyclerView.setLayoutManager(gridLayoutManager);
    recyclerView.setAdapter(adapter);

    Filter filter = new Filter( new ArrayList<>(Arrays.asList(button.getConditions())) );
    Sequence<InMemoryDocument> _docs = sequence(store.getDocuments().values());

    List<InMemoryDocument> lazy_docs = _docs
      .filter( filter::byYear)
      .filter( filter::byType)
      .filter( filter::byStatus)
      .filter( filter::isProcessed )
      .filter( filter::isFavorites )
      .filter( filter::isControl )
      .toList();

    for (InMemoryDocument doc :lazy_docs) {
      Timber.e("++imd %s", doc.getUid());
      adapter.addItem(doc);
    }

  }

  public Fragment build (){
    return this;
  }
}

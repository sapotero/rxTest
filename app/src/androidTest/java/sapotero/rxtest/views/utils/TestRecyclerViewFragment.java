package sapotero.rxtest.views.utils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import sapotero.rxtest.R;
import sapotero.rxtest.views.adapters.DocumentsAdapter;
import sapotero.rxtest.views.adapters.decorators.GridSpacingItemDecoration;

public class TestRecyclerViewFragment extends Fragment {

  private RecyclerView recyclerView;
  private DocumentsAdapter adapter;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_test_recycler_view, container, false);

    recyclerView = (RecyclerView) view.findViewById(R.id.testactivity_recycler_view);

    int columnCount = 2;
    int spacing = 32;

    GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), columnCount, GridLayoutManager.VERTICAL, false);

    adapter = new DocumentsAdapter(getActivity(), new ArrayList<>());

    recyclerView.addItemDecoration(new GridSpacingItemDecoration(columnCount, spacing, true));
    recyclerView.setLayoutManager(gridLayoutManager);
    recyclerView.setAdapter(adapter);

    return view;
  }

  public RecyclerView getRecyclerView() {
    return recyclerView;
  }

  public DocumentsAdapter getAdapter() {
    return adapter;
  }
}

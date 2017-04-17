package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sapotero.rxtest.R;
import sapotero.rxtest.views.adapters.DecisionRejectionTemplateRecyclerViewAdapter;
import sapotero.rxtest.views.fragments.dummy.DummyContent;
import sapotero.rxtest.views.fragments.dummy.DummyContent.DummyItem;

public class DecisionRejectionTemplateFragment extends Fragment {

  private static final String ARG_COLUMN_COUNT = "column-count";
  private int mColumnCount = 1;
  private OnListFragmentInteractionListener mListener;

  public DecisionRejectionTemplateFragment() {
  }

  public static DecisionRejectionTemplateFragment newInstance(int columnCount) {
    DecisionRejectionTemplateFragment fragment = new DecisionRejectionTemplateFragment();
    Bundle args = new Bundle();
    args.putInt(ARG_COLUMN_COUNT, columnCount);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_decision_rejection_template, container, false);

    Context context = view.getContext();
    RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.fragment_rejection_decision_template_list);

    if (mColumnCount <= 1) {
      recyclerView.setLayoutManager(new LinearLayoutManager(context));
    } else {
      recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
    }

    recyclerView.setAdapter(new DecisionRejectionTemplateRecyclerViewAdapter(DummyContent.ITEMS, mListener));
    return view;
  }


  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnListFragmentInteractionListener) {
      mListener = (OnListFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
        + " must implement OnListFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }
  public interface OnListFragmentInteractionListener {
    // TODO: Update argument type and name
    void onListFragmentInteraction(DummyItem item);
  }
}

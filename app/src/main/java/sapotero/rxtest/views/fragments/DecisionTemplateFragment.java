package sapotero.rxtest.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.views.adapters.DecisionTemplateRecyclerAdapter;

public class DecisionTemplateFragment extends Fragment {

  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  private OnListFragmentInteractionListener mListener;
  private DecisionTemplateRecyclerAdapter adapter;

  public DecisionTemplateFragment() {
  }


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_decision_template, container, false);

    EsdApplication.getComponent(getContext()).inject( this );

    Context context = view.getContext();
    RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.fragment_decision_template_list);
    recyclerView.setLayoutManager(new LinearLayoutManager(context));

    adapter = new DecisionTemplateRecyclerAdapter(new ArrayList<>(), mListener);

    populateAdapter();

    recyclerView.setAdapter(adapter);
    return view;
  }

  private void populateAdapter() {

    List<RTemplateEntity> templates = dataStore
      .select(RTemplateEntity.class)
      .where(RTemplateEntity.USER.eq( settings.getString("current_user").get() ))
      .get().toList();

    if (templates.size() > 0) {
      for (RTemplateEntity tmp : templates){
        adapter.addItem( tmp );
      }
    }
  }


  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnListFragmentInteractionListener) {
      mListener = (OnListFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public interface OnListFragmentInteractionListener {
    void onListFragmentInteraction(RTemplateEntity item);
  }
}

package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import sapotero.rxtest.R;
import sapotero.rxtest.views.adapters.models.DecisionSpinnerItem;

public class DecisionSpinnerAdapter extends BaseAdapter {
  private final String current_user;
  private List<DecisionSpinnerItem> decisions;
  private LayoutInflater inflter;
  private int template;
  private Context context;
  private int filterCount[];
  private String[] filterName;

  private TextView count;
  private TextView name;
  private View mainView;
  private int mPos;

  private ArrayList<DecisionSpinnerItem> suggestions = new ArrayList<>();

  public DecisionSpinnerAdapter(Context context, String current_user,  List<DecisionSpinnerItem> decisions) {
    this.current_user = current_user;
    this.context = context;
    this.decisions = decisions;
    this.inflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount() {
    return decisions.size();
  }

  @Override
  public DecisionSpinnerItem getItem(int position) {
    return decisions.get(position);
  }

  public void add(DecisionSpinnerItem item) {
    decisions.add(item);
    notifyDataSetChanged();
  }


  @Override
  public long getItemId(int position) {
    return position;
  }

  // пункт списка
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    View view = convertView;
    if (view == null) {
      view = inflter.inflate(R.layout.filter_decision_spinner_items, parent, false);
    }

    mPos = position;

    DecisionSpinnerItem item = getItem(position);

    ( (TextView) view.findViewById(R.id.decision_name) ).setText( item.getName() );
    ( (TextView) view.findViewById(R.id.decision_date) ).setText( item.getDate() );

    return view;
  }

  public int getPosition() {
    return mPos;
  }

  public int size() {
    return decisions.size();
  }

  public void clear() {
    this.decisions.clear();
  }


  public boolean hasActiveDecision() {
    Boolean result = false;

    try {
      for ( DecisionSpinnerItem decision: decisions ) {
        if (!decision.getDecision().isApproved() && Objects.equals(decision.getDecision().getSignerId(), current_user)){
          result = true;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }
}
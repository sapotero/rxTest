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

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.decisions.RBlock;
import sapotero.rxtest.db.requery.models.decisions.RBlockEntity;
import sapotero.rxtest.db.requery.models.decisions.RDisplayFirstDecisionEntity;
import sapotero.rxtest.db.requery.models.decisions.RPerformer;
import sapotero.rxtest.db.requery.models.decisions.RPerformerEntity;
import sapotero.rxtest.views.adapters.models.DecisionSpinnerItem;
import timber.log.Timber;

public class DecisionSpinnerAdapter extends BaseAdapter {

  @Inject SingleEntityStore<Persistable> dataStore;

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
  private String TAG = this.getClass().getSimpleName();

  public DecisionSpinnerAdapter(Context context, String current_user,  List<DecisionSpinnerItem> decisions) {
    this.current_user = current_user;
    this.context = context;
    this.decisions = decisions;
    this.inflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    EsdApplication.getDataComponent().inject(this);
  }

  @Override
  public int getCount() {
    return decisions.size();
  }

  @Override
  public DecisionSpinnerItem getItem(int position) {
    return position >= 0 && position <= decisions.size() - 1 ? decisions.get(position) : decisions.get(0);
  }

  public void add(DecisionSpinnerItem item) {
    decisions.add(item);
    notifyDataSetChanged();
  }

  public void addAll(List<DecisionSpinnerItem> items) {
    if (items.size() > 0){

      List<DecisionSpinnerItem> createdAndSigner = new ArrayList<>();
      List<DecisionSpinnerItem> list = new ArrayList<>();
      List<DecisionSpinnerItem> signer = new ArrayList<>();
      List<DecisionSpinnerItem> performer = new ArrayList<>();

      decisions.clear();

      for (DecisionSpinnerItem item: items ) {

        RDisplayFirstDecisionEntity rDisplayFirstDecisionEntity =
        dataStore
          .select(RDisplayFirstDecisionEntity.class)
          .where(RDisplayFirstDecisionEntity.DECISION_UID.eq( item.getDecision().getUid() ))
          .and(RDisplayFirstDecisionEntity.USER_ID.eq( current_user ))
          .get().firstOrNull();

        if ( rDisplayFirstDecisionEntity != null ) {
          createdAndSigner.add(item);
        } else if ( getPerformerIds(item).contains( current_user ) ){
          performer.add(item);
        } else if (Objects.equals(item.getDecision().getSignerId(), current_user)){
          signer.add(0, item);
        } else {
          list.add(item);
        }
      }

      decisions = new ArrayList<>();
      decisions.addAll(createdAndSigner);
      decisions.addAll(signer);
      decisions.addAll(performer);
      decisions.addAll(list);

      notifyDataSetChanged();
    }
  }

  public ArrayList<String> getPerformerIds(DecisionSpinnerItem decisionSpinnerItem){
    ArrayList<String> performers = new ArrayList<String>();

    for ( RBlock block : decisionSpinnerItem.getDecision().getBlocks()) {
      for ( RPerformer perf : ((RBlockEntity) block).getPerformers() ) {
        performers.add( ((RPerformerEntity) perf).getPerformerId() );
      }
    }

    return performers;
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

    for ( DecisionSpinnerItem decision: decisions ) {
      if (!decision.getDecision().isApproved() && Objects.equals(decision.getDecision().getSignerId(), current_user)){
        result = true;
        break;
      }
    }

    return result;
  }

  public void invalidate(String uid) {
    for (int i = 0; i < decisions.size(); i++) {
      DecisionSpinnerItem item = decisions.get(i);

      Timber.tag(TAG).e("%s\n%s", item.getDecision().getUid(), uid );

      if (Objects.equals(item.getDecision().getUid(), uid)){
        item.getDecision().setTemporary(true);
        notifyDataSetChanged();
        break;
      }
    }
  }

  public void setCurrentAsTemporary(int selectedItemPosition) {
    if (decisions.size() > 0 && selectedItemPosition >= 0 && decisions.size() >= selectedItemPosition){
      decisions.get(selectedItemPosition).getDecision().setTemporary(true);
      notifyDataSetChanged();
    }
  }
}
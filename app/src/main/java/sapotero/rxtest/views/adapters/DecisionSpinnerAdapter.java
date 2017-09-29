package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
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
import sapotero.rxtest.db.requery.models.decisions.RDisplayFirstDecisionEntity;
import sapotero.rxtest.retrofit.models.document.Block;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.Performer;
import timber.log.Timber;

public class DecisionSpinnerAdapter extends BaseAdapter {

  @Inject SingleEntityStore<Persistable> dataStore;

  private final String current_user;
  private final Context context;
  private List<Decision> decisions;
  private LayoutInflater inflater;
  private int mPos = -1;

  private String TAG = this.getClass().getSimpleName();

  public DecisionSpinnerAdapter(Context context, String current_user,  List<Decision> decisions) {
    this.context = context;
    this.current_user = current_user;
    this.decisions = decisions;
    this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    EsdApplication.getDataComponent().inject(this);
  }

  @Override
  public int getCount() {
    return decisions.size();
  }

  @Override
  public Decision getItem(int position) {
    return position >= 0 && position <= decisions.size() - 1 ? decisions.get(position) : decisions.get(0);
  }

  public void add(Decision item) {
    decisions.add(item);
    notifyDataSetChanged();
  }

  public void addAll(List<Decision> items) {
    if (items.size() > 0){

      List<Decision> createdAndSigner = new ArrayList<>();
      List<Decision> list = new ArrayList<>();
      List<Decision> signer = new ArrayList<>();
      List<Decision> performer = new ArrayList<>();

      decisions.clear();

      for (Decision item: items ) {

        RDisplayFirstDecisionEntity rDisplayFirstDecisionEntity =
        dataStore
          .select(RDisplayFirstDecisionEntity.class)
          .where(RDisplayFirstDecisionEntity.DECISION_UID.eq( item.getId() ))
          .and(RDisplayFirstDecisionEntity.USER_ID.eq( current_user ))
          .get().firstOrNull();

        if ( rDisplayFirstDecisionEntity != null ) {
          createdAndSigner.add(item);
        } else if ( getPerformerIds(item).contains( current_user ) ){
          performer.add(item);
        } else if (Objects.equals(item.getSignerId(), current_user)){
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

  private ArrayList<String> getPerformerIds(Decision item){
    ArrayList<String> performers = new ArrayList<>();

    for ( Block block : item.getBlocks()) {
      for ( Performer perf : block.getPerformers() ) {
        performers.add( perf.getPerformerId() );
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
      view = inflater.inflate(R.layout.filter_decision_spinner_items, parent, false);
    }
    Decision item = getItem(position);
    ( (TextView) view.findViewById(R.id.decision_name) ).setText( item.getSignerBlankText() );

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

    for ( Decision decision: decisions ) {
      if (decision.getApproved() != null && !decision.getApproved() && Objects.equals(decision.getSignerId(), current_user)){
        result = true;
        break;
      }
    }

    return result;
  }

  public void invalidate(String uid) {
    for (int i = 0; i < decisions.size(); i++) {
      Decision item = decisions.get(i);

      Timber.tag(TAG).e("%s\n%s", item.getId(), uid );

      if (Objects.equals(item.getId(), uid)){
        item.setChanged(true);
        notifyDataSetChanged();
        break;
      }
    }
  }

  public void setCurrentAsTemporary(int selectedItemPosition) {
    if (decisions.size() > 0 && selectedItemPosition >= 0 && decisions.size() >= selectedItemPosition){
      decisions.get(selectedItemPosition).setChanged(true);
      notifyDataSetChanged();
    }
  }

  public void setSelection(int position) {
    mPos =  position;
    notifyDataSetChanged();
  }

  @Override
  public View getDropDownView(int position, View convertView, ViewGroup parent) {
    Timber.e("getDropDownView: %s | %s", mPos, position);

    TextView itemView = (TextView) super.getDropDownView(position, convertView, parent);
    itemView.setTextColor( ContextCompat.getColor(context, position == mPos ? R.color.md_grey_900 : R.color.md_grey_600) );

    //resolved https://tasks.n-core.ru/browse/MVDESD-14112
    // увеличить ширину списка и увеличить отступ
    itemView.setPadding( 16, 24, 16, 24);
    itemView.setWidth(476);

    if (position == mPos){
      itemView.setPaintFlags( Paint.FAKE_BOLD_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG );
    }

    return itemView;
  }
}
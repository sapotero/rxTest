package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.views.activities.InfoActivity;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
  private final Context context;
  private final List<RDocumentEntity> mDataset;

  public SearchResultAdapter(Context context, List<RDocumentEntity> mDataset) {
    this.context = context;
    this.mDataset = mDataset;
  }

  @Override
  public SearchResultAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_query_item, parent, false);
    ViewHolder vh = new ViewHolder(v);
    return vh;
  }

  @Override
  public void onBindViewHolder(SearchResultAdapter.ViewHolder holder, int position) {
    RDocumentEntity doc = mDataset.get(position);
    holder.mNumber.setText( doc.getRegistrationNumber() );
    holder.mTitle.setText(  doc.getShortDescription() );

    holder.mCard.setOnClickListener(v -> {

      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
      RxSharedPreferences rxPreferences = RxSharedPreferences.create(preferences);
      Preference<Integer> rxPosition = rxPreferences.getInteger("position");
      rxPosition.set(position);

      Preference<String> rxUid = rxPreferences.getString("main_menu.uid");
      rxUid.set( doc.getUid() );

      Preference<String> rxReg = rxPreferences.getString("main_menu.regnumber");
      rxReg.set( doc.getRegistrationNumber() );

      Preference<String> rxStatus = rxPreferences.getString("main_menu.start");
      rxStatus.set( doc.getFilter() );


      Preference<String> rxDate = rxPreferences.getString("main_menu.date");
      rxDate.set( doc.getRegistrationDate() );

      Intent intent = new Intent(context, InfoActivity.class);
      context.startActivity(intent);
    });
  }

  @Override
  public int getItemCount() {
    return mDataset.size();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    private final TextView mNumber;
    private final TextView mTitle;
    private final CardView mCard;

    ViewHolder(View v) {
      super(v);
      mCard   = (CardView) v.findViewById(R.id.query_card);
      mNumber = (TextView) v.findViewById(R.id.query_number);
      mTitle  = (TextView) v.findViewById(R.id.query_title);
    }
  }
}

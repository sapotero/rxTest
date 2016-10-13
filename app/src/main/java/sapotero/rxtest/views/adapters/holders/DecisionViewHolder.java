package sapotero.rxtest.views.adapters.holders;


import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import sapotero.rxtest.R;

public class DecisionViewHolder extends RecyclerView.ViewHolder{

  public CardView cv;
  public TextView title;
  public TextView date;

  private String TAG = DecisionViewHolder.class.getSimpleName();

  public DecisionViewHolder(View itemView) {
    super(itemView);
    cv    = (CardView) itemView.findViewById(R.id.decision_card_view);
    title = (TextView) itemView.findViewById(R.id.decision_title);
    date  = (TextView) itemView.findViewById(R.id.decision_date);
  }

}
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.views.activities.InfoActivity;

public class DocumentsAdapter extends RecyclerSwipeAdapter<DocumentsAdapter.SimpleViewHolder> {


  private Context mContext;
  private List<Document> documents;

  public DocumentsAdapter(Context context, List<Document> documents) {
    this.mContext  = context;
    this.documents = documents;
  }

  @Override
  public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.swipe_layout, parent, false);
    return new SimpleViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final SimpleViewHolder viewHolder, final int position) {
    final Document item = documents.get(position);

    viewHolder.title.setText(item.getTitle() );
    viewHolder.from.setText( item.getSigner().getOrganisation());
    viewHolder.date.setText( item.getExternalDocumentNumber() + " от " + item.getRegistrationDate());



    viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);

    viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Left,  viewHolder.swipeLayout.findViewById(R.id.from_left_to_right));
    viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, viewHolder.swipeLayout.findViewById(R.id.from_right_to_left));


    viewHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
      @Override
      public void onClose(SwipeLayout layout) {
      }

      @Override
      public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
      }

      @Override
      public void onStartOpen(SwipeLayout layout) {

      }

      @Override
      public void onOpen(SwipeLayout layout) {
      }

      @Override
      public void onStartClose(SwipeLayout layout) {

      }

      @Override
      public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
      }
    });

    viewHolder.cv.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        RxSharedPreferences rxPreferences = RxSharedPreferences.create(preferences);
        Preference<Integer> rxPosition = rxPreferences.getInteger("position");
        rxPosition.set(position);

        Intent intent = new Intent(mContext, InfoActivity.class);
        mContext.startActivity(intent);

        Toast.makeText(mContext, " onClick : " + item.getMd5() + " \n" + item.getTitle(), Toast.LENGTH_SHORT).show();
      }
    });


    viewHolder.btnLocation.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        Toast.makeText(v.getContext(), "Просто так " + viewHolder.title.getText().toString(), Toast.LENGTH_SHORT).show();
      }
    });


    viewHolder.to_contol.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        Toast.makeText(view.getContext(), "Контроль " + viewHolder.title.getText().toString(), Toast.LENGTH_SHORT).show();
      }
    });

    viewHolder.to_favorites.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mItemManger.removeShownLayouts(viewHolder.swipeLayout);
        documents.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, documents.size());
        mItemManger.closeAllItems();
        Toast.makeText(view.getContext(), "Удалён " + viewHolder.title.getText().toString(), Toast.LENGTH_SHORT).show();
      }
    });


    // mItemManger is member in RecyclerSwipeAdapter Class
    mItemManger.bindView(viewHolder.itemView, position);

  }

  @Override
  public int getItemCount() {
    return documents.size();
  }

  @Override
  public int getSwipeLayoutResourceId(int position) {
    return R.id.swipe;
  }

  public Document getItem(int position) {
    return this.documents.get(position);
  }


  //  ViewHolder Class

  public static class SimpleViewHolder extends RecyclerView.ViewHolder {
    private TextView control_label;
    private TextView favorite_label;
    SwipeLayout swipeLayout;
    TextView to_favorites;
    TextView tvEdit;
    TextView to_contol;
    ImageButton btnLocation;

    CardView cv;
    TextView title;
    TextView date;
    TextView from;

    public SimpleViewHolder(View itemView) {
      super(itemView);
      swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);

      to_contol = (TextView) itemView.findViewById(R.id.card_to_favorites);
      to_favorites = (TextView) itemView.findViewById(R.id.card_delete);
      btnLocation = (ImageButton) itemView.findViewById(R.id.btnLocation);

      cv    = (CardView)itemView.findViewById(R.id.cv);
      title = (TextView)itemView.findViewById(R.id._title);
      from  = (TextView)itemView.findViewById(R.id.from);
      date   = (TextView)itemView.findViewById(R.id.date);
      favorite_label   = (TextView)itemView.findViewById(R.id.favorite_label);
      control_label   = (TextView)itemView.findViewById(R.id.control_label);



    }
  }
}
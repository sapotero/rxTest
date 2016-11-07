package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.google.gson.Gson;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import rx.functions.Action1;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.jobs.bus.UpdateDocumentJob;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.views.activities.InfoActivity;

public class DocumentsAdapter extends RecyclerSwipeAdapter<DocumentsAdapter.SimpleViewHolder> implements Action1<List<Document>> {

  @Inject RxSharedPreferences settings;
  @Inject JobManager jobManager;

  private Context mContext;
  private List<Document> documents;
  private Oshs current_user;
  private View emptyView;

  public DocumentsAdapter(Context context, List<Document> documents) {
    this.mContext  = context;
    this.documents = documents;
    EsdApplication.getComponent(context).inject(this);

    Preference<String> user = settings.getString("current_user");
    current_user = new Gson().fromJson( user.get(), Oshs.class );

  }

  @Override
  public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.documents_adapter_item_layout, parent, false);
    return new SimpleViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final SimpleViewHolder viewHolder, final int position) {
    final Document item = documents.get(position);

    viewHolder.title.setText(item.getTitle() );
    viewHolder.from.setText( item.getSigner().getOrganisation());
    viewHolder.date.setText( item.getExternalDocumentNumber() + " от " + item.getRegistrationDate());

    viewHolder.date.setText( item.getExternalDocumentNumber() + " от " + item.getRegistrationDate());

    if ( item.getUrgency() != null ){
      viewHolder.badge.setText( item.getUrgency() );
    } else {
      viewHolder.badge.setVisibility(View.INVISIBLE);
    }

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

    viewHolder.cv.setOnClickListener(view -> {

      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
      RxSharedPreferences rxPreferences = RxSharedPreferences.create(preferences);
      Preference<Integer> rxPosition = rxPreferences.getInteger("position");
      rxPosition.set(position);

      Intent intent = new Intent(mContext, InfoActivity.class);
      mContext.startActivity(intent);
      Toast.makeText(mContext, " onClick : " + item.getMd5() + " \n" + item.getTitle(), Toast.LENGTH_SHORT).show();
      viewHolder.swipeLayout.close(true);
    });

    viewHolder.to_contol.setOnClickListener(view -> {
      jobManager.addJobInBackground( new UpdateDocumentJob( item.getUid(), "favorites", true ) );

      Toast.makeText(view.getContext(), "Избранное " + viewHolder.title.getText().toString(), Toast.LENGTH_SHORT).show();
      viewHolder.swipeLayout.close(true);
    });

    viewHolder.to_favorites.setOnClickListener(view -> {

      jobManager.addJobInBackground( new UpdateDocumentJob( item.getUid(), "control", true ) );

      Toast.makeText(view.getContext(), "Контроль " + viewHolder.title.getText().toString(), Toast.LENGTH_SHORT).show();
      viewHolder.swipeLayout.close(true);
    });


    // mItemManger is member in RecyclerSwipeAdapter Class
    mItemManger.bindView(viewHolder.itemView, position);

  }

  @Override
  public int getItemCount() {
    return documents == null ? 0 : documents.size();
  }

  @Override
  public int getSwipeLayoutResourceId(int position) {
    return R.id.swipe;
  }

  public Document getItem(int position) {
    return this.documents.get(position);
  }

  @Override
  public void call(List<Document> documents) {
    this.documents = documents;
    notifyDataSetChanged();
  }

  public void addItem(Document document) {
    documents.add(document);
    notifyDataSetChanged();
  }

  public Integer getPositionByUid(String uid) {
    Document document = null;

    Log.d( "getPositionByUid", " UpdateDocumentJob " + uid );

    int position = -1;

    for (Document doc: documents) {
      position++;

      if (Objects.equals(doc.getUid(), uid)){
        document = doc;
        break;
      }
    }

    return position;

  }

  //  ViewHolder Class

  public class SimpleViewHolder extends RecyclerView.ViewHolder {
    private ImageButton to_action;
    private TextView badge;
    public TextView control_label;
    public TextView favorite_label;
    private SwipeLayout swipeLayout;
    private TextView to_favorites;
    private TextView to_contol;

    private CardView cv;
    private TextView title;
    private TextView date;
    private TextView from;

    public SimpleViewHolder(View itemView) {
      super(itemView);
      swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);

      to_contol = (TextView) itemView.findViewById(R.id.swipe_layout_card_to_control);
      to_favorites = (TextView) itemView.findViewById(R.id.swipe_layout_card_to_favorites);
      to_action = (ImageButton) itemView.findViewById(R.id.swipe_layout_card_to_action);

      cv    = (CardView)itemView.findViewById(R.id.swipe_layout_cv);
      title = (TextView)itemView.findViewById(R.id.swipe_layout_title);
      badge = (TextView)itemView.findViewById(R.id.swipe_layout_urgency_badge);
      from  = (TextView)itemView.findViewById(R.id.swipe_layout_from);
      date   = (TextView)itemView.findViewById(R.id.swipe_layout_date);
      favorite_label   = (TextView)itemView.findViewById(R.id.favorite_label);
      control_label   = (TextView)itemView.findViewById(R.id.control_label);

      favorite_label.setVisibility(View.GONE);
      control_label.setVisibility(View.GONE);
    }

    public void setControl() {
      control_label.setVisibility(View.VISIBLE);
    }
  }
}
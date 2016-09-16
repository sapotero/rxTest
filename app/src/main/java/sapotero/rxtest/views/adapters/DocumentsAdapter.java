package sapotero.rxtest.views.adapters;

import android.content.Context;
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

import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.models.documents.Document;
import sapotero.rxtest.views.activities.MainActivity;

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
    viewHolder.md5.setText(  item.getMd5());
    viewHolder.date.setText( item.getRegistrationDate());

    viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);

    // Drag From Left
    viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Left, viewHolder.swipeLayout.findViewById(R.id.bottom_wrapper1));
    // Drag From Right
    viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, viewHolder.swipeLayout.findViewById(R.id.bottom_wrapper));


    // Handling different events when swiping
    viewHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
      @Override
      public void onClose(SwipeLayout layout) {
        //when the SurfaceView totally cover the BottomView.
      }

      @Override
      public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
        //you are swiping.
      }

      @Override
      public void onStartOpen(SwipeLayout layout) {

      }

      @Override
      public void onOpen(SwipeLayout layout) {
        //when the BottomView totally show.
      }

      @Override
      public void onStartClose(SwipeLayout layout) {

      }

      @Override
      public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
        //when user's hand released.
      }
    });

        /*viewHolder.swipeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if ((((SwipeLayout) v).getOpenStatus() == SwipeLayout.Status.Close)) {
                    //Start your activity

                    Toast.makeText(mContext, " onClick : " + item.getName() + " \n" + item.getEmailId(), Toast.LENGTH_SHORT).show();
                }

            }
        });*/

    viewHolder.cv.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        MainActivity.showDocumentInfo(view, position);
        Toast.makeText(mContext, " onClick : " + item.getMd5() + " \n" + item.getTitle(), Toast.LENGTH_SHORT).show();
      }
    });


    viewHolder.btnLocation.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        Toast.makeText(v.getContext(), "Просто так " + viewHolder.title.getText().toString(), Toast.LENGTH_SHORT).show();
      }
    });


    viewHolder.tvShare.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        Toast.makeText(view.getContext(), "Контроль " + viewHolder.title.getText().toString(), Toast.LENGTH_SHORT).show();
      }
    });

    viewHolder.tvEdit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        Toast.makeText(view.getContext(), "Быстрый ответ " + viewHolder.title.getText().toString(), Toast.LENGTH_SHORT).show();
      }
    });


    viewHolder.tvDelete.setOnClickListener(new View.OnClickListener() {
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
    SwipeLayout swipeLayout;
    TextView tvDelete;
    TextView tvEdit;
    TextView tvShare;
    ImageButton btnLocation;

    CardView cv;
    TextView title;
    TextView date;
    TextView from;
    TextView md5;

    public SimpleViewHolder(View itemView) {
      super(itemView);
      swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);

      tvDelete = (TextView) itemView.findViewById(R.id.tvDelete);
      tvEdit = (TextView) itemView.findViewById(R.id.tvEdit);
      tvShare = (TextView) itemView.findViewById(R.id.tvShare);
      btnLocation = (ImageButton) itemView.findViewById(R.id.btnLocation);

      cv    = (CardView)itemView.findViewById(R.id.cv);
      title = (TextView)itemView.findViewById(R.id.title);
      from  = (TextView)itemView.findViewById(R.id.from);
      md5   = (TextView)itemView.findViewById(R.id.md5);
      date   = (TextView)itemView.findViewById(R.id.date);


    }
  }
}
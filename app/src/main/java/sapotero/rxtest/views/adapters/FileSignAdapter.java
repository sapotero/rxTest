package sapotero.rxtest.views.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.db.requery.models.queue.FileSignEntity;

public class FileSignAdapter extends RecyclerView.Adapter<FileSignAdapter.FileSignViewHolder> {

  private Context mContext;
  private List<FileSignEntity> queueEntities;

  private String TAG = this.getClass().getSimpleName();


  public FileSignAdapter(Context context, List<FileSignEntity> queueEntities) {
    this.mContext  = context;
    this.queueEntities = queueEntities;
    
  }

 
  @Override
  public FileSignViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from( parent.getContext() ).inflate(R.layout.file_sign_adapter_item_layout, parent, false);
    return new FileSignViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final FileSignViewHolder viewHolder, final int position) {
    final FileSignEntity item = queueEntities.get(position);

    viewHolder.title.setText( item.getFilename() );
    viewHolder.uids.setText( String.format("img: %s\ndoc: %s", item.getImageId(), item.getDocumentId()) );
    viewHolder.sign.setText( item.getSign() );

    viewHolder.button.setOnClickListener(view -> {
      ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
      ClipData clip =
        ClipData.newPlainText(
          "sign",
          String.format("document_id: %s\nimage_id: %s\n\n%s",
            item.getDocumentId(),
            item.getImageId(),
            item.getSign()
          )
        );
      clipboard.setPrimaryClip(clip);

      Toast.makeText(mContext, "Подпись скопирована", Toast.LENGTH_SHORT).show();
    });
  }


  @Override
  public int getItemCount() {
    return queueEntities == null ? 0 : queueEntities.size();
  }


  public FileSignEntity getItem(int position) {
    if ( queueEntities.size() == 0 ){
      return null;
    }

    if ( queueEntities.size() < position ){
      return null;
    }

    return queueEntities.get(position);
  }

  public void clear(){
    queueEntities.clear();
    notifyDataSetChanged();
  }

  public void add(List<FileSignEntity> tasks) {
    queueEntities = tasks;
    notifyDataSetChanged();
  }

  class FileSignViewHolder extends RecyclerView.ViewHolder {
    private CardView cv;
    private TextView title;
    private TextView uids;
    private TextView sign;
    private Button   button;

    FileSignViewHolder(View itemView) {
      super(itemView);
      cv     = (CardView) itemView.findViewById(R.id.file_sign_layout_cv);
      title  = (TextView) itemView.findViewById(R.id.file_sign_filename);
      uids   = (TextView) itemView.findViewById(R.id.file_sign_uids);
      sign   = (TextView) itemView.findViewById(R.id.file_sign_sign);
      button = (Button)   itemView.findViewById(R.id.file_sign_button_copy);
    }
  }

}
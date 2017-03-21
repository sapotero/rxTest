package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.db.requery.models.queue.QueueEntity;
import sapotero.rxtest.views.managers.menu.factories.CommandFactory;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.QueueViewHolder> {

  private Context mContext;
  private List<QueueEntity> queueEntities;

  private String TAG = this.getClass().getSimpleName();


  public LogAdapter(Context context, List<QueueEntity> queueEntities) {
    this.mContext  = context;
    this.queueEntities = queueEntities;
    
  }

 
  @Override
  public QueueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from( parent.getContext() ).inflate(R.layout.log_adapter_item_layout, parent, false);
    return new QueueViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final QueueViewHolder viewHolder, final int position) {
    final QueueEntity item = queueEntities.get(position);

    CommandFactory.Operation operation = CommandFactory.Operation.getOperation(item.getCommand());

    if (operation != null) {
      viewHolder.date.setText( item.getCreatedAt() );
      viewHolder.title.setText( operation.getRussinaName() );

      String status = "";

      if (item.isLocal() && item.isRemote()){
        status = "Выполнено";
      }
      if (item.isRunning()){
        status = "В работе";
      }
      if (item.isWithError()){
        status = "Ошибка";
      }

      if (!item.isRemote()){
        status = "В очереди";
      }

      viewHolder.jobStatus.setText( status );

//      viewHolder.operations.setChecked( item.isLocal() );

      viewHolder.cv.setOnClickListener(view -> {
        Toast.makeText(mContext, " onClick : " + item.getUuid(), Toast.LENGTH_SHORT).show();
      });
    }

  }


  @Override
  public int getItemCount() {
    return queueEntities == null ? 0 : queueEntities.size();
  }


  public QueueEntity getItem(int position) {
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

  class QueueViewHolder extends RecyclerView.ViewHolder {
    private TextView date;
    private CardView cv;
    private TextView title;
//    private CheckBox operations;
    private TextView jobStatus;

    public QueueViewHolder(View itemView) {
      super(itemView);
      cv     = (CardView)itemView.findViewById(R.id.log_layout_cv);
      date   = (TextView)itemView.findViewById(R.id.log_operation_date);
      title  = (TextView)itemView.findViewById(R.id.log_operation_name);
      jobStatus  = (TextView)itemView.findViewById(R.id.log_job_status);
//      operations = (CheckBox)itemView.findViewById(R.id.log_operations);
    }
  }

}
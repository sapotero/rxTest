package sapotero.rxtest.views.adapters;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.functions.Action1;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RFolderEntity;
import sapotero.rxtest.jobs.bus.UpdateDocumentJob;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.views.activities.InfoActivity;
import sapotero.rxtest.views.dialogs.InfoCardDialogFragment;
import sapotero.rxtest.views.managers.db.managers.DBDocumentManager;
import sapotero.rxtest.views.managers.menu.OperationManager;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class DocumentsAdapter extends RecyclerSwipeAdapter<DocumentsAdapter.SimpleViewHolder> implements Action1<List<Document>> {

  @Inject RxSharedPreferences settings;
  @Inject JobManager jobManager;
//  @Inject DocumentManager documentManager;
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject DBDocumentManager manager;

  private final OperationManager operationManager;

  private Context mContext;
  private List<Document> documents;
  private Oshs current_user;
  private View emptyView;

  public DocumentsAdapter(Context context, List<Document> documents) {
    this.mContext  = context;
    this.documents = documents;

    EsdApplication.getComponent(context).inject(this);

//    documentManager = new InterfaceDocumentManager().getInstance(mContext);

    operationManager = OperationManager.getInstance();
  }

  @Override
  public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.documents_adapter_item_layout, parent, false);
    return new SimpleViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final SimpleViewHolder viewHolder, final int position) {
    final Document item = documents.get(position);

    viewHolder.title.setText(item.getShortDescription() );
    viewHolder.from.setText( item.getOrganization() );

    String number = item.getExternalDocumentNumber();

//    if (number == null){
//      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
//      RxSharedPreferences rxPreferences = RxSharedPreferences.create(preferences);
//      Preference<String> uid = rxPreferences.getString("main_menu.uid");
//      number = Fields.getJournalByUid( uid.get() ).getSingle();
//    }
    viewHolder.date.setText( number + " от " + item.getRegistrationDate());

    viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);

    viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Left,  viewHolder.swipeLayout.findViewById(R.id.from_left_to_right));
    viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, viewHolder.swipeLayout.findViewById(R.id.from_right_to_left));

    if (item.getSigner() != null){
      Timber.d( "item.getSigner() %s - %s", item.getSigner().getId(), item.getSigner().getOrganisation() );
    }


    if (item.getChanged() != null){
      viewHolder.wait_for_sync.setVisibility(  item.getChanged() ? View.VISIBLE : View.GONE );
    }

    if ( item.getControl() != null && item.getControl() ){
      Timber.d( "item.getControl() %s",  item.getControl().toString() );
      viewHolder.control_label.setVisibility(View.VISIBLE);
    } else {
      viewHolder.control_label.setVisibility(View.GONE);
    }
    if ( item.getFavorites() != null && item.getFavorites() ){
      Timber.d( "item.getFavorites() %s", item.getFavorites().toString() );
      viewHolder.favorite_label.setVisibility(View.VISIBLE);
    } else {
      viewHolder.favorite_label.setVisibility(View.GONE);
    }

//    if ( item.get != null && item.getFavorites() ){
//      Timber.d( "item.getFavorites() " + item.getFavorites().toString() );
//      viewHolder.favorite_label.setVisibility(View.VISIBLE);
//    }


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

      Preference<String> rxUid = rxPreferences.getString("main_menu.uid");
      rxUid.set( item.getUid() );

      Preference<String> rxReg = rxPreferences.getString("main_menu.regnumber");
      rxReg.set( item.getRegistrationNumber() );

      Preference<String> rxStatus = rxPreferences.getString("main_menu.start");
      rxStatus.set( item.getStatusCode() );

      Preference<String> rxDate = rxPreferences.getString("main_menu.date");
      rxDate.set( item.getRegistrationDate() );

      Intent intent = new Intent(mContext, InfoActivity.class);
      mContext.startActivity(intent);
      Toast.makeText(mContext, " onClick : " + item.getMd5() + " \n" + item.getTitle(), Toast.LENGTH_SHORT).show();
      viewHolder.swipeLayout.close(true);
    });

    viewHolder.cv.setOnLongClickListener(view -> {


//      documentManager.get( item.getUid() ).toJson();


//      String _title = documentManager.getDocument(item.getUid()).getTitle();
      String _title = manager.get(item.getUid()).getTitle();
      Timber.e("title : %s", _title);

      Notification builder =
        new NotificationCompat.Builder(mContext)
          .setSmallIcon( R.drawable.gerb )
          .setContentTitle("Уведомление")
          .setContentText("Добавлена резолюция к документу " + item.getUid() )
          .setDefaults(Notification.DEFAULT_ALL)
          .setCategory(Notification.CATEGORY_MESSAGE)
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .addAction( 1 , "Утвердить", null)
          .addAction( 0 ,  "Отклонить", null)
          .build();

      NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.notify(0, builder);

      return true;
    });


    viewHolder.to_contol.setOnClickListener(view -> {
      jobManager.addJobInBackground( new UpdateDocumentJob( item.getUid(), "favorites", true ) );
//      jobManager.addJobInBackground( new MarkDocumentAsChangedJob( item.getUid() ) );

      String favorites = dataStore
        .select(RFolderEntity.class)
        .where(RFolderEntity.TYPE.eq("favorites"))
        .get().first().getUid();

      CommandParams params = new CommandParams();
      params.setFolder(favorites);
      params.setDocument( item.getUid() );

      operationManager.execute( "menu_info_shared_to_favorites", params );

      Toast.makeText(view.getContext(), "Избранное " + viewHolder.title.getText().toString(), Toast.LENGTH_SHORT).show();
      viewHolder.swipeLayout.close(true);
    });

    viewHolder.to_favorites.setOnClickListener(view -> {

      jobManager.addJobInBackground( new UpdateDocumentJob( item.getUid(), "control", true ) );
//      jobManager.addJobInBackground( new MarkDocumentAsChangedJob( item.getUid() ) );


      CommandParams params = new CommandParams();
      params.setSign( item.getUid() );

      operationManager.execute( "menu_info_shared_to_control", params );

      Toast.makeText(view.getContext(), "Контроль " + viewHolder.title.getText().toString(), Toast.LENGTH_SHORT).show();
      viewHolder.swipeLayout.close(true);
    });

    viewHolder.get_infocard.setOnClickListener(view -> {
//      manager.get( item.getUid() ).toJson();

      FragmentManager manager = ((Activity) mContext).getFragmentManager();

      new InfoCardDialogFragment().withUid( item.getUid() ).show( manager, "InfoCardDialogFragment" );

      viewHolder.swipeLayout.close(true);
    });

    viewHolder.get_files.setOnClickListener(view -> {
//      manager.get( item.getUid() ).toJson();


      String _title = manager.get(item.getUid()).getTitle();
      Timber.e("title : %s", _title);

      Notification builder =
        new NotificationCompat.Builder(mContext)
          .setSmallIcon( R.drawable.gerb )
          .setContentTitle("Уведомление FILES")
          .setContentText("Добавлена резолюция к документу " + item.getRegistrationNumber())
          .setDefaults(Notification.DEFAULT_ALL)
          .setCategory(Notification.CATEGORY_MESSAGE)
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .addAction( 1 , "Утвердить", null)
          .addAction( 0 ,  "Отклонить", null)
          .build();

      NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.notify(0, builder);

      viewHolder.swipeLayout.close(true);
    });

    viewHolder.get_editor.setOnClickListener(view -> {
//      documentManager.get( item.getUid() ).toJson();


      String _title = manager.get(item.getUid()).getTitle();
      Timber.e("title : %s", _title);

      Notification builder =
        new NotificationCompat.Builder(mContext)
          .setSmallIcon( R.drawable.gerb )
          .setContentTitle("Уведомление EDITOR")
          .setContentText("Добавлена резолюция к документу " + item.getRegistrationNumber())
          .setDefaults(Notification.DEFAULT_ALL)
          .setCategory(Notification.CATEGORY_MESSAGE)
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .addAction( 1 , "Утвердить", null)
          .addAction( 0 ,  "Отклонить", null)
          .build();

      NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
      notificationManager.notify(0, builder);

      viewHolder.swipeLayout.close(true);
    });




    if ( item.getUrgency() != null ){
      viewHolder.badge.setVisibility(View.VISIBLE);
      viewHolder.badge.setText( item.getUrgency() );
    } else {
      viewHolder.badge.setVisibility(View.GONE);
    }
    Timber.tag("view " + item.getRegistrationNumber() + " " + item.getTitle());


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
//    notifyItemInserted(documents.size());
  }

  public void setDocuments(ArrayList<Document> docs) {
    documents = docs;
    notifyDataSetChanged();
//    notifyItemInserted(documents.size());
  }

  public void clear(){
    documents.clear();
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
    private Button get_infocard;
    private Button get_files;
    private Button get_editor;

    private TextView badge;
    private TextView control_label;
    private TextView favorite_label;
    private SwipeLayout swipeLayout;
    private TextView to_favorites;
    private TextView to_contol;

    private TextView wait_for_sync;

    private CardView cv;
    private TextView title;
    private TextView date;
    private TextView from;

    public SimpleViewHolder(View itemView) {
      super(itemView);
      swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);

      to_contol = (TextView) itemView.findViewById(R.id.swipe_layout_card_to_control);
      to_favorites = (TextView) itemView.findViewById(R.id.swipe_layout_card_to_favorites);

      get_infocard = (Button) itemView.findViewById(R.id.swipe_layout_card_get_infocard);
      get_files    = (Button) itemView.findViewById(R.id.swipe_layout_card_get_files);
      get_editor   = (Button) itemView.findViewById(R.id.swipe_layout_card_get_editor);


      cv    = (CardView)itemView.findViewById(R.id.swipe_layout_cv);
      title = (TextView)itemView.findViewById(R.id.swipe_layout_title);
      badge = (TextView)itemView.findViewById(R.id.swipe_layout_urgency_badge);
      from  = (TextView)itemView.findViewById(R.id.swipe_layout_from);
      date   = (TextView)itemView.findViewById(R.id.swipe_layout_date);
      favorite_label   = (TextView)itemView.findViewById(R.id.favorite_label);
      control_label   = (TextView)itemView.findViewById(R.id.control_label);
      wait_for_sync   = (TextView)itemView.findViewById(R.id.wait_for_sync);

      favorite_label.setVisibility(View.GONE);
      control_label.setVisibility(View.GONE);
    }

    public void setControl() {
      control_label.setVisibility(View.VISIBLE);
    }
  }
}
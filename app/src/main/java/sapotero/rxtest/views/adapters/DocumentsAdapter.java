package sapotero.rxtest.views.adapters;

import android.annotation.SuppressLint;
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
import android.widget.TextView;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.Observable;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RLinks;
import sapotero.rxtest.db.requery.models.RLinksEntity;
import sapotero.rxtest.db.requery.models.RSignerEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.rx.UpdateCountEvent;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.views.activities.InfoActivity;
import sapotero.rxtest.views.managers.db.managers.DBDocumentManager;
import sapotero.rxtest.views.managers.menu.OperationManager;
import sapotero.rxtest.views.menu.MenuBuilder;
import timber.log.Timber;

public class DocumentsAdapter extends RecyclerView.Adapter<DocumentsAdapter.SimpleViewHolder> implements Action1<List<Document>> {

  @Inject RxSharedPreferences settings;
  @Inject JobManager jobManager;
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject DBDocumentManager manager;
  @Inject OperationManager operationManager;

  private Context mContext;
  private List<RDocumentEntity> documents;
  private ObservableDocumentList real_docs;
  private MenuBuilder mainMenu;
  private String TAG = this.getClass().getSimpleName();

  @Override
  public void call(List<Document> documents) {

  }

  public void hideItem(String uid) {
    Timber.tag(TAG).v("hideItem: %s", uid);


    Timber.tag(TAG).v("total documents: %s", documents.size());

    int index = -1;
    for (int i = 0; i <documents.size()-1 ; i++) {
      RDocumentEntity doc = documents.get(i);
      Timber.tag(TAG).v("test: %s | %s", doc.getUid(), uid);

      if ( Objects.equals(doc.getUid(), uid) ){
        index = i;
        break;
      }
    }
    Timber.tag(TAG).v("index: %s", index);

    if ( Holder.MAP.containsKey( uid ) ){
      Holder.MAP.remove( uid );
      Timber.tag(TAG).v("has item");
//      if (index != -1){
//        Timber.tag(TAG).v("removed");
//
//        RDocumentEntity document = Holder.MAP.get(uid);
//        documents.remove(document);
//        real_docs.add(document);
//        notifyItemRemoved(index);
//      }
      if ( documents.get(index).isProcessed() ){
        documents.remove(index);
      }
    }

    Timber.tag(TAG).v("total documents: %s", documents.size());


    notifyDataSetChanged();

  }

  private static class ObservableDocumentList<T> {

    protected final List<T> list;
    protected final PublishSubject<T> onAdd;

    public ObservableDocumentList() {
      this.list = new ArrayList<T>();
      this.onAdd = PublishSubject.create();
    }
    public void add(T value) {
      list.add(value);
      onAdd.onNext(value);
    }
    public void clear() {
      list.clear();
    }

    public Observable<T> getObservable() {
      return onAdd;
    }
  }

  @SuppressLint("NewApi")
  public DocumentsAdapter(Context context, List<RDocumentEntity> documents) {
    this.mContext  = context;
    this.documents = documents;

    real_docs = new ObservableDocumentList();

    populateDocs(documents);

    real_docs.getObservable()
      .debounce(1000, TimeUnit.MILLISECONDS)
      .subscribe( data -> {
        Timber.e("FROM UPDATE STREAM");
        if (mainMenu != null) {
          EventBus.getDefault().postSticky(new UpdateCountEvent());
        }
      });

    EsdApplication.getComponent(context).inject(this);
  }

  private void populateDocs(List<RDocumentEntity> documents) {
    for (int i = 0; i < documents.size(); i++) {
      real_docs.add(documents.get(i));
    }
  }

  @Override
  public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.documents_adapter_item_layout, parent, false);
    return new SimpleViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final SimpleViewHolder viewHolder, final int position) {
    final RDocumentEntity item = documents.get(position);

    viewHolder.title.setText( item.getShortDescription() );

    //resolved https://tasks.n-core.ru/browse/MVDESD-12625
    //  На плитке Обращения и НПА не показывать строку "Без организации", если её действительно нет(
//    Timber.d("start with: %s %s", item.getUid().startsWith( Fields.Journal.INCOMING_ORDERS.getValue() ), item.getUid().startsWith( Fields.Journal.CITIZEN_REQUESTS.getValue() ));
    if(
          item.getUid().startsWith( Fields.Journal.INCOMING_ORDERS.getValue() )
      ||  item.getUid().startsWith( Fields.Journal.CITIZEN_REQUESTS.getValue() )

      ){

      if ( item.getOrganization().toLowerCase().contains("без организации") ){
        Timber.d("empty organization" );
        viewHolder.from.setText("");
      } else {
        Timber.e( "SIGNER %s", item.getSigner() );
        if ( item.getSigner() != null ){
          RSignerEntity signer = (RSignerEntity) item.getSigner();
          viewHolder.from.setText( signer.getOrganisation() );
        }
      }

    } else {
      viewHolder.from.setText( item.getOrganization() );
    }

    String number = item.getExternalDocumentNumber();

    if (number == null){
      number = item.getRegistrationNumber();
    }

    viewHolder.date.setText( item.getTitle() );

//    Timber.tag("Status LINKS").e("filter: %s | %s | %s", item.getFilter(), Fields.Status.SIGNING.getValue(), Fields.Status.APPROVAL.getValue() );
    if( Objects.equals(item.getFilter(), Fields.Status.SIGNING.getValue()) ||  Objects.equals(item.getFilter(), Fields.Status.APPROVAL.getValue()) ){

      Timber.tag("Status LINKS").e("size: %s", item.getLinks().size() );

      if ( item.getLinks().size() >= 1){

        try {
          Set<RLinks> links = item.getLinks();

          ArrayList<RLinks> arrayList = new ArrayList<RLinks>();
          for (RLinks str : links) {
            arrayList.add(str);
          }

          RLinksEntity _link = (RLinksEntity) arrayList.get(0);
          Timber.tag("Status LINKS").e("size > 0 | first: %s", _link.getUid() );

          RDocumentEntity doc = dataStore
            .select(RDocumentEntity.class)
            .where(RDocumentEntity.UID.eq( _link.getUid() ))
            .get().first();

          viewHolder.date.setText( item.getTitle() + " на " + doc.getRegistrationNumber() );
        } catch (NoSuchElementException e) {
          e.printStackTrace();
        }
      }

    }

//    viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
//
//    viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Left,  viewHolder.swipeLayout.findViewById(R.id.from_left_to_right));
//    viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, viewHolder.swipeLayout.findViewById(R.id.from_right_to_left));

    // FIX добавить отображение ОжидаетСинхронизации
    // FIX отображать срочность поверх всего во фрагменте
    if (item.isChanged() != null){
//      viewHolder.wait_for_sync.setVisibility(  item.getChanged() ? View.VISIBLE : View.GONE );
    }

    if ( item.isControl() != null && item.isControl() ){
      Timber.d( "item.getControl() %s",  item.isControl().toString() );
      viewHolder.control_label.setVisibility(View.VISIBLE);
    } else {
      viewHolder.control_label.setVisibility(View.GONE);
    }
    if ( item.isFavorites() != null && item.isFavorites() ){
      Timber.d( "item.getFavorites() %s", item.isFavorites().toString() );
      viewHolder.favorite_label.setVisibility(View.VISIBLE);
    } else {
      viewHolder.favorite_label.setVisibility(View.GONE);
    }


    viewHolder.cv.setOnClickListener(view -> {

      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
      RxSharedPreferences rxPreferences = RxSharedPreferences.create(preferences);
      Preference<Integer> rxPosition = rxPreferences.getInteger("position");
      rxPosition.set(position);

      Preference<String> rxUid = rxPreferences.getString("activity_main_menu.uid");
      rxUid.set( item.getUid() );

      Preference<String> rxReg = rxPreferences.getString("activity_main_menu.regnumber");
      rxReg.set( item.getRegistrationNumber() );

      Preference<String> rxStatus = rxPreferences.getString("activity_main_menu.start");
      rxStatus.set( item.getFilter() );

      Preference<String> rxDate = rxPreferences.getString("activity_main_menu.date");
      rxDate.set( item.getRegistrationDate() );

      Intent intent = new Intent(mContext, InfoActivity.class);
      mContext.startActivity(intent);
      Toast.makeText(mContext, " onClick : " + item.getMd5() + " \n" + item.getTitle(), Toast.LENGTH_SHORT).show();
//      viewHolder.swipeLayout.close(true);
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

    if ( item.getUrgency() != null ){
      viewHolder.badge.setVisibility(View.VISIBLE);
      viewHolder.badge.setText( item.getUrgency() );
    } else {
      viewHolder.badge.setVisibility(View.GONE);
    }

  }

  @Override
  public int getItemCount() {
    return documents == null ? 0 : documents.size();
  }


  public RDocumentEntity getItem(int position) {
    return this.documents.get(position);
  }


  public Integer getPositionByUid(String uid) {
    RDocumentEntity document = null;

    Log.d( "getPositionByUid", " UpdateDocumentJob " + uid );

    int position = -1;

    for (RDocumentEntity doc: documents) {
      position++;

      if (Objects.equals(doc.getUid(), uid)){
        document = doc;
        break;
      }
    }

    return position;

  }

  public void clear(){
    notifyItemRangeRemoved(0, documents.size());
    Holder.MAP.clear();
    documents.clear();
    real_docs.clear();
    notifyDataSetChanged();
  }

  public void addItem(RDocumentEntity document) {
    if ( !Holder.MAP.containsKey( document.getUid()) ){
      Holder.MAP.put( document.getUid(), document );
      documents.add(document);
      real_docs.add(document);
      notifyItemInserted(documents.size());
    }
  }

  public void setDocuments(List<RDocumentEntity> list_dosc, RecyclerView recyclerView) {
    Timber.e("setDocuments %s", list_dosc.size());
    documents = list_dosc;
    notifyDataSetChanged();

  }

  //  ViewHolder Class

  class SimpleViewHolder extends RecyclerView.ViewHolder {
    private TextView badge;
    private TextView control_label;
    private TextView favorite_label;

    private TextView wait_for_sync;

    private CardView cv;
    private TextView title;
    private TextView date;
    private TextView from;

    public SimpleViewHolder(View itemView) {
      super(itemView);


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

    public void showControl() {
      control_label.setVisibility(View.VISIBLE);
    }
    public void hideControl() {
      control_label.setVisibility(View.GONE);
    }
  }

  private static class Holder {
    static Map<String, RDocumentEntity> MAP = new HashMap<>();
  }
}
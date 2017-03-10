package sapotero.rxtest.views.adapters;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import sapotero.rxtest.db.requery.models.decisions.RDecision;
import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.rx.UpdateCountEvent;
import sapotero.rxtest.events.utils.NoDocumentsEvent;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.views.activities.InfoActivity;
import sapotero.rxtest.views.activities.MainActivity;
import sapotero.rxtest.views.managers.db.managers.DBDocumentManager;
import sapotero.rxtest.views.managers.menu.OperationManager;
import timber.log.Timber;

public class DocumentsAdapter extends RecyclerView.Adapter<DocumentsAdapter.DocumentViewHolder> implements Action1<List<Document>> {

  @Inject RxSharedPreferences settings;
  @Inject JobManager jobManager;
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject DBDocumentManager manager;
  @Inject OperationManager operationManager;

  private Context mContext;
  private List<RDocumentEntity> documents;
  private ObservableDocumentList real_docs;

  private String TAG = this.getClass().getSimpleName();

  private int lastPosition = -1;


  @Override
  public void call(List<Document> documents) {

  }

  public void hideItem(String uid) {
    Timber.tag(TAG).v("hideItem: %s", uid);


    Timber.tag(TAG).v("total documents: %s", documents.size());

    int index = -1;

    for (int i = 0; i <documents.size() ; i++) {
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

      if (index != -1){
        Timber.tag(TAG).v("removed");

        documents.remove(index);
        notifyDataSetChanged();
      }

//      if ( documents.get(index).isProcessed() ){
//        documents.remove(index);
//        notifyItemRemoved(index);
//      } else {
//        notifyDataSetChanged();
//      }

    }

    Timber.tag(TAG).v("total documents: %s", documents.size());




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
        EventBus.getDefault().post(new UpdateCountEvent());
      });

    EsdApplication.getComponent(context).inject(this);
  }

  private void populateDocs(List<RDocumentEntity> documents) {
    for (int i = 0; i < documents.size(); i++) {
      real_docs.add(documents.get(i));
    }
  }

  @Override
  public DocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.documents_adapter_item_layout, parent, false);
    return new DocumentViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final DocumentViewHolder viewHolder, final int position) {
    final RDocumentEntity item = documents.get(position);

    viewHolder.title.setText( item.getShortDescription() );
    viewHolder.subtitle.setText( item.getComment() );

    //resolved https://tasks.n-core.ru/browse/MVDESD-12625
    //  На плитке Обращения и НПА не показывать строку "Без организации", если её действительно нет(
//    Timber.d("star with: %s %s", item.getUid().startsWith( Fields.Journal.INCOMING_ORDERS.getValue() ), item.getUid().startsWith( Fields.Journal.CITIZEN_REQUESTS.getValue() ));
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

    if ( item.isChanged() != null && item.isChanged() ){
      viewHolder.sync_label.setVisibility(View.VISIBLE);
    } else {
      viewHolder.sync_label.setVisibility(View.GONE);
    }

    if ( item.isControl() != null && item.isControl() ){
      viewHolder.control_label.setVisibility(View.VISIBLE);
    } else {
      viewHolder.control_label.setVisibility(View.GONE);
    }

    if ( item.isFavorites() != null && item.isFavorites() ){;
      viewHolder.favorite_label.setVisibility(View.VISIBLE);
    } else {
      viewHolder.favorite_label.setVisibility(View.GONE);
    }


    // если обработаное - то ничего нельзя делать
    if (
      item.isFromFavoritesFolder() != null && item.isFromFavoritesFolder() ||
      item.isFromProcessedFolder() != null && item.isFromProcessedFolder()
      ){
      viewHolder.lock_label.setVisibility(View.VISIBLE);
    } else {
      viewHolder.lock_label.setVisibility(View.GONE);
    }


    viewHolder.cv.setOnClickListener(view -> {

      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
      RxSharedPreferences rxPreferences = RxSharedPreferences.create(preferences);
      Preference<Integer> rxPosition = rxPreferences.getInteger("position");
      rxPosition.set(position);

      settings.getString("activity_main_menu.uid").set( item.getUid() );
      settings.getInteger("activity_main_menu.position").set( viewHolder.getAdapterPosition() );
      settings.getString("activity_main_menu.regnumber").set( item.getRegistrationNumber() );
      settings.getString("activity_main_menu.star").set( item.getFilter() );
      settings.getBoolean("activity_main_menu.from_sign").set( item.isFromSign() );
      settings.getString("activity_main_menu.date").set( item.getRegistrationDate() );

      Intent intent = new Intent(mContext, InfoActivity.class);

      MainActivity activity = (MainActivity) mContext;
      activity.startActivity(intent);
//      activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

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

    // resolved https://tasks.n-core.ru/browse/MVDESD-12625
    //  Номер документа на плитке выделять красным.
    //  Красным документ должен подсвечиваться, только тогда,
    //  когда создаём проект резолюции.
    //  В подписавших резолюцию должен быть Министр.

    if ( item.getDecisions().size() >= 1){
      for (RDecision dec: item.getDecisions()){
        RDecisionEntity decision = (RDecisionEntity) dec;
        if ( decision.isRed() != null && decision.isRed() && !decision.isApproved() ){
//          viewHolder.date.setTextColor( ContextCompat.getColor(mContext, R.color.md_red_600) );
//          viewHolder.title.setTextColor( ContextCompat.getColor(mContext, R.color.md_white_1000 ) );
//          viewHolder.title.setBackgroundColor( ContextCompat.getColor(mContext, R.color.md_red_300 ) );
          viewHolder.cv.setBackground( ContextCompat.getDrawable(mContext, R.drawable.top_border) );
          break;
        }
      }
    } else {
//      viewHolder.cv.setBackground( null );
      viewHolder.cv.setBackground( ContextCompat.getDrawable(mContext, R.color.md_white_1000 ) );
    }



//    setAnimation(viewHolder.itemView, position);

  }

  private void setAnimation(View viewToAnimate, int position){



    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
    viewToAnimate.startAnimation(animation);
  }

  @Override
  public int getItemCount() {
    return documents == null ? 0 : documents.size();
  }


  public RDocumentEntity getItem(int position) {
    if ( documents.size() == 0 ){
      return null;
    }

    if ( documents.size() < position ){
      return null;
    }

    return documents.get(position);
  }

  public void getNextFromPosition(int position) {

    position += 1;

    if ( documents.size() == 0 ){
      Timber.e("noDocuments %s", documents.size());
      EventBus.getDefault().post( new NoDocumentsEvent() );
    } else {

      if (position >= documents.size()) {
        position = 0;
      }

      Timber.tag(TAG).e("position: %s", position);

      RDocumentEntity item = documents.get(position);

      settings.getInteger("activity_main_menu.position").set(position);
      settings.getString("activity_main_menu.uid").set(item.getUid());
      settings.getString("activity_main_menu.regnumber").set(item.getRegistrationNumber());
      settings.getString("activity_main_menu.star").set(item.getFilter());
      settings.getBoolean("activity_main_menu.from_sign").set(item.isFromSign());
      settings.getString("activity_main_menu.date").set(item.getRegistrationDate());
    }

  }

  public void getPrevFromPosition(int position) {
    position -= 1;

    if ( documents.size() == 0 ){
      Timber.e("noDocuments %s", documents.size());
      EventBus.getDefault().post( new NoDocumentsEvent() );
    } else {
      if ( position < 0 ){
        position = documents.size() - 1;
      }
      if ( position == documents.size() ){
        position = 0;
      }

      Timber.tag(TAG).e("position: %s", position);


      RDocumentEntity item = documents.get(position);

      settings.getInteger("activity_main_menu.position").set(position);
      settings.getString("activity_main_menu.uid").set( item.getUid() );
      settings.getString("activity_main_menu.regnumber").set( item.getRegistrationNumber() );
      settings.getString("activity_main_menu.star").set( item.getFilter() );
      settings.getBoolean("activity_main_menu.from_sign").set( item.isFromSign() );
      settings.getString("activity_main_menu.date").set( item.getRegistrationDate() );
    }


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

  class DocumentViewHolder extends RecyclerView.ViewHolder {
    private TextView sync_label;
    private TextView lock_label;
    private TextView subtitle;
    private TextView badge;
    private TextView control_label;
    private TextView favorite_label;

    private TextView wait_for_sync;

    private CardView cv;
    private TextView title;
    private TextView date;
    private TextView from;

    public DocumentViewHolder(View itemView) {
      super(itemView);


      cv    = (CardView)itemView.findViewById(R.id.swipe_layout_cv);
      title = (TextView)itemView.findViewById(R.id.swipe_layout_title);
      subtitle = (TextView)itemView.findViewById(R.id.swipe_layout_subtitle);
      badge = (TextView)itemView.findViewById(R.id.swipe_layout_urgency_badge);
      from  = (TextView)itemView.findViewById(R.id.swipe_layout_from);
      date  = (TextView)itemView.findViewById(R.id.swipe_layout_date);
      favorite_label = (TextView)itemView.findViewById(R.id.favorite_label);
      sync_label = (TextView)itemView.findViewById(R.id.sync_label);
      control_label  = (TextView)itemView.findViewById(R.id.control_label);
      lock_label  = (TextView)itemView.findViewById(R.id.lock_label);
      wait_for_sync  = (TextView)itemView.findViewById(R.id.wait_for_sync);

      favorite_label.setVisibility(View.GONE);
      control_label.setVisibility(View.GONE);
    }

  }

  private static class Holder {
    static Map<String, RDocumentEntity> MAP = new HashMap<>();
  }
}
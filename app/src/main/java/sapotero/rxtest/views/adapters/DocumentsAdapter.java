package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.functions.Action1;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.utils.NoDocumentsEvent;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.views.activities.InfoActivity;
import sapotero.rxtest.views.activities.MainActivity;
import timber.log.Timber;

public class DocumentsAdapter extends RecyclerView.Adapter<DocumentsAdapter.DocumentViewHolder> implements Action1<List<Document>> {

  @Inject Settings settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  //  private List<RDocumentEntity> documents;
  //  private ObservableDocumentList real_docs;
  private List<InMemoryDocument> documents;
  private Context mContext;

  private String TAG = this.getClass().getSimpleName();

  private int lastPosition = -1;


  @Override
  public void call(List<Document> documents) {

  }


  public void removeAllWithRange() {
//    notifyItemRangeRemoved(0, documents.size());
    Holder.MAP.clear();
    documents.clear();
    notifyDataSetChanged();
  }

  public DocumentsAdapter(Context context, List<InMemoryDocument> documents) {
    this.mContext  = context;
    this.documents = documents;

    EsdApplication.getDataComponent().inject(this);
  }

  @Override
  public DocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.documents_adapter_item_layout, parent, false);
    return new DocumentViewHolder(view);
  }


  @Override
  public void onBindViewHolder(final DocumentViewHolder viewHolder, final int position) {
    final InMemoryDocument imd = documents.get(position);

    if (imd.getDocument() != null) {

      Document item = imd.getDocument();

      viewHolder.title.setText( item.getShortDescription() );
      viewHolder.subtitle.setText( item.getComment() );

      //resolved https://tasks.n-core.ru/browse/MVDESD-12625
      //  На плитке Обращения и НПА не показывать строку "Без организации", если её действительно нет(
      //    Timber.d("star with: %s %s", item.getUid().startsWith( Fields.Journal.INCOMING_ORDERS.getValue() ), item.getUid().startsWith( Fields.Journal.CITIZEN_REQUESTS.getValue() ));
      if(
        Arrays.asList(
          Fields.Journal.INCOMING_ORDERS.getValue(),
          Fields.Journal.CITIZEN_REQUESTS.getValue()
        ).contains( imd.getIndex() ) ) {

        if ( item.getOrganization().toLowerCase().contains("без организации") ){
          Timber.d("empty organization" );
          viewHolder.from.setText("");
        } else {
          Timber.e( "SIGNER %s", item.getSigner() );
          if ( item.getSigner() != null ){
            viewHolder.from.setText( item.getSigner().getOrganisation() );
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

      if( Arrays.asList( Fields.Status.SIGNING.getValue(), Fields.Status.APPROVAL.getValue() ).contains(imd.getFilter()) ){

        if (!Objects.equals(item.getFirstLink(), "")){

        }
        // рефактор апи!
//        Timber.tag("Status LINKS").e("size: %s", item.getLinks().size() );
//
//        if ( item.getLinks().size() >= 1){
//
//          try {
//            Set<RLinks> links = item.getLinks();
//
//            ArrayList<RLinks> arrayList = new ArrayList<RLinks>();
//            for (RLinks str : links) {
//              arrayList.add(str);
//            }
//
//            RLinksEntity _link = (RLinksEntity) arrayList.get(0);
//            Timber.tag("Status LINKS").e("size > 0 | first: %s", _link.getUid() );
//
//            RDocumentEntity doc = dataStore
//              .select(RDocumentEntity.class)
//              .where(RDocumentEntity.UID.eq( _link.getUid() ))
//              .get().first();
//
//            viewHolder.date.setText( item.getTitle() + " на " + doc.getRegistrationNumber() );
//          } catch (NoSuchElementException e) {
//            e.printStackTrace();
//          }
//        }

      }

      if ( item.getChanged() != null && item.getChanged() ){
        viewHolder.sync_label.setVisibility(View.VISIBLE);
      } else {
        viewHolder.sync_label.setVisibility(View.GONE);
      }



      if ( item.getControl() != null && item.getControl() ){
        viewHolder.control_label.setVisibility(View.VISIBLE);
      } else {
        viewHolder.control_label.setVisibility(View.GONE);
      }

      if ( item.getFavorites() != null && item.getFavorites() ){;
        viewHolder.favorite_label.setVisibility(View.VISIBLE);
      } else {
        viewHolder.favorite_label.setVisibility(View.GONE);
      }


      // если обработаное - то ничего нельзя делать
       if ( item.isFromFavoritesFolder() || item.isFromProcessedFolder() ){
         viewHolder.lock_label.setVisibility(View.VISIBLE);
       } else {
         viewHolder.lock_label.setVisibility(View.GONE);
       }


      viewHolder.cv.setOnClickListener(view -> {

        settings.setUid( item.getUid() );
        settings.setMainMenuPosition( viewHolder.getAdapterPosition() );
        settings.setRegNumber( item.getRegistrationNumber() );
        settings.setStatusCode( imd.getFilter() );
        settings.setLoadFromSearch( false );
        settings.setRegDate( item.getRegistrationDate() );

        Intent intent = new Intent(mContext, InfoActivity.class);

        MainActivity activity = (MainActivity) mContext;
        activity.startActivity(intent);
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

      if ( item.isRed() ){
        viewHolder.cv.setBackground( ContextCompat.getDrawable(mContext, R.drawable.top_border) );


        // resolved https://tasks.n-core.ru/browse/MVDESD-13426
        // Выделять номер документа красным на плитке
        viewHolder.date.setTextColor( ContextCompat.getColor(mContext, R.color.md_red_A700 ) );

      } else {
        viewHolder.cv.setBackground( ContextCompat.getDrawable(mContext, R.color.md_white_1000 ) );
        viewHolder.date.setTextColor( ContextCompat.getColor(mContext, R.color.md_grey_800 ) );
      }

    }

  }


//  @Override
//  public void onBindViewHolder(final DocumentViewHolder viewHolder, final int position) {
//    final RDocumentEntity item = documents.get(position);
//
//    viewHolder.title.setText( item.getShortDescription() );
//    viewHolder.subtitle.setText( item.getComment() );
//
//    //resolved https://tasks.n-core.ru/browse/MVDESD-12625
//    //  На плитке Обращения и НПА не показывать строку "Без организации", если её действительно нет(
//    //    Timber.d("star with: %s %s", item.getUid().startsWith( Fields.Journal.INCOMING_ORDERS.getValue() ), item.getUid().startsWith( Fields.Journal.CITIZEN_REQUESTS.getValue() ));
//    if(
//          item.getUid().startsWith( Fields.Journal.INCOMING_ORDERS.getValue() )
//      ||  item.getUid().startsWith( Fields.Journal.CITIZEN_REQUESTS.getValue() )
//
//      ){
//
//      if ( item.getOrganization().toLowerCase().contains("без организации") ){
//        Timber.d("empty organization" );
//        viewHolder.from.setText("");
//      } else {
//        Timber.e( "SIGNER %s", item.getSigner() );
//        if ( item.getSigner() != null ){
//          RSignerEntity signer = (RSignerEntity) item.getSigner();
//          viewHolder.from.setText( signer.getOrganisation() );
//        }
//      }
//
//    } else {
//      viewHolder.from.setText( item.getOrganization() );
//    }
//
//    String number = item.getExternalDocumentNumber();
//
//    if (number == null){
//      number = item.getRegistrationNumber();
//    }
//
//    viewHolder.date.setText( item.getTitle() );
//
//    if( Objects.equals(item.getFilter(), Fields.Status.SIGNING.getValue()) ||  Objects.equals(item.getFilter(), Fields.Status.APPROVAL.getValue()) ){
//
//      Timber.tag("Status LINKS").e("size: %s", item.getLinks().size() );
//
//      if ( item.getLinks().size() >= 1){
//
//        try {
//          Set<RLinks> links = item.getLinks();
//
//          ArrayList<RLinks> arrayList = new ArrayList<RLinks>();
//          for (RLinks str : links) {
//            arrayList.add(str);
//          }
//
//          RLinksEntity _link = (RLinksEntity) arrayList.get(0);
//          Timber.tag("Status LINKS").e("size > 0 | first: %s", _link.getUid() );
//
//          RDocumentEntity doc = dataStore
//            .select(RDocumentEntity.class)
//            .where(RDocumentEntity.UID.eq( _link.getUid() ))
//            .get().first();
//
//          viewHolder.date.setText( item.getTitle() + " на " + doc.getRegistrationNumber() );
//        } catch (NoSuchElementException e) {
//          e.printStackTrace();
//        }
//      }
//
//    }
//
//    if ( item.isChanged() != null && item.isChanged() ){
//      viewHolder.sync_label.setVisibility(View.VISIBLE);
//    } else {
//      viewHolder.sync_label.setVisibility(View.GONE);
//    }
//
//
//
//    if ( item.isControl() != null && item.isControl() ){
//      viewHolder.control_label.setVisibility(View.VISIBLE);
//    } else {
//      viewHolder.control_label.setVisibility(View.GONE);
//    }
//
//    if ( item.isFavorites() != null && item.isFavorites() ){;
//      viewHolder.favorite_label.setVisibility(View.VISIBLE);
//    } else {
//      viewHolder.favorite_label.setVisibility(View.GONE);
//    }
//
//
//    // если обработаное - то ничего нельзя делать
//    if (
//      item.isFromFavoritesFolder() != null && item.isFromFavoritesFolder() ||
//      item.isFromProcessedFolder() != null && item.isFromProcessedFolder()
//      ){
//      viewHolder.lock_label.setVisibility(View.VISIBLE);
//    } else {
//      viewHolder.lock_label.setVisibility(View.GONE);
//    }
//
//
//    viewHolder.cv.setOnClickListener(view -> {
//
//      settings.setUid( item.getUid() );
//      settings.setMainMenuPosition( viewHolder.getAdapterPosition() );
//      settings.setRegNumber( item.getRegistrationNumber() );
//      settings.setStatusCode( item.getFilter() );
//      settings.setLoadFromSearch( false );
//      settings.setRegDate( item.getRegistrationDate() );
//
//      Intent intent = new Intent(mContext, InfoActivity.class);
//
//      MainActivity activity = (MainActivity) mContext;
//      activity.startActivity(intent);
////      activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
//
////      Toast.makeText(mContext, " onClick : " + item.getMd5() + " \n" + item.getTitle(), Toast.LENGTH_SHORT).show();
////      viewHolder.swipeLayout.close(true);
//    });
//
//    viewHolder.cv.setOnLongClickListener(view -> {
//
//
////      documentManager.get( item.getUid() ).toJson();
//
//
////      String _title = documentManager.getDocument(item.getUid()).getTitle();
////      String _title = manager.get(item.getUid()).getTitle();
////      Timber.e("title : %s", _title);
////
////      Notification builder =
////        new NotificationCompat.Builder(mContext)
////          .setSmallIcon( R.drawable.gerb )
////          .setContentTitle("Уведомление")
////          .setContentText("Добавлена резолюция к документу " + item.getUid() )
////          .setDefaults(Notification.DEFAULT_ALL)
////          .setCategory(Notification.CATEGORY_MESSAGE)
////          .setPriority(NotificationCompat.PRIORITY_HIGH)
////          .addAction( 1 , "Утвердить", null)
////          .addAction( 0 ,  "Отклонить", null)
////          .build();
////
////      NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
////      notificationManager.notify(0, builder);
////
//      return true;
//    });
//
//    if ( item.getUrgency() != null ){
//      viewHolder.badge.setVisibility(View.VISIBLE);
//      viewHolder.badge.setText( item.getUrgency() );
//    } else {
//      viewHolder.badge.setVisibility(View.GONE);
//    }
//
//    // resolved https://tasks.n-core.ru/browse/MVDESD-12625
//    //  Номер документа на плитке выделять красным.
//    //  Красным документ должен подсвечиваться, только тогда,
//    //  когда создаём проект резолюции.
//    //  В подписавших резолюцию должен быть Министр.
//
//    if (item.isRed() != null && item.isRed()){
//      viewHolder.cv.setBackground( ContextCompat.getDrawable(mContext, R.drawable.top_border) );
//
//
//      // resolved https://tasks.n-core.ru/browse/MVDESD-13426
//      // Выделять номер документа красным на плитке
//      viewHolder.date.setTextColor( ContextCompat.getColor(mContext, R.color.md_red_A700 ) );
//
//    } else {
//      viewHolder.cv.setBackground( ContextCompat.getDrawable(mContext, R.color.md_white_1000 ) );
//      viewHolder.date.setTextColor( ContextCompat.getColor(mContext, R.color.md_grey_800 ) );
//    }
//
//
//
////    setAnimation(viewHolder.itemView, position);
//
//  }

  @Override
  public int getItemCount() {
    return documents == null ? 0 : documents.size();
  }


  public InMemoryDocument getItem(int position) {
    if ( documents.size() == 0 ){
      return null;
    }

    if ( documents.size() < position ){
      return null;
    }

    return documents.get(position);
  }

  public void getNextFromPosition(int position) {
    Timber.tag(TAG).e("getNextFromPosition %s", position);
    Timber.tag(TAG).e("documents.size() %s", documents.size());

    position += 1;

    if ( documents.size() == 0 ){
      EventBus.getDefault().post( new NoDocumentsEvent() );
    } else {

      if (position >= documents.size()) {
        position = 0;
      }

      Timber.tag(TAG).e("position: %s", position);
//
//      RDocumentEntity item = documents.get(position);
//
//      settings.setMainMenuPosition(position);
//      settings.setUid(item.getUid());
//      settings.setRegNumber(item.getRegistrationNumber());
//      settings.setStatusCode(item.getFilter());
//      settings.setRegDate(item.getRegistrationDate());
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


//      RDocumentEntity item = documents.get(position);
//
//      settings.setMainMenuPosition(position);
//      settings.setUid( item.getUid() );
//      settings.setRegNumber( item.getRegistrationNumber() );
//      settings.setStatusCode( item.getFilter() );
////      settings.setFromSign( item.isFromSign() );
//      settings.setRegDate( item.getRegistrationDate() );
    }


  }

  public void clear(){
    Holder.MAP.clear();
    documents.clear();
    notifyDataSetChanged();
  }

  public void addItem(InMemoryDocument document) {
    if ( !Holder.MAP.containsKey( document.getUid()) ){
      Holder.MAP.put( document.getUid(), document );
      documents.add(document);
      notifyItemInserted(documents.size());
    }
  }


  class DocumentViewHolder extends RecyclerView.ViewHolder {
    private TextView sync_label;
    private TextView lock_label;
    private TextView subtitle;
    private TextView badge;
    private TextView control_label;
    private TextView favorite_label;

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

      favorite_label.setVisibility(View.GONE);
      control_label.setVisibility(View.GONE);
    }

  }

  private static class Holder {
    static Map<String, InMemoryDocument> MAP = new HashMap<>();
  }

}
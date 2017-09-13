package sapotero.rxtest.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.db.requery.utils.V2DocumentType;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.views.activities.InfoActivity;
import timber.log.Timber;

public class DocumentsAdapter extends RecyclerView.Adapter<DocumentsAdapter.DocumentViewHolder> {

  @Inject ISettings settings;

  private List<InMemoryDocument> documents;
  private Context mContext;
  private final String TAG = this.getClass().getSimpleName();

  public void removeAllWithRange() {
    Holder.MAP.clear();
    documents.clear();
    notifyDataSetChanged();
  }

  public DocumentsAdapter(Context context, List<InMemoryDocument> documents) {
    Timber.tag("RecyclerViewRefresh").d("DocumentsAdapter: new DocumentsAdapter");
    Timber.tag(TAG).e("INIT");

    this.mContext  = context;
    this.documents = documents;

    EsdApplication.getManagerComponent().inject(this);
  }

  @Override
  public DocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.documents_adapter_item_layout, parent, false);
    return new DocumentViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final DocumentViewHolder viewHolder, final int position) {
    final InMemoryDocument doc = documents.get(position);

    if (doc.getDocument() != null) {

      Document item = doc.getDocument();

      viewHolder.title.setText( item.getShortDescription() );
      viewHolder.subtitle.setText( item.getComment() );

      //resolved https://tasks.n-core.ru/browse/MVDESD-12625
      //  На плитке Обращения и НПА не показывать строку "Без организации", если её действительно нет(
      if( Arrays.asList( V2DocumentType.INCOMING_ORDERS.getName(), V2DocumentType.CITIZEN_REQUESTS.getName() ).contains( doc.getIndex() ) ) {

        if ( item.getOrganization() != null && item.getOrganization().toLowerCase().contains("без организации") ){
          viewHolder.from.setText("");
        } else {
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

      if( Arrays.asList( Fields.Status.SIGNING.getValue(), Fields.Status.APPROVAL.getValue() ).contains(doc.getFilter()) ){

        if (item.getFirstLink() != null && !Objects.equals(item.getFirstLink(), "")){
          viewHolder.date.setText( item.getTitle() + " на " + item.getFirstLink() );
        }

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
        settings.setIsProject( item.isProject() );
        settings.setMainMenuPosition( viewHolder.getAdapterPosition() );
        settings.setRegNumber( item.getRegistrationNumber() );
        settings.setStatusCode( doc.getFilter() );
        settings.setLoadFromSearch( false );
        settings.setRegDate( item.getRegistrationDate() );

        ArrayList<String> documentUids = new ArrayList<>();
        for (InMemoryDocument inMemoryDocument : documents) {
          documentUids.add( inMemoryDocument.getUid() );
        }

        Intent intent = InfoActivity.newIntent(mContext, documentUids);

        Activity activity = (Activity) mContext;
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
        viewHolder.setBackgroundResourceId( R.drawable.top_border );

        // resolved https://tasks.n-core.ru/browse/MVDESD-13426
        // Выделять номер документа красным на плитке
        viewHolder.date.setTextColor( ContextCompat.getColor(mContext, R.color.md_red_A700 ) );
        viewHolder.cv.setCardElevation(4f);
      } else {
        viewHolder.cv.setBackground( ContextCompat.getDrawable(mContext, R.color.md_white_1000 ) );
        viewHolder.date.setTextColor( ContextCompat.getColor(mContext, R.color.md_grey_800 ) );
      }

      if ( item.isReturned() ) {
        viewHolder.returned_label.setVisibility(View.VISIBLE);
      } else {
        viewHolder.returned_label.setVisibility(View.GONE);
      }

      if ( item.isRejected() ) {
        viewHolder.rejected_label.setVisibility(View.VISIBLE);
      } else {
        viewHolder.rejected_label.setVisibility(View.GONE);
      }

      if ( item.isAgain() ) {
        viewHolder.again_label.setVisibility(View.VISIBLE);
      } else {
        viewHolder.again_label.setVisibility(View.GONE);
      }
    }


    switch ( doc.getState() ){

      case LOADING:
        viewHolder.cv.setCardElevation(0f);
        viewHolder.cv.setBackground( ContextCompat.getDrawable( mContext, R.drawable.color_change_to_dark) );
        TransitionDrawable transition = (TransitionDrawable) viewHolder.cv.getBackground();
        transition.startTransition(300);
        transition.setCrossFadeEnabled(true);

        viewHolder.sync_label.setVisibility(View.VISIBLE);
        viewHolder.cv.setClickable(false);
        viewHolder.cv.setFocusable(false);

        break;
      case READY:
        viewHolder.cv.setCardElevation(4f);
        viewHolder.cv.setClickable(true);
        viewHolder.cv.setFocusable(true);
        break;
    }
  }

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

  public void clear(){
    Holder.MAP.clear();
    documents.clear();
    notifyDataSetChanged();
  }

  public void addItem(InMemoryDocument document) {
    if ( !Holder.MAP.containsKey( document.getUid()) ){
      documents.add(document);
      notifyItemInserted( documents.size() );
      recreateHash();
    }
  }

  private void recreateHash() {
    Holder.MAP = new HashMap<>();

    for (int i = 0; i < documents.size(); i++) {
      Holder.MAP.put( documents.get(i).getUid(), i );
    }
  }


  public void addList(List<InMemoryDocument> docs) {

    documents = docs;
    recreateHash();

    notifyDataSetChanged();
  }

  public class DocumentViewHolder extends RecyclerView.ViewHolder {
    private TextView sync_label;
    private TextView lock_label;
    private TextView subtitle;
    private TextView badge;
    private TextView control_label;
    private TextView favorite_label;
    private TextView returned_label;
    private TextView rejected_label;
    private TextView again_label;

    private CardView cv;
    private TextView title;
    private TextView date;
    private TextView from;

    private int backgroundResourceId = 0;

    DocumentViewHolder(View itemView) {
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
      returned_label  = (TextView)itemView.findViewById(R.id.returned_label);
      rejected_label  = (TextView)itemView.findViewById(R.id.rejected_label);
      again_label  = (TextView)itemView.findViewById(R.id.again_label);

      favorite_label.setVisibility(View.GONE);
      control_label.setVisibility(View.GONE);
      returned_label.setVisibility(View.GONE);
      rejected_label.setVisibility(View.GONE);
      again_label.setVisibility(View.GONE);
    }

    public int getBackgroundResourceId() {
      return backgroundResourceId;
    }

    public void setBackgroundResourceId(int backgroundResourceId) {
      this.backgroundResourceId = backgroundResourceId;
    }
  }

  private static class Holder {
    static Map<String, Integer> MAP = new HashMap<>();
  }

}
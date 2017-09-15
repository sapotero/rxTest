package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sapotero.rxtest.R;
import sapotero.rxtest.db.mapper.PerformerMapper;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;
import timber.log.Timber;

public class PrimaryUsersAdapter extends BaseAdapter implements Filterable {

  private Context context;
  private final ArrayList<PrimaryConsiderationPeople> sourceItems;
  private ArrayList<PrimaryConsiderationPeople> resultItems;
  private String TAG = DecisionAdapter.class.getSimpleName();

  private PrimaryUsersAdapterFilterListener primaryUsersAdapterFilterListener = null;

  public PrimaryUsersAdapter(Context context, ArrayList<PrimaryConsiderationPeople> items) {
    this.sourceItems = items;
    this.resultItems = items;
    this.context = context;
  }

  public void registerListener(PrimaryUsersAdapterFilterListener primaryUsersAdapterFilterListener) {
    this.primaryUsersAdapterFilterListener = primaryUsersAdapterFilterListener;
  }

  @Override
  public View getView(int position, View view, ViewGroup parent) {
    ViewHolder viewHolder;

    if (view == null){
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = inflater.inflate(R.layout.primary_user, parent, false);

      viewHolder = new ViewHolder();
      viewHolder.name = (TextView) view.findViewById(R.id.primary_user__name);
      viewHolder.title = (TextView) view.findViewById(R.id.primary_user__title);
      viewHolder.image = (ImageView) view.findViewById(R.id.primary_user__image);
      viewHolder.image_wrapper = (CardView) view.findViewById(R.id.primary_user__image_wrapper);
      viewHolder.wrapper = (LinearLayout) view.findViewById(R.id.primary_user__wrapper);

      view.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) view.getTag();
    }
    PrimaryConsiderationPeople user = getItem(position);

    viewHolder.name.setText( user.getName() );
    viewHolder.title.setText( user.getOrganization() );

    Timber.e( "getIImage %s", user.getIImage() );

    if (user.getIImage() != null){
      try {
        String str = user.getIImage().replaceAll("(\\n)", "");
        byte[] decodedString = Base64.decode(str.getBytes(), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        viewHolder.image.setImageBitmap(decodedByte);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else {
      viewHolder.image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable._person));
      viewHolder.image_wrapper.setVisibility(View.VISIBLE);
    }

    if ( user.isDelimiter() ){
      viewHolder.image_wrapper.setVisibility(View.GONE);
    }



    return view;
  }

  public void add(PrimaryConsiderationPeople user) {
    sourceItems.add( user );
    notifyDataSetChanged();
  }

  public void addAll(ArrayList<PrimaryConsiderationPeople> users) {
    sourceItems.addAll(users);
    notifyDataSetChanged();
  }

  public Oshs getOshs(int position){
    PrimaryConsiderationPeople item = resultItems.get(position);
    Oshs oshs = (Oshs) new PerformerMapper().convert(item, PerformerMapper.DestinationType.OSHS);
    return oshs;
  }

  @Override
  public Filter getFilter() {
    return new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults();
        if (constraint != null) {
          List<PrimaryConsiderationPeople> results = new ArrayList<>();
          for (PrimaryConsiderationPeople item : sourceItems) {
            if (item.getName().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
              results.add(item);
            }
          }
          filterResults.values = results;
          filterResults.count = results.size();
        }
        return filterResults;
      }

      @Override
      protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        if (filterResults != null && filterResults.count > 0) {
          resultItems = (ArrayList<PrimaryConsiderationPeople>) filterResults.values;
          notifyDataSetChanged();
        } else {
          resultItems = new ArrayList<>();
          notifyDataSetChanged();
        }

        if ( primaryUsersAdapterFilterListener != null ) {
          primaryUsersAdapterFilterListener.onPrimaryUsersAdapterFilterComplete();
        }
      }
    };
  }

  public void cancelFiltering() {
    resultItems = sourceItems;
    notifyDataSetChanged();
  }

  private static class ViewHolder {
    public TextView name;
    public TextView title;
    public ImageView image;
    public CardView image_wrapper;
    public LinearLayout wrapper;
  }

  @Override
  public int getCount() {
    return resultItems.size();
  }

  public PrimaryConsiderationPeople getItem(int i){
    return resultItems.get(i);
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

  public List<PrimaryConsiderationPeople> getResultItems() {
    return resultItems;
  }

  @Override
  public void notifyDataSetChanged() {
    // resolved https://tasks.n-core.ru/browse/MVDESD-13414
    // Отображать порядок ДЛ в МП, также как в группах СЭД
    Collections.sort(sourceItems, (o1, o2) -> o1.getSortIndex() != null && o2.getSortIndex() != null ? o1.getSortIndex().compareTo( o2.getSortIndex() ) : 0 );

    super.notifyDataSetChanged();
  }

  public void addResultItem(PrimaryConsiderationPeople user) {
    resultItems.add( user );
    notifyDataSetChanged();
  }

  public void addFirstResultItem(PrimaryConsiderationPeople user) {
    resultItems.add( 0, user );
    notifyDataSetChanged();
  }

  public void removeItem(PrimaryConsiderationPeople user) {
    resultItems.remove( user );
    notifyDataSetChanged();
  }

  public interface PrimaryUsersAdapterFilterListener {
    void onPrimaryUsersAdapterFilterComplete();
  }
}

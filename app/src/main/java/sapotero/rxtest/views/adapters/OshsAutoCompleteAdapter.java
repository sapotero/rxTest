package sapotero.rxtest.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.retrofit.utils.OshsAdapterService;
import sapotero.rxtest.retrofit.utils.RetrofitManager;
import sapotero.rxtest.utils.ISettings;

public class OshsAutoCompleteAdapter  extends BaseAdapter implements Filterable {

  private static final int MAX_RESULTS = 10;
  private Context mContext;
  private List<Oshs> resultList = new ArrayList<Oshs>();

  private String TAG = this.getClass().getSimpleName();

  @Inject OkHttpClient okHttpClient;
  @Inject ISettings settings;

  private ArrayList<String> ignore_user_ids;

  private int threshold;
  
  private View view;

  // If true, organizations will be included in search results
  private boolean withOrganizations = false;

  private OshsAutoCompleteAdapterFilterListener oshsAutoCompleteAdapterFilterListener = null;

  public OshsAutoCompleteAdapter(Context context, View view) {
    mContext = context;
    EsdApplication.getNetworkComponent().inject( this );
    this.view = view;
  }

  public void registerListener(OshsAutoCompleteAdapterFilterListener oshsAutoCompleteAdapterFilterListener) {
    this.oshsAutoCompleteAdapterFilterListener = oshsAutoCompleteAdapterFilterListener;
  }

  @Override
  public int getCount() {
    return resultList.size();
  }

  @Override
  public Oshs getItem(int index) {
    return resultList.get(index);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = inflater.inflate(R.layout.oshs_dropdown_item, parent, false);
    }
    ((TextView) convertView.findViewById(R.id.user_name)).setText(getItem(position).getName());
    ((TextView) convertView.findViewById(R.id.user_organization)).setText(getItem(position).getOrganization());


    return convertView;
  }

  @Override
  public Filter getFilter() {
    return new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults();
        filterResults.count = 0;

        if (constraint != null && constraint.length() >= threshold) {
          List<Oshs> results = null;
          try {
            results = findOshs(mContext, constraint.toString());
          } catch (IOException e) {
            e.printStackTrace();
          }

          filterResults.values = results;
          assert results != null;
          filterResults.count  = results.size();
        }

        return filterResults;
      }

      @Override
      protected void publishResults(CharSequence constraint, FilterResults results) {
        if (results != null && results.values != null && results.count > 0) {
          resultList = (List<Oshs>) results.values;

          if (view != null) {
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
          }

          if ( oshsAutoCompleteAdapterFilterListener != null ) {
            oshsAutoCompleteAdapterFilterListener.onOshsAutoCompleteAdapterFilterComplete();
          }

          notifyDataSetChanged();
        } else {
          notifyDataSetInvalidated();
        }
      }};
  }

  private List<Oshs> findOshs(Context context, String term) throws IOException {

    Retrofit retrofit = new RetrofitManager( context, settings.getHost() + "/v2/", okHttpClient).process();
    OshsAdapterService documentsService = retrofit.create( OshsAdapterService.class );
    Call<Oshs[]> call = documentsService.find(settings.getLogin(), settings.getToken(), term);

    Oshs[] data = call.execute().body();

    ArrayList<Oshs> result = new ArrayList<>();

    if (data != null && data.length > 0){
      for (Oshs oshs : data) {
        String oshsId = oshs.getId();
        Boolean isGroup = oshs.getIsGroup();
        Boolean isOrganization = oshs.getIsOrganization();

        if ( withOrganizations ) {
          // To include organizations in search results consider all items as not organizations
          isOrganization = false;
        }

        if (oshsId != null && isGroup != null && isOrganization != null) {
          if (!isGroup && !isOrganization) {
            if ( ignore_user_ids != null && ignore_user_ids.contains(oshsId) ) {
              continue;
            }
            result.add(oshs);
          }
        }
      }
    }

    return result;
  }

  public void setIgnoreUsers(ArrayList<String> users) {
    ignore_user_ids = new ArrayList<>();
    if (users != null) {
      for ( String ignoreUser : users ) {
        ignore_user_ids.add( ignoreUser );
      }
    }
  }

  public void addIgnoreUser(String ignoreUser) {
    if ( ignore_user_ids != null ) {
      ignore_user_ids.add( ignoreUser );
    }
  }

  public void setThreshold(int threshold) {
    this.threshold = threshold;
  }

  public void withOrganizations(boolean withOrganizations) {
    this.withOrganizations = withOrganizations;
  }

  public void clear() {
    resultList.clear();
    notifyDataSetChanged();
  }

  public List<Oshs> getResultList() {
    return resultList;
  }

  public interface OshsAutoCompleteAdapterFilterListener {
    void onOshsAutoCompleteAdapterFilterComplete();
  }
}

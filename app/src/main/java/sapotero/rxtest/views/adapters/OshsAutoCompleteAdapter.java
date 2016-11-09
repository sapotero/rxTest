package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.application.config.Constant;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.retrofit.utils.OshsAdapterService;
import sapotero.rxtest.retrofit.utils.RetrofitManager;

public class OshsAutoCompleteAdapter  extends BaseAdapter implements Filterable {

  private static final int MAX_RESULTS = 10;
  private Context mContext;
  private List<Oshs> resultList = new ArrayList<Oshs>();

  private String TAG = this.getClass().getSimpleName();

  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;

  private Preference<String> login;
  private Preference<String> token;


  public OshsAutoCompleteAdapter(Context context) {
    mContext = context;
    EsdApplication.getComponent( context ).inject( this );
    loadSettings();
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
    Filter filter = new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults();
        if (constraint != null) {
          List<Oshs> results = null;
          try {
            results = findOshs(mContext, constraint.toString());
          } catch (IOException e) {
            e.printStackTrace();
          }

          filterResults.values = results;
          filterResults.count  = results.size();
        }
        return filterResults;
      }

      @Override
      protected void publishResults(CharSequence constraint, FilterResults results) {
        if (results != null && results.count > 0) {
          resultList = (List<Oshs>) results.values;
          notifyDataSetChanged();
        } else {
          notifyDataSetInvalidated();
        }
      }};
    return filter;
  }

  private List<Oshs> findOshs(Context context, String OshsTitle) throws IOException {

    Retrofit retrofit = new RetrofitManager( context, Constant.HOST + "/v2/", okHttpClient).process();
    OshsAdapterService documentsService = retrofit.create( OshsAdapterService.class );
    Call<Oshs[]> call = documentsService.find(login.get(), token.get(), OshsTitle);

    Oshs[] data = call.execute().body();

    return new ArrayList<>(Arrays.asList(data));
  }

  private void loadSettings(){
    login = settings.getString("login");
    token = settings.getString("token");
  }
}
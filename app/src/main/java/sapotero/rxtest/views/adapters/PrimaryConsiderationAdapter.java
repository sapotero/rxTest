package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.ArrayList;

import javax.inject.Inject;

import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.views.adapters.models.PrimaryConsiderationAdapterViewModel;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;

public class PrimaryConsiderationAdapter extends BaseAdapter {

  @Inject RxSharedPreferences settings;

  private Context context;
  private final ArrayList<PrimaryConsiderationPeople> items;
  private String TAG = DecisionAdapter.class.getSimpleName();

  private  ArrayList<PrimaryConsiderationAdapterViewModel> checked = new ArrayList<>();

  public PrimaryConsiderationAdapter(Context context, ArrayList<PrimaryConsiderationPeople> items) {
    this.items = items;
    this.context = context;
    EsdApplication.getComponent(context).inject( this );
  }

  private PrimaryConsiderationAdapter.Callback callback;
  public interface Callback {
    void onRemove();
    void onChange();
    void onAttrChange();
  }

  public void registerCallBack(PrimaryConsiderationAdapter.Callback callback){
    this.callback = callback;
  }


  @Override
  public View getView(int position, View view, ViewGroup parent) {
    ViewHolder viewHolder;

    if (view == null){
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = inflater.inflate(R.layout.primary_people, parent, false);

      viewHolder = new ViewHolder();
      viewHolder.name   = (TextView) view.findViewById(R.id.primary_name);
      viewHolder.remove = (Button)   view.findViewById(R.id.remove_user);

      viewHolder.copy        = (Switch) view.findViewById(R.id.copy);
      viewHolder.responsible = (Switch) view.findViewById(R.id.responsible);

      // настройка
      // Отображать настройки подлинника
      if (settings.getBoolean("settings_view_show_origin").get()){
        viewHolder.copy.setVisibility(View.VISIBLE);
      }
      view.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) view.getTag();
    }
    PrimaryConsiderationPeople user = getItem(position);

    if ( !checked.contains(viewHolder) ){
      checked.add( new PrimaryConsiderationAdapterViewModel(position, viewHolder, user) );
    }

    viewHolder.name.setText( user.getName() );
    viewHolder.copy.setChecked( user.isCopy() );
    viewHolder.responsible.setChecked( user.isResponsible() );

    viewHolder.remove.setOnClickListener(v -> {

      if (items.indexOf( user ) != -1 ){
        items.remove(position);


        for ( PrimaryConsiderationAdapterViewModel check: checked ) {
          if ( check.getUser() == user ){
            checked.remove(check);
            break;
          }
        }

        callback.onRemove();
      }

      notifyDataSetChanged();
    });


    viewHolder.copy.setOnClickListener(v -> {

      for (PrimaryConsiderationPeople u : items){
        u.setCopy(false);
      }
      user.setCopy( viewHolder.copy.isChecked() );


      updateView();

      if (callback != null) {
        callback.onChange();
      }
    });

    viewHolder.responsible.setOnClickListener(v -> {

      for (PrimaryConsiderationPeople u : items){
        u.setResponsible(false);
      }
      user.setResponsible( viewHolder.responsible.isChecked() );

      updateView();
    });

    return view;


  }

  private void updateView() {
    for ( PrimaryConsiderationAdapterViewModel check: checked ) {
      ViewHolder viewholder = check.getViewholder();
      viewholder.copy.setChecked( check.getUser().isCopy() );
      viewholder.responsible.setChecked( check.getUser().isResponsible() );
    }
  }

  public void add(PrimaryConsiderationPeople user) {
    if (user != null){
      items.add( user );
      notifyDataSetChanged();
    }
  }

  public static class ViewHolder {
    public TextView name;
    public Button remove;
    public Switch copy;
    public Switch responsible;
  }

  @Override
  public int getCount() {
    return items.size();
  }

  public ArrayList<PrimaryConsiderationPeople> getAll() {
    return items;
  }

  public PrimaryConsiderationPeople getItem(int i){
    return items.get(i);
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

}

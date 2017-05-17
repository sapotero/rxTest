package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.views.adapters.models.PrimaryConsiderationAdapterViewModel;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;
import timber.log.Timber;

public class PrimaryConsiderationAdapter extends BaseAdapter {

  @Inject Settings settings;

  private Context context;
  private final ArrayList<PrimaryConsiderationPeople> items;
  private String TAG = DecisionAdapter.class.getSimpleName();

  private  ArrayList<PrimaryConsiderationAdapterViewModel> checked = new ArrayList<>();

  public PrimaryConsiderationAdapter(Context context, ArrayList<PrimaryConsiderationPeople> items) {
    this.items = items;
    this.context = context;
    EsdApplication.getDataComponent().inject( this );
  }

  private Callback callback;

  public interface Callback {
    void onRemove();
    void onChange();
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }

  public boolean hasOriginal() {
    Boolean result = false;

    if (items.size() > 0){
      for (PrimaryConsiderationPeople user: items) {
        if ( user.isOriginal() ){
          result = true;
        }
      }
    }
    return result;
  }

  public void dropAllOriginal() {
    Timber.tag("Adapter").i("size: %s", items.size() );
    if (items.size() > 0){
      for (PrimaryConsiderationPeople user: items) {
        user.setOriginal(false);
      }
    }

    if (checked.size() > 0){
      for (PrimaryConsiderationAdapterViewModel vm: checked) {
        vm.getViewholder().is_original.setChecked(false);
      }
    }

    notifyDataSetChanged();
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

      viewHolder.is_responsible = (Switch) view.findViewById(R.id.is_responsible);
      viewHolder.is_original    = (Switch) view.findViewById(R.id.is_original);

      // настройка
      // Отображать настройки подлинника
      if (settings.isShowOrigin()){
        viewHolder.is_original.setVisibility(View.VISIBLE);
      }
      view.setTag(viewHolder);

    } else {
      viewHolder = (ViewHolder) view.getTag();
    }
    PrimaryConsiderationPeople user = getItem(position);

    PrimaryConsiderationAdapterViewModel checked_view = new PrimaryConsiderationAdapterViewModel(position, viewHolder, user);
    if ( !checked.contains(checked_view) ){
      checked.add( checked_view );
    }

    viewHolder.name.setText( user.getName().replaceAll( "\\(.+\\)", "" ) );
    viewHolder.is_responsible.setChecked( user.isResponsible() );
    viewHolder.is_original.setChecked( user.isOriginal() );

    viewHolder.remove.setOnClickListener(v -> {

      if (items.indexOf( user ) != -1 ){
        items.remove(position);


        for ( PrimaryConsiderationAdapterViewModel check: checked ) {
          if ( check.getUser() == user ){
            checked.remove(check);
            break;
          }
        }

        if (callback != null) {
          callback.onRemove();
        }
      }

      notifyDataSetChanged();
    });


    viewHolder.is_responsible.setOnClickListener(v -> {

      for (PrimaryConsiderationPeople u : items){
        u.setResponsible(false);
      }
      user.setResponsible( viewHolder.is_responsible.isChecked() );

      if (callback != null) {
        callback.onChange();
      }

      updateView();
    });

    viewHolder.is_original.setOnClickListener(v -> {

      for (PrimaryConsiderationPeople u : items){
        u.setOriginal(false);
      }
      user.setOriginal( viewHolder.is_original.isChecked() );


      if (callback != null) {
        callback.onChange();
      }

      updateView();
    });

    return view;


  }

  private void updateView() {

    for ( PrimaryConsiderationAdapterViewModel check: checked ) {
      ViewHolder viewholder = check.getViewholder();
      viewholder.is_responsible.setChecked( check.getUser().isResponsible() );
      viewholder.is_original.setChecked( check.getUser().isOriginal() );
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
    public Switch is_responsible;
    public Switch is_original;
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

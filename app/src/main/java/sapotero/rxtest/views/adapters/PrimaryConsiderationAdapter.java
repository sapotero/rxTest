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

import sapotero.rxtest.R;
import sapotero.rxtest.views.adapters.models.PrimaryConsiderationAdapterViewModel;
import sapotero.rxtest.views.adapters.utils.PrimaryConsiderationPeople;

public class PrimaryConsiderationAdapter extends BaseAdapter {

  private Context context;
  private final ArrayList<PrimaryConsiderationPeople> items;
  private String TAG = DecisionAdapter.class.getSimpleName();

  private  ArrayList<PrimaryConsiderationAdapterViewModel> checked = new ArrayList<>();

  public PrimaryConsiderationAdapter(Context context, ArrayList<PrimaryConsiderationPeople> items) {
    this.items = items;
    this.context = context;
  }

  private PrimaryConsiderationAdapter.Callback callback;
  public interface Callback {
    void onRemove();
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
      viewHolder.name     = (TextView) view.findViewById(R.id.primary_name);
      viewHolder.position = (TextView) view.findViewById(R.id.primary_title);
      viewHolder.remove = (Button)   view.findViewById(R.id.remove_user);

      viewHolder.copy        = (Switch) view.findViewById(R.id.copy);
      viewHolder.responsible = (Switch) view.findViewById(R.id.responsible);
      viewHolder.out         = (Switch) view.findViewById(R.id.out);

      view.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) view.getTag();
    }
    PrimaryConsiderationPeople user = getItem(position);

    if ( !checked.contains(viewHolder) ){
      checked.add( new PrimaryConsiderationAdapterViewModel(position, viewHolder, user) );
    }


    viewHolder.name.setText( user.getName() );
    viewHolder.position.setText( user.getPosition() );

    viewHolder.copy.setChecked( user.isCopy() );
    viewHolder.responsible.setChecked( user.isResponsible() );
    viewHolder.out.setChecked( user.isOut() );

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
    });

    viewHolder.responsible.setOnClickListener(v -> {

      for (PrimaryConsiderationPeople u : items){
        u.setResponsible(false);
      }
      user.setResponsible( viewHolder.responsible.isChecked() );

      updateView();
    });

    viewHolder.out.setOnClickListener(v -> {
      user.setOut( viewHolder.out.isChecked() );
    });


    return view;


  }

  private void updateView() {
    for ( PrimaryConsiderationAdapterViewModel check: checked ) {
      ViewHolder viewholder = check.getViewholder();
      viewholder.copy.setChecked( check.getUser().isCopy() );
      viewholder.responsible.setChecked( check.getUser().isResponsible() );
      viewholder.out.setChecked( check.getUser().isOut() );
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
    public TextView position;
    public Button remove;
    public Switch copy;
    public Switch responsible;
    public Switch out;
  }

  @Override
  public int getCount() {
    return items.size();
  }

  public PrimaryConsiderationPeople getItem(int i){
    return items.get(i);
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

}

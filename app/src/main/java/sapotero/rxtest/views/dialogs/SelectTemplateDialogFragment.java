package sapotero.rxtest.views.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.utils.Settings;

public class SelectTemplateDialogFragment extends DialogFragment implements View.OnClickListener {

  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject Settings settings;

  private String TAG = this.getClass().getSimpleName();
  Callback callback;
  private String type;

  public void setType(String type) {
    this.type = type;
  }

  public interface Callback {
    void onSelectTemplate(String template);
  }
  public void registerCallBack(Callback callback){
    this.callback = callback;
  }


  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    EsdApplication.getDataComponent().inject( this );
    getDialog().setTitle("Title!");

    View view = inflater.inflate(R.layout.dialog_choose_template, container, false);
    ListView list = (ListView) view.findViewById(R.id.dialog_template_listview_templates);

    ArrayList<String> items = new ArrayList<>();

    List<RTemplateEntity> templates = dataStore
      .select( RTemplateEntity.class)
      .where(  RTemplateEntity.USER.eq( settings.getLogin() ))
      .and(RTemplateEntity.TYPE.eq(type))
      .get().toList();

    if (templates.size() > 0) {
      for (RTemplateEntity tmp : templates){
        items.add( tmp.getTitle() );
      }
    }

    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.list_black_text, items);
    list.setAdapter(adapter);

    list.setOnItemClickListener((parent, view12, position, id) -> {
      if ( callback != null){
        callback.onSelectTemplate( adapter.getItem( position ) );
        dismiss();
      }
    });

    return view;
  }


  @Override
  public void onClick(View v) {

  }
}

package sapotero.rxtest.views.menu.factories;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import sapotero.rxtest.views.adapters.models.DocumentTypeItem;
import sapotero.rxtest.views.adapters.utils.DocumentTypeAdapter;
import sapotero.rxtest.views.menu.builders.ButtonBuilder;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import sapotero.rxtest.views.menu.fields.Item;
import timber.log.Timber;

public class ItemsBuilder implements ButtonBuilder.Callback {

  private final Context context;
  private FrameLayout view;
  private Spinner journalSpinner;
  private DocumentTypeAdapter journalSpinnerAdapter;

  private String TAG = this.getClass().getSimpleName();



  private Callback callback;

  public interface Callback {
    void onMenuUpdate(ArrayList<ConditionBuilder> result);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }



  public ItemsBuilder(Context context) {
    this.context = context;
  }

  public void setSpinner(Spinner selector) {
    journalSpinner = selector;

    journalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        Timber.tag(TAG).w( journalSpinnerAdapter.getItem(position).getName() );

        updateView();
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
      }
    });
  }

  public void setSpinnerDefaults() {

    List<DocumentTypeItem> document_types = new ArrayList<>();

    for ( Item item : Item.values()) {
      document_types.add( new DocumentTypeItem( context, item ) );
    }

    journalSpinnerAdapter = new DocumentTypeAdapter(context, document_types);
    journalSpinner.setAdapter(journalSpinnerAdapter);
  }



  public void prev() {
    journalSpinner.setSelection( journalSpinnerAdapter.prev() );
  }
  public void next() {
    journalSpinner.setSelection( journalSpinnerAdapter.next() );
  }


  public View getView() {
    return view;
  }


  private void updateView() {
    if (view == null){
      view = new FrameLayout(context);
    }

    view.removeAllViews();


    Item item = getSelectedItem();
    Timber.tag(TAG).i( "CURRENT BUTTONS: %s", item.getButtons().size() );

    RadioGroup button_group = getButtonGroupLayout(context);

    if ( item.getButtons().size() > 0 ){
      for ( ButtonBuilder button: item.getButtons() ){
        button_group.addView( button.getView(context) );
        button.registerCallBack(this);
      }
      ((RadioButton) button_group.getChildAt(0)).setChecked(true);
    }

    if ( item.isVisible() ) {
      Timber.tag(TAG).w("visible");
    }

    view.addView( button_group );

//    callback.onMenuUpdate( getCountConditions() );
  }

  private Item getSelectedItem(){
    return journalSpinnerAdapter.getItem(journalSpinner.getSelectedItemPosition()).getItem();
  }

  private ArrayList<ConditionBuilder> getConditions(){
    Item item = getSelectedItem();

    ArrayList<ConditionBuilder> result = new ArrayList<>();

    Collections.addAll(result, item.getQueryConditions());

    if (item.getButtons().size() > 0) {
      for (ButtonBuilder b : item.getButtons()) {

        RadioButton button = b.getButton();

        Boolean active = false;
        if (button != null){
          active =  button.isPressed();
        }


        Timber.e( "button conditions: %s | active: %s ", Arrays.toString( b.getConditions() ), active  );

        if ( b.isActive() ){
          Collections.addAll(result, b.getConditions());
        }
      }
    }
    return result;
  }

  private RadioGroup getButtonGroupLayout(Context context) {
    RadioGroup view = new RadioGroup(context);
    RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT,
      1.0f
    );
    view.setOrientation(RadioGroup.HORIZONTAL);
    view.setLayoutParams(params);
    return view;
  }


  @Override
  public void onButtonBuilderUpdate() {
    Timber.tag(TAG).i( "onButtonBuilderUpdate" );
    callback.onMenuUpdate( getConditions() );
  }

}

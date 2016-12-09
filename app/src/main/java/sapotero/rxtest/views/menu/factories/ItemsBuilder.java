package sapotero.rxtest.views.menu.factories;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
import sapotero.rxtest.views.views.MultiOrganizationSpinner;
import timber.log.Timber;

public class ItemsBuilder implements ButtonBuilder.Callback {

  private String TAG = this.getClass().getSimpleName();
  private final Context context;
  private FrameLayout view;
  private Spinner journalSpinner;
  private DocumentTypeAdapter journalSpinnerAdapter;
  private Callback callback;
  private LinearLayout organizationsLayout;
  private MultiOrganizationSpinner organizationSelector;
  private CheckBox favoritesButton;



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

  public void setOrganizationsLayout(LinearLayout organizationsLayout) {
    this.organizationsLayout = organizationsLayout;
  }

  public void setOrganizationSelector(MultiOrganizationSpinner organizationSelector) {
    this.organizationSelector = organizationSelector;
  }

  public void setFavoritesButton(CheckBox favoritesButton) {
    this.favoritesButton = favoritesButton;
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

    if (organizationsLayout != null){
      organizationsLayout.setVisibility( item.isVisible() ?  View.VISIBLE : View.GONE);
    }

    view.addView( button_group );
  }

  private Item getSelectedItem(){
    return journalSpinnerAdapter.getItem(journalSpinner.getSelectedItemPosition()).getItem();
  }

  private ArrayList<ConditionBuilder> getConditions(){
    Item item = getSelectedItem();

    ArrayList<ConditionBuilder> result = new ArrayList<>();

    Collections.addAll(result, item.getQueryConditions());

    if (item.getButtons().size() > 0) {
      Boolean empty = true;
      for (ButtonBuilder b : item.getButtons()) {

        RadioButton button = b.getButton();

        Boolean active = false;
        if (button != null){
          active =  button.isPressed();

          if ( active ){
            empty = false;
            Collections.addAll(result, b.getConditions());
          }
        }

        Timber.e( "button conditions: %s | active: %s ", Arrays.toString( b.getConditions() ), active  );
      }

      if (empty){
        Collections.addAll(result, item.getButtons().get(0).getConditions() );
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

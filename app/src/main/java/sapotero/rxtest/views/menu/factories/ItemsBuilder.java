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

import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.views.adapters.models.DocumentTypeItem;
import sapotero.rxtest.views.adapters.utils.DocumentTypeAdapter;
import sapotero.rxtest.views.menu.builders.ButtonBuilder;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import sapotero.rxtest.views.menu.fields.MainMenuItem;
import sapotero.rxtest.views.views.MultiOrganizationSpinner;
import timber.log.Timber;

public class ItemsBuilder implements ButtonBuilder.Callback {

  @Inject RxSharedPreferences settings;

  private String TAG = this.getClass().getSimpleName();
  private final Context context;
  private FrameLayout view;
  private Spinner journalSpinner;
  private DocumentTypeAdapter journalSpinnerAdapter;
  private Callback callback;
  private LinearLayout organizationsLayout;
  private MultiOrganizationSpinner organizationSelector;
  private CheckBox favoritesButton;
  private String user;


  public boolean isVisible() {
    return getSelectedItem().isVisible();
  }

  public void setUser(String user) {
    this.user = user;
  }


  public interface Callback {
    void onMenuUpdate(ArrayList<ConditionBuilder> result);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }



  public ItemsBuilder(Context context) {
    this.context = context;
    EsdApplication.getComponent( context ).inject(this);
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
    this.favoritesButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
      Timber.tag("checkbox").i( "checked: %s", isChecked );
      onButtonBuilderUpdate();
    });
  }

  public void setSpinnerDefaults() {

    List<DocumentTypeItem> document_types = new ArrayList<>();

    for ( MainMenuItem mainMenuItem : MainMenuItem.values()) {
      document_types.add( new DocumentTypeItem( context, mainMenuItem, user ) );
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
  public void get( int type ) {
    journalSpinner.setSelection( type );
  }

  public View getView() {
    return view;
  }


  private void updateView() {
    if (view == null){
      view = new FrameLayout(context);
    }

    view.removeAllViews();


    MainMenuItem mainMenuItem = getSelectedItem();
    Timber.tag(TAG).i( "CURRENT BUTTONS: %s", mainMenuItem.getMainMenuButtons().size() );

    RadioGroup button_group = getButtonGroupLayout(context);

    if ( mainMenuItem.getMainMenuButtons().size() > 0 ){
      for ( ButtonBuilder button: mainMenuItem.getMainMenuButtons() ){
        button_group.addView( button.getView(context) );
        button.registerCallBack(this);
      }
      ((RadioButton) button_group.getChildAt(0)).setChecked(true);
    } else {
      onButtonBuilderUpdate();
    }

    if (organizationsLayout != null){
      organizationsLayout.setVisibility( mainMenuItem.isVisible() ?  View.VISIBLE : View.GONE);
    }

    view.addView( button_group );
  }

  public MainMenuItem getSelectedItem(){
    return journalSpinnerAdapter.getItem(journalSpinner.getSelectedItemPosition()).getMainMenuItem();
  }

  private ArrayList<ConditionBuilder> getConditions(){
    MainMenuItem mainMenuItem = getSelectedItem();

    ArrayList<ConditionBuilder> result = new ArrayList<>();

    Collections.addAll(result, mainMenuItem.getQueryConditions() );

    if (mainMenuItem.getMainMenuButtons().size() > 0) {
      Boolean empty = true;
      for (ButtonBuilder b : mainMenuItem.getMainMenuButtons()) {

        RadioButton button = b.getButton();

        Boolean active = false;

        if (button != null){
          active =  button.isPressed();

          if ( active ){
            empty = false;
            Collections.addAll(result, b.getConditions());
          }
        }

//        Timber.e( "button conditions: %s | active: %s %s %s", Arrays.toString( b.getConditions() ), active, button.isActivated(), button.isChecked()  );
      }

      if (empty){
        Boolean nil = true;

        for (ButtonBuilder b : mainMenuItem.getMainMenuButtons()) {
          RadioButton button = b.getButton();
          if ( button != null && button.isChecked() ){
            Collections.addAll(result, b.getConditions() );
            nil = false;
          }
        }

        if ( nil )  {
          Collections.addAll(result, mainMenuItem.getMainMenuButtons().get(0).getConditions() );
        }

      }
    }
//
//    Timber.e( "button conditions: %s ", favoritesButton.isChecked() );
//
//    result.add( new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.USER.eq( settings.getString("login").get() ) ) );

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

  public void update() {
    updateView();
  }



  @Override
  public void onButtonBuilderUpdate() {
    Timber.tag(TAG).i( "onButtonBuilderUpdate" );
    callback.onMenuUpdate( getConditions() );
  }

}

package sapotero.rxtest.views.menu.factories;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.events.service.UpdateDocumentsByStatusEvent;
import sapotero.rxtest.views.adapters.DocumentTypeAdapter;
import sapotero.rxtest.views.adapters.models.DocumentTypeItem;
import sapotero.rxtest.views.custom.OrganizationSpinner;
import sapotero.rxtest.views.menu.builders.ButtonBuilder;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import sapotero.rxtest.views.menu.fields.MainMenuButton;
import sapotero.rxtest.views.menu.fields.MainMenuItem;
import timber.log.Timber;

public class ItemsBuilder implements ButtonBuilder.Callback {

  @Inject RxSharedPreferences settings;

  private String TAG = this.getClass().getSimpleName();
  private final Context context;
  private RadioGroup view;
  private Spinner journalSpinner;
  private DocumentTypeAdapter journalSpinnerAdapter;
  private Callback callback;
  private LinearLayout organizationsLayout;
  private OrganizationSpinner organizationSelector;
  private CheckBox favoritesButton;
  private String user;
  private Integer index;


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
      @RequiresApi(api = Build.VERSION_CODES.M)
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

  public void setOrganizationSelector(OrganizationSpinner organizationSelector) {
    this.organizationSelector = organizationSelector;
  }

  public void setFavoritesButton(CheckBox favoritesButton) {
    this.favoritesButton = favoritesButton;
    this.favoritesButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
      Timber.tag("checkbox").i( "checked: %s", isChecked );
      onButtonBuilderUpdate(index);
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

  public RadioGroup getView() {
    return view;
  }


  @RequiresApi(api = Build.VERSION_CODES.M)
  private void updateView() {
    if (view == null){
      view = new RadioGroup(context);
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
      onButtonBuilderUpdate(index);
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
    if ( !mainMenuItem.isProcessed() ){
      Collections.addAll(result, MainMenuButton.getByIndex(index).getConditions() );
    }

    if ( mainMenuItem.getIndex() == 3 ){
      Collections.addAll(result, MainMenuButton.getByIndex(index).getConditions() );
    }

    Timber.tag(TAG).v( "onMenuUpdate: %s", result.size() );
    for (ConditionBuilder condition : result ) {
      Timber.tag(TAG).i("** %s", condition.toString());
    }


    Timber.tag(TAG).v( MainMenuButton.getByIndex(index).getFormat() );

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

  @RequiresApi(api = Build.VERSION_CODES.M)
  public void update() {
    updateView();
  }



  @Override
  public void onButtonBuilderUpdate(Integer index) {
    try {
      this.index = index;
      Timber.tag(TAG).i( "onButtonBuilderUpdate" );
      EventBus.getDefault().post( new UpdateDocumentsByStatusEvent( getSelectedItem(), MainMenuButton.getByIndex(index) ) );
      callback.onMenuUpdate( getConditions() );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}

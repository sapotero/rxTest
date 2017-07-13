package sapotero.rxtest.views.menu;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.util.ArrayList;

import sapotero.rxtest.R;
import sapotero.rxtest.views.custom.OrganizationSpinner;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import sapotero.rxtest.views.menu.factories.ItemsBuilder;
import sapotero.rxtest.views.menu.fields.MainMenuItem;
import timber.log.Timber;

public class MenuBuilder implements ItemsBuilder.Callback{
  private final ItemsBuilder itemsBuilder;
  private final Context context;

  private Callback callback;
  private FrameLayout view;
  private Spinner journalSpinner;
  private FrameLayout buttons;
  private LinearLayout organizations;

  private String TAG = this.getClass().getSimpleName();
  private OrganizationSpinner organizationsSelector;
  private CheckBox favorites;

  private String user;

  private ArrayList<ConditionBuilder> result;

  public ArrayList<ConditionBuilder> getResult() {
    return result;
  }

  public boolean isVisible() {
    return itemsBuilder.isVisible();
  }

  public void selectJournal(int type) {
    itemsBuilder.get(type);
  }


//  public void updateCount() {
//    itemsBuilder.getSelectedItem().recalcuate();
////    updateFromJob();
//  }

  public void invalidate() {
    Timber.tag(TAG).i("invalidate");
//    getItem().recalcuate();
//    itemsBuilder.invalidate();
  }


  public interface Callback {

    void onMenuBuilderUpdate(ArrayList<ConditionBuilder> view);
    void onUpdateError(Throwable error);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }


  public MainMenuItem getItem(){
    return itemsBuilder.getSelectedItem();
  }

  public MenuBuilder(Context context) {
    this.context = context;
    this.itemsBuilder = new ItemsBuilder( context );
    this.itemsBuilder.registerCallBack(this);
  }

  public ItemsBuilder getItemsBuilder() {
    return itemsBuilder;
  }

  public MenuBuilder withJournalSelector(Spinner selector) {
    journalSpinner = selector;
    return this;
  }

  public MenuBuilder withUser(String user) {
    this.user = user;
    return this;
  }


  public MenuBuilder withButtonsLayout(FrameLayout menu_builder_buttons) {
    buttons = menu_builder_buttons;
    return this;
  }

  public MenuBuilder withOrganizationLayout(LinearLayout menu_builder_organization) {
    this.organizations = menu_builder_organization;
    return this;
  }

  public MenuBuilder withOrganizationSelector(OrganizationSpinner organizationsSelector) {
    this.organizationsSelector = organizationsSelector;
    return this;
  }

  public MenuBuilder withFavoritesButton(CheckBox favorites_button) {
    favorites = favorites_button;
    return this;
  }



  public void build(){
    if ( journalSpinner != null ){

      if ( journalSpinner.getAdapter() == null){
        itemsBuilder.setSpinner(journalSpinner);
        itemsBuilder.setSpinnerDefaults();
      }

    }

    itemsBuilder.setFavoritesButton(favorites);
    itemsBuilder.setOrganizationSelector( organizationsSelector );
    itemsBuilder.setOrganizationsLayout( organizations );
    itemsBuilder.setUser( user );

  }

  public void update() {
//    //test updateFromJob adapter
//    int index = journalSpinner.getSelectedItemPosition();
//    DocumentTypeAdapter tmp_adapter = (DocumentTypeAdapter) journalSpinner.getAdapter();

  }

  public void showPrev() {
    organizationsSelector.clear();
    itemsBuilder.prev();
  }
  public void showNext() {
    organizationsSelector.clear();
    itemsBuilder.next();
  }

  public void setFavorites( int count ){
    favorites.setText(
      String.format( "%s %s", context.getString( R.string.favorites_template ), count )
    );
  }

  @Override
  public void onMenuUpdate( ArrayList<ConditionBuilder> result ) {

    this.result = result;

    view = new FrameLayout(context);
    RadioGroup tmp_view = itemsBuilder.getView();

    buttons.removeAllViews();
    buttons.addView( tmp_view );


    callback.onMenuBuilderUpdate( result );
  }


}

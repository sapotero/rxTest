package sapotero.rxtest.views.menu;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;

import sapotero.rxtest.R;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import sapotero.rxtest.views.menu.factories.ItemsBuilder;
import sapotero.rxtest.views.menu.fields.Item;
import sapotero.rxtest.views.views.MultiOrganizationSpinner;
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
  private MultiOrganizationSpinner organizationsSelector;
  private CheckBox favorites;

  public boolean isVisible() {
    return itemsBuilder.isVisible();
  }


  public interface Callback {

    void onMenuBuilderUpdate(ArrayList<ConditionBuilder> view);
    void onUpdateError(Throwable error);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }


  public Item getItem(){
    return itemsBuilder.getSelectedItem();
  }

  public MenuBuilder(Context context) {
    this.context = context;
    this.itemsBuilder = new ItemsBuilder( context );
    this.itemsBuilder.registerCallBack(this);
  }

  public MenuBuilder withJournalSelector(Spinner selector) {
    journalSpinner = selector;
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

  public MenuBuilder withOrganizationSelector(MultiOrganizationSpinner organizationsSelector) {
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

    Timber.tag(TAG).i( "onMenuUpdate" );

    view = new FrameLayout(context);

    buttons.removeAllViews();
    buttons.addView( itemsBuilder.getView() );




    callback.onMenuBuilderUpdate( result );
  }


}

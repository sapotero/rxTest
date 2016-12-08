package sapotero.rxtest.views.menu;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;

import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import sapotero.rxtest.views.menu.factories.ItemsBuilder;
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


  public interface Callback {

    void onMenuBuilderUpdate(ArrayList<ConditionBuilder> view);
    void onUpdateError(Throwable error);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
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

  public MenuBuilder withButtons(FrameLayout menu_builder_buttons) {
    buttons = menu_builder_buttons;
    return this;
  }

  public MenuBuilder withOrganization(LinearLayout menu_builder_organization) {
    organizations = menu_builder_organization;
    return this;
  }

  public MenuBuilder withOrganizationSelector(MultiOrganizationSpinner organizationsSelector) {
    organizationsSelector = organizationsSelector;
    return this;
  }

  public MenuBuilder withFavorites(CheckBox favorites_button) {
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
  }

  public void prev() {
    itemsBuilder.prev();
  }
  public void next() {
    itemsBuilder.next();
  }


  @Override
  public void onMenuUpdate( ArrayList<ConditionBuilder> result ) {

    Timber.tag(TAG).i( "onMenuUpdate" );

    view = new FrameLayout(context);


    buttons.removeAllViews();
    buttons.addView( itemsBuilder.getView() );


    for ( ConditionBuilder condition: result ) {
      Timber.tag(TAG).i( "++ %s", condition.toString() );
    }


    callback.onMenuBuilderUpdate( result );
  }


}

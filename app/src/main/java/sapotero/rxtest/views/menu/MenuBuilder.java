package sapotero.rxtest.views.menu;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.views.menu.factories.ItemsFactory;

public class MenuBuilder implements ItemsFactory.Callback{
  private final ItemsFactory items;
  private final Context context;

  private Callback callback;
  private FrameLayout view;
  private Spinner journalSpinner;
  private SingleEntityStore<Persistable> db;
  private FrameLayout buttons;
  private LinearLayout organizations;

  private String TAG = this.getClass().getSimpleName();

  public interface Callback {

    void onMenuBuilderUpdate(View view);
    void onUpdateError(Throwable error);
  }

  public void registerCallBack(Callback callback){
    this.callback = callback;
  }




  public MenuBuilder(Context context) {
    this.context = context;
    this.items = new ItemsFactory( context );
    this.items.registerCallBack(this);
  }

  public MenuBuilder withJournalSelector(Spinner selector) {
    journalSpinner = selector;
    return this;
  }

  public MenuBuilder withDB(SingleEntityStore<Persistable> dataStore) {
    db = dataStore;
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

  public void build(){
    if ( journalSpinner != null ){

      if ( journalSpinner.getAdapter() == null){
        items.setSpinner(journalSpinner);
        items.setSpinnerDefaults();
      }

    }
  }

  public void prev() {
    items.prev();
  }
  public void next() {
    items.next();
  }


  @Override
  public void onMenuUpdate() {
    view = new FrameLayout(context);


    buttons.removeAllViews();
    if ( items.getView() != null){
      buttons.addView( items.getView() );
    }


    callback.onMenuBuilderUpdate( view );
  }

}

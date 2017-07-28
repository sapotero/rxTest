package sapotero.rxtest.views.adapters.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import sapotero.rxtest.views.menu.fields.MainMenuItem;

public class FragmentPagerAdapterBuilder extends FragmentPagerAdapter {

  private final MainMenuItem item;

  FragmentPagerAdapterBuilder(FragmentManager fragmentManager, MainMenuItem item) {
    super(fragmentManager);
    this.item = item;
  }

  @Override
  public Fragment getItem(int position) {

    Fragment result = null;

    if ( item.getMainMenuButtons().size() >= position ){
      result = new FragmentBuilder()
        .setButton( item.getMainMenuButtons().get(position) )
        .build();
    }

    return result;
  }

  @Override
  public CharSequence getPageTitle(int position) {

    CharSequence result = null;

    if ( item.getMainMenuButtons().size() >= position ){
      result = item.getMainMenuButtons().get(position).getLabel();
    }

    return result;
  }

  @Override
  public int getCount() {
    return item.getMainMenuButtons().size();
  }
}

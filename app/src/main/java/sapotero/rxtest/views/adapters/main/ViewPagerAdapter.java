package sapotero.rxtest.views.adapters.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import sapotero.rxtest.views.menu.fields.MainMenuItem;

public class ViewPagerAdapter extends FragmentPagerAdapter {

  private FragmentManager fragmentManager;
  private MainMenuItem item;

  public ViewPagerAdapter(FragmentManager fragmentManager) {
    super(fragmentManager);
    this.fragmentManager = fragmentManager;
  }

  public ViewPagerAdapter setItem(MainMenuItem item) {
    this.item = item;
    return this;
  }

  public FragmentPagerAdapterBuilder build(){
    return new FragmentPagerAdapterBuilder(fragmentManager, item);
  }

  @Override
  public int getCount() {
    return item.getMainMenuButtons().size();
  }

  @Override
  public Fragment getItem(int position) {
    return null;
  }
}

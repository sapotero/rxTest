package sapotero.rxtest.views.adapters.main;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import sapotero.rxtest.views.menu.fields.MainMenuItem;

public class ViewPagerAdapter {

  private FragmentManager fragmentManager;
  private MainMenuItem item;

  public ViewPagerAdapter() {
  }

  public ViewPagerAdapter setFragmentManager(FragmentManager fragmentManager) {
    this.fragmentManager = fragmentManager;
    return this;
  }

  public ViewPagerAdapter setItem(MainMenuItem item) {
    this.item = item;
    return this;
  }

  public FragmentPagerAdapter build(){
    return new FragmentPagerAdapterBuilder(fragmentManager, item);
  }
}

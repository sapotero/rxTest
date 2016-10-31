package sapotero.rxtest.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import sapotero.rxtest.views.fragments.InfoCardDocumentsFragment;
import sapotero.rxtest.views.fragments.InfoCardWebViewFragment;

public class TabPagerAdapter extends FragmentPagerAdapter {
  private int tabs_count;

  public TabPagerAdapter(FragmentManager fragmentManager, int tabs_count) {
    super(fragmentManager);
    this.tabs_count = tabs_count;
  }

  @Override
  public Fragment getItem(int position) {

    switch (position) {
      case 0:
        return new InfoCardDocumentsFragment();
      case 1:
        return new InfoCardWebViewFragment();
      default:
        return null;
    }
  }

  @Override
  public int getCount() {
    return tabs_count;
  }
}
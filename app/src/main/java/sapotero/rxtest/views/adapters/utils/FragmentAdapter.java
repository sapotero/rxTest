package sapotero.rxtest.views.adapters.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import sapotero.rxtest.views.fragments.interfaces.PreviewUpdater;

public class FragmentAdapter extends FragmentPagerAdapter implements PreviewUpdater, Listable {

  public FragmentAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override
  public Fragment getItem(int position) {
    return null;
  }

  @Override
  public int getCount() {
    return 0;
  }

  @Override
  public void update() {
  }

  @Override
  public String getLabel() {
    return this.getClass().getSimpleName();
  }
}

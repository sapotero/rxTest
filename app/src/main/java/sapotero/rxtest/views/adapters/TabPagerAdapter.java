package sapotero.rxtest.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import sapotero.rxtest.views.fragments.InfoCardDocumentsFragment;
import sapotero.rxtest.views.fragments.InfoCardWebViewFragment;

public class TabPagerAdapter extends FragmentPagerAdapter {
  private String uid;
  private boolean zoom = false;
  private boolean doubleTapEnabled = true;

  public TabPagerAdapter(FragmentManager fragmentManager) {
    super(fragmentManager);
  }



  @Override
  public Fragment getItem(int position) {
    switch (position) {
      case 0:
        return new InfoCardDocumentsFragment().withUid(uid).withOutZoom(zoom);
      case 1:
        return new InfoCardWebViewFragment().withUid(uid).withEnableDoubleTap(doubleTapEnabled);
      default:
        return null;
    }
  }

  @Override
  public CharSequence getPageTitle(int position) {
    switch(position) {
      case 0:
        return "Документ";
      case 1:
        return "Инфокарточка";
    }
    return null;
  }

  @Override
  public int getCount() {
    return 2;
  }

  public void withUid(String uid) {
    this.uid = uid;
  }

  public void withoutZoom(boolean bool) {
    this.zoom = bool;
  }

  public void withEnableDoubleTap(boolean doubleTapEnabled) {
    this.doubleTapEnabled = doubleTapEnabled;
  }
}
package sapotero.rxtest.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import sapotero.rxtest.views.fragments.InfoCardDocumentsFragment;
import sapotero.rxtest.views.fragments.InfoCardWebViewFragment;

public class TabPagerAdapter extends FragmentPagerAdapter {
  private String uid;

  public TabPagerAdapter(FragmentManager fragmentManager) {
    super(fragmentManager);
  }

  @Override
  public Fragment getItem(int position) {
    switch (position) {
      case 0:
        return new InfoCardDocumentsFragment().withUid(uid);
      case 1:
        return new InfoCardWebViewFragment().withUid(uid);
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
}
package sapotero.rxtest.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import sapotero.rxtest.views.fragments.InfoCardDocumentsFragment;
import sapotero.rxtest.views.fragments.InfoCardFieldsFragment;
import sapotero.rxtest.views.fragments.InfoCardLinksFragment;

public class TabSigningPagerAdapter extends FragmentPagerAdapter {

  public TabSigningPagerAdapter(FragmentManager fragmentManager) {
    super(fragmentManager);
  }

  @Override
  public Fragment getItem(int position) {
    switch (position) {
      case 0:
        return new InfoCardDocumentsFragment();
      case 1:
        return new InfoCardFieldsFragment();
      case 2:
        return new InfoCardLinksFragment();
    }
    return null;
  }

  @Override
  public CharSequence getPageTitle(int position) {
    switch(position) {
      case 0:
        return "Документ";
      case 1:
        return "Поля документа";
      case 2:
        return "Связанные документы";
    }
    return null;
  }

  @Override
  public int getCount() {
    return 3;
  }


}
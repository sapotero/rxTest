package sapotero.rxtest.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import sapotero.rxtest.views.fragments.InfoCardDocumentsFragment;
import sapotero.rxtest.views.fragments.InfoCardFieldsFragment;
import sapotero.rxtest.views.fragments.InfoCardLinksFragment;

public class TabSigningPagerAdapter extends FragmentPagerAdapter {

  private String uid;
  private boolean withoutZoom  = false;
  private boolean withoutLinks = false;

  public TabSigningPagerAdapter(FragmentManager fragmentManager) {
    super(fragmentManager);
  }

  @Override
  public Fragment getItem(int position) {
    switch (position) {
      case 0:
        return new InfoCardDocumentsFragment().withUid(uid).withOutZoom(withoutZoom);
      case 1:
        return new InfoCardFieldsFragment().withUid(uid);
      case 2:
        return new InfoCardLinksFragment().withUid(uid);
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
    return withoutLinks ? 2 : 3;
  }

  public void withUid(String uid) {
    this.uid = uid;
  }

  public void withoutZoom(boolean bool) {
    this.withoutZoom = bool;
  }

  public void withoutLinks(boolean withoutLinks) {
    this.withoutLinks = withoutLinks;
  }
}
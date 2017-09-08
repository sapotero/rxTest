package sapotero.rxtest.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.HashMap;
import java.util.Map;

import sapotero.rxtest.views.adapters.utils.FragmentAdapter;
import sapotero.rxtest.views.fragments.InfoCardDocumentsFragment;
import sapotero.rxtest.views.fragments.InfoCardLinksFragment;
import sapotero.rxtest.views.fragments.InfoCardWebViewFragment;
import sapotero.rxtest.views.fragments.interfaces.PreviewFragment;

public class TabPagerAdapter extends FragmentAdapter {
  private final FragmentFactory factory;

  private String uid;
  private boolean zoom = false;
  private boolean doubleTapEnabled = true;
  private boolean withoutLinks = false;

  public TabPagerAdapter(FragmentManager fragmentManager) {
    super(fragmentManager);
    factory = new FragmentFactory();
  }

  private class FragmentFactory {
    Map<Integer, PreviewFragment> fragments = new HashMap<>();

    public PreviewFragment get(int position){
      PreviewFragment result = null;

      if ( fragments.containsKey(position) ) {
        fragments.get(position).update();
      } else {

        PreviewFragment fragment;
        switch (position) {
          case 0:
            fragment = new InfoCardDocumentsFragment().withUid(uid).withOutZoom(zoom);
            break;
          case 1:
            fragment = new InfoCardWebViewFragment().withUid(uid).withEnableDoubleTap(doubleTapEnabled);
            break;
          case 2:
            fragment = new InfoCardLinksFragment().withUid(uid);
            break;
          default:
            fragment = new InfoCardDocumentsFragment().withUid(uid).withOutZoom(zoom);
            break;
        }

        fragments.put(position, fragment);
        result = fragment;
      }
      return result;
    }

    public void update(int position){
      if ( fragments.containsKey(position) ) {
        fragments.get(position).update();
      }
    }

    void updateAll() {
      for (PreviewFragment fragment: fragments.values()) {
        fragment.update();
      }
    }
  }

  @Override
  public Fragment getItem(int position) {
    return factory.get(position);
  }

  @Override
  public CharSequence getPageTitle(int position) {
    switch(position) {
      case 0:
        return "Документ";
      case 1:
        return "Инфокарточка";
      case 2:
        return "Связанные документы";
    }
    return null;
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

  public void withoutLinks(boolean withoutLinks) {
    this.withoutLinks = withoutLinks;
  }

  @Override
  public int getCount() {
    return withoutLinks ? 2 : 3;
  }

  @Override
  public String getLabel() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void update() {
    factory.updateAll();
  }
}
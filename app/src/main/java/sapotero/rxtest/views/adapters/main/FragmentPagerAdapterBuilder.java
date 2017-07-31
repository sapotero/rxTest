package sapotero.rxtest.views.adapters.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import sapotero.rxtest.views.menu.builders.ButtonBuilder;
import sapotero.rxtest.views.menu.fields.MainMenuItem;

public class FragmentPagerAdapterBuilder extends FragmentPagerAdapter {

  private final MainMenuItem item;
  private ArrayList<FragmentBuilder> fragments;

  FragmentPagerAdapterBuilder(FragmentManager fragmentManager, MainMenuItem item) {
    super(fragmentManager);
    this.item = item;


    buildFragments();
  }

  public ArrayList<FragmentBuilder> getFragments() {
    return fragments;
  }

  private void buildFragments() {
    fragments = new ArrayList<>();

    if (item.getMainMenuButtons().size() > 0){
      for (ButtonBuilder button: item.getMainMenuButtons()) {
        fragments.add( new FragmentBuilder()
          .setButton( button )
          .build() );
      }
    }
  }

  @Override
  public Fragment getItem(int position) {

    Fragment result = null;

    if ( fragments.size() >= position ){
      result = fragments.get(position);
    }

    return result;
  }

  @Override
  public CharSequence getPageTitle(int position) {

    CharSequence result = null;

    if ( fragments.size() > position ){
      FragmentBuilder fragment = fragments.get(position);
      result = String.format( fragment.getButton().getLabel(), fragment.getCount() );
    }

    return result;
  }




  @Override
  public int getCount() {
    return item.getMainMenuButtons().size();
  }

  public void recalculate(){

  }
}

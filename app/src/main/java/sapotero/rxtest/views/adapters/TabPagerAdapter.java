package sapotero.rxtest.views.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import sapotero.rxtest.views.fragments.InfoCardFragment;

public class TabPagerAdapter extends FragmentPagerAdapter {
  final int PAGE_COUNT = 2;

  private String tabTitles[] = new String[] { "Документ", "Инфокарточка" };
  private Context context;

  public TabPagerAdapter(FragmentManager fm, Context context) {
    super(fm);
    this.context = context;
  }

  @Override
  public int getCount() {
    return PAGE_COUNT;
  }

  @Override
  public Fragment getItem(int position) {
    Log.d( "__position" , String.valueOf(position) );
    return InfoCardFragment.newInstance(position + 1);
  }

  @Override
  public CharSequence getPageTitle(int position) {
    Log.d( "__getPageTitle" , String.valueOf(position) );
    return tabTitles[position];
  }
}
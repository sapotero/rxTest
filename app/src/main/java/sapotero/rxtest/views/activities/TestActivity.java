package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.views.adapters.main.ViewPagerAdapter;
import sapotero.rxtest.views.menu.fields.MainMenuItem;

public class TestActivity extends AppCompatActivity {

  @Inject ISettings settings;
  @Inject MemoryStore store;

  @BindView(R.id.activity_test_tab_layout) TabLayout tabLayout;
  @BindView(R.id.activity_test_view_pager) ViewPager viewPager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);

    ButterKnife.bind(this);
    EsdApplication.getManagerComponent().inject(this);

    buildAdapter();

  }

  private void buildAdapter() {

    FragmentPagerAdapter adapters = new ViewPagerAdapter()
      .setFragmentManager( getSupportFragmentManager() )
      .setItem(MainMenuItem.ALL )
      .build();

    viewPager.setAdapter(adapters);

    viewPager.setOffscreenPageLimit(4);

    tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override
      public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem( tab.getPosition() );
      }

      @Override
      public void onTabUnselected(TabLayout.Tab tab) {
      }

      @Override
      public void onTabReselected(TabLayout.Tab tab) {
      }
    });


    tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
    tabLayout.setupWithViewPager(viewPager);
  }

}

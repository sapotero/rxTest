package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.views.adapters.main.FragmentBuilder;
import sapotero.rxtest.views.adapters.main.FragmentPagerAdapterBuilder;
import sapotero.rxtest.views.adapters.main.ViewPagerAdapter;
import sapotero.rxtest.views.menu.fields.MainMenuItem;
import timber.log.Timber;

public class TestActivity extends AppCompatActivity {

  @Inject ISettings settings;
  @Inject MemoryStore store;

  @BindView(R.id.activity_test_tab_layout) TabLayout tabLayout;
  @BindView(R.id.activity_test_view_pager) ViewPager viewPager;
  private FragmentPagerAdapterBuilder adapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);


    ButterKnife.bind(this);
    EsdApplication.getManagerComponent().inject(this);

    buildAdapter();
    update();
  }

  private void update() {
    Observable
      .interval( 1, TimeUnit.SECONDS )
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        data -> {
          Timber.e("+");
          if ( adapter.getCount() > 0 ) {

            for (int i = 0; i < adapter.getFragments().size(); i++) {
              FragmentBuilder fragment = adapter.getFragments().get(i);
              String result = String.format(fragment.getButton().getLabel(), fragment.getCount());
              tabLayout.getTabAt(i).setText( result );
            }

            tabLayout.setTabTextColors(
              getResources().getColor(R.color.md_grey_600),
              getResources().getColor(R.color.md_grey_800));

          }
        }, Timber::e
      );
  }

  private void buildAdapter() {

    adapter = new ViewPagerAdapter( getSupportFragmentManager() )
      .setItem(MainMenuItem.ALL )
      .build();

    viewPager.setAdapter(adapter);

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

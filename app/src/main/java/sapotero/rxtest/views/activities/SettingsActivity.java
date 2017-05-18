package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.views.fragments.SettingsViewFragment;


public class SettingsActivity extends AppCompatActivity {

  @BindView(R.id.toolbar) Toolbar toolbar;

  CompositeSubscription subscriptions;

  private String TAG = this.getClass().getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);


    ButterKnife.bind(this);

    toolbar.setTitle("Настройки приложения");
    toolbar.setSubtitle("доступ и отображение");
    toolbar.setTitleTextColor( getResources().getColor( R.color.md_grey_100 ) );
    toolbar.setSubtitleTextColor( getResources().getColor( R.color.md_grey_400 ) );
    toolbar.setNavigationOnClickListener(v ->{
        finish();
      }
    );

    toolbar.setContentInsetStartWithNavigation(250);

    if (savedInstanceState == null) {
      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
      fragmentTransaction.add(R.id.settings_view_fragment, new SettingsViewFragment());
//      fragmentTransaction.addByOne(R.id.settings_user_fragment, new SettingsMainFragment() );
    fragmentTransaction.commit();
    }

  }

  @Override protected void onResume() {
    super.onResume();
//    MenuItem item = menu.findItem(R.id.menu_my_item);
  }

  @Override protected void onPause() {
    super.onPause();
    if (subscriptions != null){
      subscriptions.unsubscribe();
    }
  }

}

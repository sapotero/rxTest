package sapotero.rxtest.views.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.CheckBox;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.jakewharton.rxbinding.widget.RxCompoundButton;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.views.fragments.SettingsUserFragment;
import sapotero.rxtest.views.fragments.SettingsViewFragment;
import timber.log.Timber;


public class SettingsActivity extends AppCompatActivity {

  @BindView(R.id.toolbar) Toolbar toolbar;

  @Inject RxSharedPreferences settings;

  Preference<Boolean> fooPreference;
  CompositeSubscription subscriptions;

  private String TAG = this.getClass().getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);


    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    fooPreference = settings.getBoolean("foo");

    toolbar.setTitleTextColor(Color.WHITE);
    toolbar.setTitle("Settings");
    toolbar.setNavigationOnClickListener(v ->{
      finish();
      }
    );

    if (savedInstanceState == null) {
      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
      fragmentTransaction.add(R.id.settings_user_fragment, new SettingsUserFragment() );
      fragmentTransaction.add(R.id.settings_view_fragment, new SettingsViewFragment());
    fragmentTransaction.commit();
    }
  }

  @Override protected void onResume() {
    super.onResume();

    subscriptions = new CompositeSubscription();
//    bindPreference(foo1Checkbox, fooPreference);
//    bindPreference(foo2Checkbox, fooPreference);

    Timber.tag(TAG).i( " settings_username_host - " + settings.getString("settings_username_host").get() );
  }

  @Override protected void onPause() {
    super.onPause();
    subscriptions.unsubscribe();
  }

  void bindPreference(CheckBox checkBox, Preference<Boolean> preference) {
    subscriptions.add(
      preference
        .asObservable()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(RxCompoundButton.checked(checkBox))
    );

    subscriptions.add(
      RxCompoundButton
        .checkedChanges(checkBox)
        .skip(1)
        .subscribe(preference.asAction())
    );
  }
}

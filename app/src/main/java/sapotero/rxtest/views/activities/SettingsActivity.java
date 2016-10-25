package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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


public class SettingsActivity extends AppCompatActivity {

  @BindView(R.id.foo_1) CheckBox foo1Checkbox;
  @BindView(R.id.foo_2) CheckBox foo2Checkbox;

  Preference<Boolean> fooPreference;
  CompositeSubscription subscriptions;

  @Inject RxSharedPreferences settings;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    ButterKnife.bind(this);

    fooPreference = settings.getBoolean("foo", false);
  }

  @Override protected void onResume() {
    super.onResume();

    subscriptions = new CompositeSubscription();
    bindPreference(foo1Checkbox, fooPreference);
    bindPreference(foo2Checkbox, fooPreference);
  }

  @Override protected void onPause() {
    super.onPause();
    subscriptions.unsubscribe();
  }

  void bindPreference(CheckBox checkBox, Preference<Boolean> preference) {
    subscriptions.add(preference.asObservable()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(RxCompoundButton.checked(checkBox)));
    subscriptions.add(RxCompoundButton.checkedChanges(checkBox)
      .skip(1)
      .subscribe(preference.asAction()));
  }
}

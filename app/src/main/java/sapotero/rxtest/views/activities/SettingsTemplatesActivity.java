package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;

public class SettingsTemplatesActivity extends AppCompatActivity {

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.settings_templates_decisions_list) ListView decisions_list;

  @Inject RxSharedPreferences settings;

  private String TAG = this.getClass().getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings_templates);


    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    toolbar.setTitle("Настройки приложения");
    toolbar.setSubtitle("шаблоны резолюции");
    toolbar.setTitleTextColor( getResources().getColor( R.color.md_grey_100 ) );
    toolbar.setSubtitleTextColor( getResources().getColor( R.color.md_grey_400 ) );
    toolbar.setNavigationOnClickListener(v ->{
        finish();
      }
    );

    toolbar.setContentInsetStartWithNavigation(250);
  }


  @Override protected void onResume() {
    super.onResume();

    Preference<Set<String>> templates = settings.getStringSet("settings_templates_decisions");
    Set<String> data = templates.get();

    assert data != null;
    if ( data.size() == 0 ){
      String[] array = getResources().getStringArray(R.array.settings_templates_decisions);

      List<String> list = Arrays.asList(array);
      Set<String> set = new HashSet<>(list);

      templates.set( set );
    }

    List<String> template_list = new ArrayList( templates.get() );
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, template_list);
    decisions_list.setAdapter(adapter);

  }

  @Override protected void onPause() {
    super.onPause();
  }

}

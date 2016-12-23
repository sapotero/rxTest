package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RTemplateEntity;

public class SettingsTemplatesActivity extends AppCompatActivity {

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.settings_templates_decisions_list) ListView decisions_list;

  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  private String TAG = this.getClass().getSimpleName();
  private AutoCompleteTextView list;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings_templates);


    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    toolbar.setTitle("Настройки приложения");
    toolbar.setSubtitle("шаблоны резолюции");
    toolbar.setTitleTextColor(getResources().getColor(R.color.md_grey_100));
    toolbar.setSubtitleTextColor(getResources().getColor(R.color.md_grey_400));
    toolbar.setNavigationOnClickListener(v -> {
        finish();
      }
    );

    toolbar.setContentInsetStartWithNavigation(250);
  }


  @Override protected void onResume() {
    super.onResume();

      ArrayList<String> items = new ArrayList<>();


      List<RTemplateEntity> templates = dataStore
        .select(RTemplateEntity.class)
        .get().toList();

      if (templates.size() > 0) {
        for (RTemplateEntity tmp : templates){
          items.add( tmp.getTitle() );
        }
      }

    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_black_text, items);
    decisions_list.setAdapter(adapter);

  }

  @Override protected void onPause() {
    super.onPause();
  }

}

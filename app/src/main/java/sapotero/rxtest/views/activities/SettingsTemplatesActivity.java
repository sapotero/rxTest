package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.views.fragments.DecisionRejectionTemplateFragment;
import sapotero.rxtest.views.fragments.DecisionTemplateFragment;
import sapotero.rxtest.views.fragments.dummy.DummyContent;

public class SettingsTemplatesActivity extends AppCompatActivity implements DecisionTemplateFragment.OnListFragmentInteractionListener, DecisionRejectionTemplateFragment.OnListFragmentInteractionListener {

  @BindView(R.id.activity_settings_content_wrapper) LinearLayout wrapper;
  @BindView(R.id.toolbar) Toolbar toolbar;

  private String TAG = this.getClass().getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings_templates);

    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);



    initToolBar();

    initFragments();

  }

  private void initFragments() {
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

    fragmentTransaction.add( R.id.activity_settings_content_wrapper, new DecisionTemplateFragment() );
    fragmentTransaction.add( R.id.activity_settings_content_wrapper, new DecisionRejectionTemplateFragment() );

    fragmentTransaction.commit();
  }

  private void initToolBar() {
    toolbar.setTitle("Настройки приложения");
    toolbar.setSubtitle("Шаблоны резолюции/отклонения");
    toolbar.setContentInsetStartWithNavigation(250);

    toolbar.setTitleTextColor( ContextCompat.getColor(this, R.color.md_grey_100) );
    toolbar.setSubtitleTextColor( ContextCompat.getColor(this, R.color.md_grey_400) );

    toolbar.setNavigationOnClickListener(v -> {
      finish();
      }
    );
  }


  @Override protected void onResume() {
    super.onResume();
//
//      ArrayList<String> items = new ArrayList<>();
//
//
//      List<RTemplateEntity> templates = dataStore
//        .select(RTemplateEntity.class)
//        .get().toList();
//
//      if (templates.size() > 0) {
//        for (RTemplateEntity tmp : templates){
//          items.add( tmp.getTitle() );
//        }
//      }
//
//    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_black_text, items);
//    decisions_list.setAdapter(adapter);

  }

  @Override protected void onPause() {
    super.onPause();
  }

  @Override
  public void onListFragmentInteraction(DummyContent.DummyItem item) {

  }
}

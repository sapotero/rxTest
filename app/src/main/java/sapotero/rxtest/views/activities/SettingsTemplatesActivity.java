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
import sapotero.rxtest.db.requery.models.RTemplateEntity;
import sapotero.rxtest.views.fragments.DecisionRejectionTemplateFragment;
import sapotero.rxtest.views.fragments.DecisionTemplateFragment;

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

  }

  @Override protected void onPause() {
    super.onPause();
  }

  @Override
  public void onListFragmentInteraction(RTemplateEntity item) {

  }
}

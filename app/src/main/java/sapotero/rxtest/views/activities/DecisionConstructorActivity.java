package sapotero.rxtest.views.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.views.adapters.models.UrgencyItem;
import sapotero.rxtest.views.fragments.DecisionFragment;
import sapotero.rxtest.views.views.EsdSelectView;
import timber.log.Timber;

public class DecisionConstructorActivity extends AppCompatActivity implements DecisionFragment.OnFragmentInteractionListener {

  @Inject RxSharedPreferences settings;

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.button_add_decision) Button button_add_decision;

  @BindView(R.id.signup_text_input_job_category) EsdSelectView<UrgencyItem> editTextJobCategory;

  private String TAG = this.getClass().getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_decision_constructor);

    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    toolbar.setTitle("Редактор резолюции");
    toolbar.inflateMenu(R.menu.info_decision_constructor);
    toolbar.setNavigationOnClickListener(v ->{
        finish();
      }
    );

    if (savedInstanceState == null) {
      FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
      fragmentTransaction.add(R.id.decisions_container, new DecisionFragment() );
      fragmentTransaction.commit();
    }

    List<UrgencyItem> urgency = new ArrayList<>();

    urgency.add( new UrgencyItem( "Весьма срочно"    , "Весьма срочно" ) );
    urgency.add( new UrgencyItem( "Крайне срочно"    , "Крайне срочно" ) );
    urgency.add( new UrgencyItem( "Няшная срочность" , "Няшная срочность" ) );
    urgency.add( new UrgencyItem( "Очень срочно"     , "Очень срочно" ) );
    urgency.add( new UrgencyItem( "Срочно"           , "Срочно" ) );

    editTextJobCategory.setItems(urgency);
    editTextJobCategory.setOnItemSelectedListener(
      (item, selectedIndex) -> Timber.tag(TAG).v(String.valueOf(item))
    );


  }

  @OnClick(R.id.button_add_decision)
  public void submit(View view) {
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.add(R.id.decisions_container, new DecisionFragment() );
    fragmentTransaction.commit();
  }

  @Override
  public void onFragmentInteraction(Uri uri) {

  }
}

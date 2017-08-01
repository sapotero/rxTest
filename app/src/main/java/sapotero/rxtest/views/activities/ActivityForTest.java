package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import sapotero.rxtest.R;

public class ActivityForTest extends AppCompatActivity {

  @BindView(R.id.testactivity_view_container) FrameLayout viewContainer;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_for_test);

    ButterKnife.bind(this);
  }

  public void addView(int viewLayoutId) {
    View.inflate(this, viewLayoutId, viewContainer);
  }

  public void addView(View view) {
    viewContainer.addView(view);
  }

  public void addFragment(Fragment fragment) {
    FragmentManager fm = getSupportFragmentManager();
    Fragment fragmentExisting = fm.findFragmentById(R.id.testactivity_view_container);

    if ( fragmentExisting == null ) {
      fm.beginTransaction()
        .add(R.id.testactivity_view_container, fragment)
        .commit();

    } else {
      fm.beginTransaction()
        .replace(R.id.testactivity_view_container, fragment)
        .commit();
    }
  }
}

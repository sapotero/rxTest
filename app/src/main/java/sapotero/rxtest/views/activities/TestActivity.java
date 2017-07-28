package sapotero.rxtest.views.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import javax.inject.Inject;

import butterknife.ButterKnife;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.MemoryStore;

public class TestActivity extends AppCompatActivity {

  @Inject ISettings settings;
  @Inject MemoryStore store;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);

    ButterKnife.bind(this);
    EsdApplication.getManagerComponent().inject(this);

  }
}

package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

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
import sapotero.rxtest.db.requery.models.queue.FileSignEntity;
import sapotero.rxtest.views.adapters.FileSignAdapter;
import sapotero.rxtest.views.adapters.decorators.GridSpacingItemDecoration;
import timber.log.Timber;

public class FileSignActivity extends AppCompatActivity {

  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  @BindView(R.id.activity_file_sign_toolbar) Toolbar toolbar;
  @BindView(R.id.activity_file_sign_recycle_view) RecyclerView recyclerView;

  private FileSignAdapter adapter;
  private String TAG = this.getClass().getSimpleName();


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_file_sign);

    ButterKnife.bind(this);
    EsdApplication.getComponent().inject(this);


    populateView();
  }

  private void populateView() {

    toolbar.setContentInsetStartWithNavigation(250);
    toolbar.setTitle("ЭЦП ЭО");
    toolbar.setNavigationOnClickListener(v ->{
        finish();
      }
    );

    int columnCount = 3;
    int spacing = 32;


    List<FileSignEntity> empty_list = new ArrayList<FileSignEntity>();
    adapter = new FileSignAdapter(this, empty_list );
    GridLayoutManager gridLayoutManager = new GridLayoutManager(this, columnCount, GridLayoutManager.VERTICAL, false);

    recyclerView.addItemDecoration(new GridSpacingItemDecoration(columnCount, spacing, true));
    recyclerView.setLayoutManager(gridLayoutManager);

    recyclerView.setAdapter(adapter);

    updateAdapter();

  }

  private void updateAdapter() {

    List<FileSignEntity> signs = dataStore
      .select(FileSignEntity.class)
      .get()
      .toList();

    Timber.tag(TAG).e("task list size: %s", signs.size());

    adapter.add(signs);

  }
}

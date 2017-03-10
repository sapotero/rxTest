package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.queue.QueueEntity;
import sapotero.rxtest.views.adapters.LogAdapter;
import sapotero.rxtest.views.adapters.decorators.GridSpacingItemDecoration;
import timber.log.Timber;

public class LogActivity extends AppCompatActivity {


  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  @BindView(R.id.activity_log_recycle_view) RecyclerView recyclerView;
  @BindView(R.id.activity_log_toolbar) Toolbar toolbar;
  private LogAdapter adapter;
  private String TAG = this.getClass().getSimpleName();


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_log);
    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    populateView();
  }

  private void populateView() {

    toolbar.setContentInsetStartWithNavigation(250);
    toolbar.setTitle("Просмотр отложенных задач");
    toolbar.setNavigationOnClickListener(v ->{
        finish();
      }
    );

    List<QueueEntity> tasks = dataStore
      .select(QueueEntity.class)
      .where(QueueEntity.UUID.ne(""))
      .get()
      .toList();

    Timber.tag(TAG).e("task list size: %s", tasks.size());

    adapter = new LogAdapter(this, tasks);

    int columnCount = 1;
    int spacing = 0;

    GridLayoutManager gridLayoutManager = new GridLayoutManager(this, columnCount, GridLayoutManager.VERTICAL, false);

    recyclerView.addItemDecoration(new GridSpacingItemDecoration(columnCount, spacing, true));
    recyclerView.setLayoutManager(gridLayoutManager);
    recyclerView.setAdapter(adapter);

  }
}

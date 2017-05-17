package sapotero.rxtest.views.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.queue.QueueEntity;
import sapotero.rxtest.views.adapters.LogAdapter;
import sapotero.rxtest.views.adapters.decorators.DividerItemDecoration;
import sapotero.rxtest.views.adapters.decorators.GridSpacingItemDecoration;
import timber.log.Timber;

public class LogActivity extends AppCompatActivity {

  @Inject SingleEntityStore<Persistable> dataStore;

  @BindView(R.id.activity_log_recycle_view) RecyclerView recyclerView;
  @BindView(R.id.activity_log_toolbar) Toolbar toolbar;
  @BindView(R.id.activity_reload_table) ImageView reload;

  private LogAdapter adapter;
  private String TAG = this.getClass().getSimpleName();


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_log);
    ButterKnife.bind(this);
    EsdApplication.getDataComponent().inject(this);

  }
  @OnClick(R.id.activity_reload_table)
  public void reload(){
    updateAdapter();
  }

  @Override
  protected void onResume() {
    super.onResume();
    populateView();
  }

  private void populateView() {

    toolbar.setContentInsetStartWithNavigation(250);
    toolbar.setTitle("Просмотр отложенных задач");
    toolbar.setNavigationOnClickListener(v ->{
        finish();
      }
    );
    int columnCount = 1;
    int spacing = 0;


    List<QueueEntity> empty_list = new ArrayList<QueueEntity>();
    adapter = new LogAdapter(this, empty_list );
    GridLayoutManager gridLayoutManager = new GridLayoutManager(this, columnCount, GridLayoutManager.VERTICAL, false);

    recyclerView.addItemDecoration(new GridSpacingItemDecoration(columnCount, spacing, true));
    recyclerView.setLayoutManager(gridLayoutManager);
    recyclerView.addItemDecoration(new DividerItemDecoration( getDrawable(R.drawable.devider) ));

    recyclerView.setAdapter(adapter);

    updateAdapter();

  }

  private void updateAdapter() {

    List<QueueEntity> tasks = dataStore
      .select(QueueEntity.class)
      .get()
      .toList();

    Timber.tag(TAG).e("task list size: %s", tasks.size());

    adapter.add(tasks);

  }
}

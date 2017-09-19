package sapotero.rxtest.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.birbit.android.jobqueue.JobManager;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RRouteEntity;
import sapotero.rxtest.db.requery.utils.JournalStatus;
import sapotero.rxtest.jobs.bus.UpdateDocumentJob;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.views.adapters.TabPagerAdapter;
import sapotero.rxtest.views.adapters.TabSigningPagerAdapter;
import sapotero.rxtest.views.fragments.InfoActivityDecisionPreviewFragment;
import sapotero.rxtest.views.fragments.RoutePreviewFragment;
import timber.log.Timber;

public class InfoNoMenuActivity extends AppCompatActivity {

  @BindView(R.id.activity_info_preview_container) LinearLayout preview_container;
  @BindView(R.id.frame_preview_decision) FrameLayout frame;
  @BindView(R.id.tab_main) ViewPager viewPager;
  @BindView(R.id.tabs) TabLayout tabLayout;
  @BindView(R.id.toolbar) Toolbar toolbar;

  @Inject JobManager jobManager;
  @Inject ISettings settings;
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject MemoryStore store;

  private String TAG = this.getClass().getSimpleName();
  private JournalStatus status;

  private String UID;
  private RDocumentEntity doc;

  private boolean showInfoCard = false;
  private boolean isProject = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_info);
    ButterKnife.bind(this);

    EsdApplication.getManagerComponent().inject(this);

    Intent intent = getIntent();
    if ( intent != null ){
      UID = getIntent().getExtras().getString("UID");

      try {
        showInfoCard = getIntent().getExtras().getBoolean("CARD");
      } catch (Exception e) {
        e.printStackTrace();
      }

      doc = dataStore
        .select(RDocumentEntity.class)
        .where(RDocumentEntity.UID.eq(UID)).get().firstOrNull();

      isProject = doc.getRoute() != null && ((RRouteEntity) doc.getRoute()).getSteps() != null && ((RRouteEntity) doc.getRoute()).getSteps().size() > 0;
    }

    setToolbar();

    setPreview();
    setTabContent();
    disableArrows();
  }

  private void disableArrows() {
    ImageButton prev = (ImageButton) findViewById(R.id.activity_info_prev_document);
    ImageButton next = (ImageButton) findViewById(R.id.activity_info_next_document);

    prev.setClickable(false);
    next.setClickable(false);
    prev.setAlpha(0.5f);
    next.setAlpha(0.5f);
  }

  private void setPreview() {
    addLoader();

    android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.addToBackStack("PREVIEW");

    if ( status == JournalStatus.SIGNING || status == JournalStatus.APPROVAL || isProject ) {
      fragmentTransaction.replace( R.id.activity_info_preview_container, new RoutePreviewFragment().withUid(UID), "PREVIEW" );
    } else {
      fragmentTransaction.replace( R.id.activity_info_preview_container, new InfoActivityDecisionPreviewFragment().withUid(UID).withEnableButtons(false), "PREVIEW" );
    }

    fragmentTransaction.commit();
  }

  private void addLoader() {
    preview_container.removeAllViews();

    frame.setVisibility(View.VISIBLE);

    int durationMillis = 300;

    Animation fadeIn = new AlphaAnimation(0, 1);
    fadeIn.setInterpolator(new DecelerateInterpolator());
    fadeIn.setDuration(durationMillis);

    Animation fadeOut = new AlphaAnimation(1, 0);
    fadeOut.setInterpolator(new AccelerateInterpolator());
    fadeOut.setStartOffset(durationMillis);
    fadeOut.setDuration(durationMillis);

    AnimationSet animation = new AnimationSet(true);
    AnimationSet wrapperAnimation = new AnimationSet(true);

    wrapperAnimation.addAnimation(fadeIn);
    animation.addAnimation(fadeOut);

    frame.setAnimation(animation);
    preview_container.setAnimation(wrapperAnimation);

    Observable.just("")
      .delay(durationMillis, TimeUnit.MILLISECONDS)
      .subscribeOn( Schedulers.newThread() )
      .observeOn( AndroidSchedulers.mainThread() )
      .subscribe(
        data  -> {
          frame.setVisibility(View.GONE);
        },
        Timber::e
      );
  }

  private void setToolbar() {
    toolbar.setTitleTextColor( getResources().getColor( R.color.md_grey_100 ) );
    toolbar.setSubtitleTextColor( getResources().getColor( R.color.md_grey_400 ) );

    toolbar.setContentInsetStartWithNavigation(250);

    toolbar.setNavigationOnClickListener(v ->{
      closeActivity();
      }
    );

    String _filter = doc.getFilter() != null ? doc.getFilter() : "";

    status = JournalStatus.getByName( _filter  );

    toolbar.setTitle( String.format("%s от %s", doc.getRegistrationNumber(), doc.getRegistrationDate() ) );
  }

  private void closeActivity() {
    settings.setImageIndex(0);
    finish();
  }

  private void setTabContent() {
    if (viewPager.getAdapter() == null) {
      if ( showInfoCard ){
        // для перехода по ссылкам из блока Согласование письмами
        TabPagerAdapter adapter = new TabPagerAdapter ( getSupportFragmentManager() );
        adapter.withUid(UID);
        adapter.withoutZoom(true);
        adapter.withEnableDoubleTap(false);
        viewPager.setAdapter(adapter);

      } else{
        if ( status == JournalStatus.SIGNING || status == JournalStatus.APPROVAL || isProject ) {
          TabSigningPagerAdapter adapter = new TabSigningPagerAdapter( getSupportFragmentManager() );
          adapter.withUid(UID);
          adapter.withoutZoom(true);
          adapter.withoutLinks(true);

          viewPager.setAdapter(adapter);

        } else {
          TabPagerAdapter adapter = new TabPagerAdapter ( getSupportFragmentManager() );
          adapter.withUid(UID);
          adapter.withoutZoom(true);
          adapter.withEnableDoubleTap(false);
          adapter.withoutLinks(true);
          viewPager.setAdapter(adapter);
        }
      }

      viewPager.setOffscreenPageLimit(10);

      tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
          viewPager.setCurrentItem( tab.getPosition() );
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
        }
      });
    }

    tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
    tabLayout.setupWithViewPager(viewPager);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    Timber.tag(TAG).i(String.valueOf(menu));
    return false;
  }

  @Override
  public void onStart() {
    super.onStart();

    if ( !EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().register(this);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if ( EventBus.getDefault().isRegistered(this) ){
      EventBus.getDefault().unregister(this);
    }

    finish();
  }

  @Override
  protected void onResume() {
    super.onResume();

    if ( doc != null && doc.isFromLinks() != null && doc.isFromLinks() ) {
      updateDocument();
    }
  }

  private void updateDocument() {
    jobManager.addJobInBackground( new UpdateDocumentJob( UID, true, settings.getLogin(), settings.getCurrentUserId() ) );
  }

  @Override
  public void onBackPressed() {
    closeActivity();
  }
}

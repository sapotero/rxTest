package sapotero.rxtest.views.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.birbit.android.jobqueue.JobManager;
import com.f2prateek.rx.preferences.Preference;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.requery.Persistable;
import io.requery.query.Scalar;
import io.requery.query.Update;
import io.requery.rx.SingleEntityStore;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.R;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.models.RFolderEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.events.bus.MassInsertDoneEvent;
import sapotero.rxtest.events.crypto.SignDataResultEvent;
import sapotero.rxtest.events.crypto.SignDataWrongPinEvent;
import sapotero.rxtest.retrofit.models.Oshs;
import sapotero.rxtest.utils.queue.QueueManager;
import sapotero.rxtest.views.adapters.TabPagerAdapter;
import sapotero.rxtest.views.adapters.TabSigningPagerAdapter;
import sapotero.rxtest.views.dialogs.SelectOshsDialogFragment;
import sapotero.rxtest.views.fragments.DecisionPreviewFragment;
import sapotero.rxtest.views.fragments.InfoActivityDecisionPreviewFragment;
import sapotero.rxtest.views.fragments.InfoCardDocumentsFragment;
import sapotero.rxtest.views.fragments.InfoCardFieldsFragment;
import sapotero.rxtest.views.fragments.InfoCardLinksFragment;
import sapotero.rxtest.views.fragments.InfoCardWebViewFragment;
import sapotero.rxtest.views.fragments.RoutePreviewFragment;
import sapotero.rxtest.views.managers.menu.OperationManager;
import sapotero.rxtest.views.managers.menu.factories.CommandFactory;
import sapotero.rxtest.views.managers.menu.utils.CommandParams;
import timber.log.Timber;

public class InfoActivity extends AppCompatActivity implements InfoActivityDecisionPreviewFragment.OnFragmentInteractionListener, DecisionPreviewFragment.OnFragmentInteractionListener, RoutePreviewFragment.OnFragmentInteractionListener, InfoCardDocumentsFragment.OnFragmentInteractionListener, InfoCardWebViewFragment.OnFragmentInteractionListener, InfoCardLinksFragment.OnFragmentInteractionListener, InfoCardFieldsFragment.OnFragmentInteractionListener, OperationManager.Callback, SelectOshsDialogFragment.Callback {


  @BindView(R.id.activity_info_preview_container) LinearLayout preview_container;

  @BindView(R.id.tab_main) ViewPager viewPager;
  @BindView(R.id.tabs) TabLayout tabLayout;


  @Inject JobManager jobManager;
  @Inject CompositeSubscription subscriptions;
  @Inject OkHttpClient okHttpClient;
  @Inject RxSharedPreferences settings;
  @Inject SingleEntityStore<Persistable> dataStore;

  // test
  @Inject QueueManager queue;
  @Inject OperationManager operationManager;

  private byte[] CARD;

  private Preference<String> TOKEN;
  private Preference<String> LOGIN;
  private Preference<String> PASSWORD;
  private Preference<String> UID;
  private Preference<String> DOCUMENT_UID;
  private Preference<String> STATUS_CODE;
  private Preference<Integer> POSITION;
  private Preference<String> REG_NUMBER;
  private Preference<String> REG_DATE;

//  private InterfaceDocumentManager documentManager;
  private String TAG = this.getClass().getSimpleName();

  @BindView(R.id.toolbar) Toolbar toolbar;

  private Fields.Status status;
  private Fields.Journal journal;
  private SelectOshsDialogFragment oshs;

  private Menu menu;
  private MaterialDialog dialog;
  private String SIGN;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    setTheme(R.style.AppTheme);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_info);
    ButterKnife.bind(this);
    EsdApplication.getComponent(this).inject(this);

    operationManager.registerCallBack(this);

    loadSettings();
    setToolbar();

    setPreview();
    setTabContent();

  }


  private void setPreview() {

    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

    if ( status == Fields.Status.SIGNING || status == Fields.Status.APPROVAL ){
      fragmentTransaction.add( R.id.activity_info_preview_container, new RoutePreviewFragment() );
    } else {
      fragmentTransaction.add( R.id.activity_info_preview_container, new InfoActivityDecisionPreviewFragment() );
    }

    fragmentTransaction.commit();
  }

  private void setToolbar() {

    toolbar.setTitleTextColor( getResources().getColor( R.color.md_grey_100 ) );
    toolbar.setSubtitleTextColor( getResources().getColor( R.color.md_grey_400 ) );

    toolbar.setContentInsetStartWithNavigation(250);

    toolbar.setNavigationOnClickListener(v ->{
      finish();
      }
    );


    // sent_to_the_report (отправлен на доклад)
    // sent_to_the_performance (отправлен на исполнение)
    // primary_consideration (первичное рассмотрение)
    // approval (согласование проектов документов)
    // signing (подписание проектов документов)

    int menu;

    switch ( STATUS_CODE.get() ){
      case "sent_to_the_report":
        menu = R.menu.info_menu_sent_to_the_report;
        break;
      case "sent_to_the_performance":
        menu = R.menu.info_menu_sent_to_the_performance;
        break;
      case "primary_consideration":
        menu = R.menu.info_menu_primary_consideration;
        break;
      case "approval":
        menu = R.menu.info_menu_approval;
        break;
      case "signing":
        menu = R.menu.info_menu_signing;
        break;
      default:
        menu = R.menu.info_menu;
        break;
    }
    toolbar.inflateMenu(menu);


    status  = Fields.Status.findStatus(STATUS_CODE.get());
    journal = Fields.getJournalByUid( UID.get() );

    toolbar.setTitle( String.format("%s от %s", REG_NUMBER.get(), REG_DATE.get()) );

    Timber.tag("MENU").e( "STATUS CODE: %s", STATUS_CODE.get() );



//    RDocumentEntity doc = dataStore
//      .select(RDocumentEntity.class)
//      .where(RDocumentEntity.UID.eq(UID.get())).get().first();
//
//
//    for (int i = 0; i < toolbar.getMenu().size(); i++) {
//      MenuItem item = toolbar.getMenu().getItem(i);
//
//      switch ( item.getItemId() ) {
//        case R.id.menu_info_shared_to_favorites:
//          item.setTitle(getString( doc.isFavorites() != null && doc.isFavorites() ? R.string.remove_from_favorites : R.string.to_favorites));
//          break;
//        case R.id.menu_info_shared_to_control:
//          item.setTitle(getString( doc.isControl() != null && doc.isControl() ? R.string.remove_from_control : R.string.to_control));
//          break;
//      }
//    }



  }


  private void setTabContent() {

    if (viewPager.getAdapter() == null) {

      Timber.tag(TAG).e("setTabContent %s", "%" + Fields.Journal.CITIZEN_REQUESTS.getValue() );

      dataStore
        .select(RFolderEntity.class)
        .get().toObservable()
        .observeOn(Schedulers.io())
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe( folder -> {
          Timber.e( "%s - %s ", folder.getType(), folder.getTitle() );
        });

      if ( status == Fields.Status.SIGNING || status == Fields.Status.APPROVAL ){
        TabSigningPagerAdapter adapter = new TabSigningPagerAdapter( getSupportFragmentManager() );
        viewPager.setAdapter(adapter);
      } else {
        TabPagerAdapter adapter = new TabPagerAdapter ( getSupportFragmentManager() );
        viewPager.setAdapter(adapter);
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


  private void loadSettings() {
    LOGIN    = settings.getString("login");
    UID      = settings.getString("main_menu.uid");
    PASSWORD = settings.getString("password");
    TOKEN    = settings.getString("token");
    POSITION = settings.getInteger("position");
    DOCUMENT_UID = settings.getString("document.uid");
    STATUS_CODE = settings.getString("main_menu.start");
    REG_NUMBER = settings.getString("main_menu.regnumber");
    REG_DATE = settings.getString("main_menu.date");

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

    if ( subscriptions != null && subscriptions.hasSubscriptions() ){
      subscriptions.unsubscribe();
    }

    finish();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(MassInsertDoneEvent event) {
    Toast.makeText( getApplicationContext(), event.message, Toast.LENGTH_SHORT).show();
  }


  @Override
  public void onFragmentInteraction(Uri uri) {
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    EventBus.getDefault().register(this);

  }

  /* OperationManager.Callback */
  @Override
  public void onExecuteSuccess(String command) {
    Timber.tag("OpManagerCallback").i("onExecuteSuccess %s", command);

    Update<Scalar<Integer>> query;

    query = dataStore
      .update(RDocumentEntity.class)
      .set(RDocumentEntity.PROCESSED, true)
      .set(RDocumentEntity.FILTER, "processed");

    if ( Objects.equals(command, "add_to_folder") ) {
      Boolean result = true;

      MenuItem item = toolbar.getMenu().findItem(R.id.menu_info_shared_to_favorites);

      if ( item.getTitle() == getString(R.string.to_favorites) ){
        item.setTitle( getString(R.string.remove_from_favorites) );
      } else {
        item.setTitle( getString(R.string.to_favorites) );
        result = false;
      }

      query = query.set( RDocumentEntity.FAVORITES, result );
    }

    if ( Objects.equals(command, "check_for_control") ) {
     Boolean result = true;
     MenuItem item = toolbar.getMenu().findItem(R.id.menu_info_shared_to_control);

      if ( item.getTitle() == getString(R.string.to_control) ){
        item.setTitle( getString(R.string.remove_from_control) );
      } else {
        item.setTitle( getString(R.string.to_control) );
        result = false;
      }

      query = query.set( RDocumentEntity.CONTROL, result );
    }

    if ( Objects.equals(command, "change_person") ) {
      Toast.makeText( getApplicationContext(), "Операция передачи успешно завершена", Toast.LENGTH_SHORT).show();
      toolbar.getMenu().clear();
      toolbar.inflateMenu(R.menu.info_menu);
    }

    if ( Objects.equals(command, "next_person") ) {
      Toast.makeText( getApplicationContext(), "Операция подписания успешно завершена", Toast.LENGTH_SHORT).show();
      toolbar.getMenu().clear();
      toolbar.inflateMenu(R.menu.info_menu);
    }

    if ( Objects.equals(command, "prev_person") ) {
      Toast.makeText( getApplicationContext(), "Операция отклонения успешно завершена", Toast.LENGTH_SHORT).show();
      toolbar.getMenu().clear();
      toolbar.inflateMenu(R.menu.info_menu);
    }


    query.where( RDocumentEntity.UID.eq(UID.get()) ).get().value();


//    new Handler().postDelayed( () -> {
//      prog.dismiss();
//    }, 5000L);


  }

  @Override
  public void onExecuteError() {
    Timber.tag("OpManagerCallback").i("onExecuteSuccess");
  }




  // OSHS selector
  @Override
  public void onSearchSuccess(Oshs user) {
    CommandParams params = new CommandParams();
    params.setPerson( user.getId() );
    operationManager.execute( CommandFactory.Operation.APPROVAL_CHANGE_PERSON, params );
  }

  @Override
  public void onSearchError(Throwable error) {

  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(SignDataResultEvent event) throws Exception {
    Timber.d("SignDataResultEvent %s", event.sign);

    if (event.sign != null) {
      Toast.makeText( getApplicationContext(), event.sign, Toast.LENGTH_SHORT ).show();

    }

    if (dialog != null) {
      dialog.hide();
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onMessageEvent(SignDataWrongPinEvent event) throws Exception {
    Timber.d("SignDataResultEvent %s", event.data);

    if (event.data != null) {
      Toast.makeText( getApplicationContext(), event.data, Toast.LENGTH_SHORT ).show();

    }

    if (dialog != null) {
      dialog.hide();
    }
  }

}

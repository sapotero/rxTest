package sapotero.rxtest.memory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.inject.Inject;

import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.dagger.components.DaggerTestDataComponent;
import sapotero.rxtest.dagger.components.TestDataComponent;
import sapotero.rxtest.dagger.components.TestManagerComponent;
import sapotero.rxtest.dagger.components.TestNetworkComponent;
import sapotero.rxtest.jobs.utils.JobModule;
import sapotero.rxtest.managers.menu.utils.OperationManagerModule;
import sapotero.rxtest.retrofit.utils.OkHttpModule;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.queue.utils.QueueManagerModule;

import static org.powermock.api.mockito.PowerMockito.doNothing;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ EsdApplication.class })
public class MemoryStoreTest {

  @Inject ISettings settings;
  @Mock MemoryStore store;

  private TestSubscriber<InMemoryDocument> subscriber;
  private PublishSubject<InMemoryDocument> pub;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);

    RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
      @Override public Scheduler getMainThreadScheduler() {
        return Schedulers.immediate();
      }
    });

    TestDataComponent testDataComponent = DaggerTestDataComponent.builder().build();
    TestNetworkComponent testNetworkComponent = testDataComponent.plusTestNetworkComponent(new OkHttpModule());
    TestManagerComponent testManagerComponent = testNetworkComponent.plusTestManagerComponent(
      new JobModule(), new QueueManagerModule(), new OperationManagerModule());

    testManagerComponent.inject(this);

    PowerMockito.mockStatic(EsdApplication.class);
    PowerMockito.when(EsdApplication.getManagerComponent()).thenReturn(testManagerComponent);

  }

  @After
  public void tearDown() throws Exception {
    RxAndroidPlugins.getInstance().reset();
  }


  @Test
  public void buildStore() {
    store = new MemoryStore();
    MemoryStore spyStore = Mockito.spy(store);

    doNothing().when(spyStore).loadFromDB();

    spyStore.build();
  }

  @Test
  public void createSubscription(){
    subscriber = new TestSubscriber<>();
    pub = store.getPublishSubject();

//    store
//      .getPublishSubject()
//      .subscribe(subscriber);
  }

  @Test
  public  void validateEmptySubscription(){
//    subscriber.assertNoErrors();
//    subscriber.assertNoTerminalEvent();
  }

}

//
//    sub
//      .buffer( 200, TimeUnit.MILLISECONDS )
//      .onBackpressureBuffer(512)
//      .onBackpressureDrop()
//      .subscribeOn(Schedulers.computation())
//      .observeOn(AndroidSchedulers.mainThread())
//      .subscribe(
//      docs -> {
//      for (InMemoryDocument doc: docs ) {
//      documents.put( doc.getUid(), doc );
//      Timber.tag("RecyclerViewRefresh").d("MemoryStore: pub.onNext()");
//      pub.onNext( doc );
//      }
//
//      if (docs.size() > 0){
//      EventBus.getDefault().post( new JournalSelectorUpdateCountEvent() );
////            counterRecreate();
//      }
//
//      },
//      Timber::e
//      );

package sapotero.rxtest.memory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

@RunWith(PowerMockRunner.class)
@PrepareForTest({ EsdApplication.class })
public class MemoryStoreTest {

  @Inject ISettings settings;

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
  public void createSubscription(){
    MemoryStore memoryStore = buildStore();
    Assert.assertNotNull( memoryStore.getPublishSubject() );
  }

  @Test
  public void validateEmptySubscription(){
    MemoryStore memoryStore = buildStore();
    TestSubscriber<InMemoryDocument> subscriber = new TestSubscriber<>();

    memoryStore
      .getPublishSubject()
      .subscribe(subscriber);

    assertSubscriberValid(subscriber);
    subscriber.assertNoValues();
  }

  @Test
  public void validateAddDocumentToStore(){
    MemoryStore memoryStore = buildStore();
    TestSubscriber<InMemoryDocument> subscriber = new TestSubscriber<>();

    PublishSubject<InMemoryDocument> pub = memoryStore.getPublishSubject();

    pub.subscribe(subscriber);

    assertSubscriberValid(subscriber);


    InMemoryDocument document = new InMemoryDocument();
    pub.onNext( document );

    subscriber.assertValue( document );
    subscriber.assertValueCount(1);
    assertSubscriberValid(subscriber);


    pub.onNext( document );
    pub.onNext( document );
    pub.onNext( document );

    subscriber.assertValueCount(4);
    assertSubscriberValid(subscriber);

  }

  public MemoryStore buildStore() {
    return new MemoryStore().withDB(false).build();
  }

  private void assertSubscriberValid(TestSubscriber<InMemoryDocument> subscriber) {
    subscriber.assertNoErrors();
    subscriber.assertNoTerminalEvent();
    subscriber.assertNotCompleted();
  }

}
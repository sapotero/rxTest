package sapotero.rxtest.mapper;


import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.inject.Inject;

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
import sapotero.rxtest.utils.queue.utils.QueueManagerModule;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ EsdApplication.class })
public class Test {

  private TestManagerComponent testManagerComponent;

  @Inject ISettings settings;

  @Mock MemoryStore store;
  private MemoryStore spyStore;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);

    TestDataComponent testDataComponent = DaggerTestDataComponent.builder().build();
    TestNetworkComponent testNetworkComponent = testDataComponent.plusTestNetworkComponent(new OkHttpModule());
    testManagerComponent = testNetworkComponent.plusTestManagerComponent(
            new JobModule(), new QueueManagerModule(), new OperationManagerModule() );

    testManagerComponent.inject(this);

    PowerMockito.mockStatic(EsdApplication.class);
    PowerMockito.when(EsdApplication.getManagerComponent()).thenReturn(testManagerComponent);

  }


  @org.junit.Test
  public void run() {
    this.store = new MemoryStore();
    this.spyStore = Mockito.spy(store);

    doNothing().when(store).loadFromDB();
  }
}

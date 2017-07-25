package sapotero.rxtest.memory;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.utils.memory.MemoryStore;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ EsdApplication.class })
public class MemoryStoreTest {

//  private TestManagerComponent testDataComponent;
  private MemoryStore store;
  private MemoryStore spyStore;

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);

//    testDataComponent = DaggerTestManagerComponent.builder().build();
//    testDataComponent.inject(this);

    PowerMockito.mockStatic(EsdApplication.class);
//    PowerMockito.when(EsdApplication.getManagerComponent()).thenReturn(testDataComponent);

    createStore();
  }

  private void createStore(){
    this.store = new MemoryStore();
    this.spyStore = Mockito.spy(store);;

    doNothing().when(store).loadFromDB();
  };

  @Test
  public void check(){
    assertEquals(true, true);
  }

}

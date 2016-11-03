package sapotero.rxtest.views.adapters.pagination;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import sapotero.rxtest.retrofit.models.documents.Document;

public class ResponseManager {

  private final static int MAX_LIMIT = 1000;
  private static final long FAKE_RESPONSE_TIME_IN_MS = 200;
  private final static int MAX_FAKE_ERROR_COUNT = 2;
  private final static int OFFSET_WHEN_FAKE_ERROR = 200;

  private static volatile ResponseManager client;

  private int fakeErrorCount = 0;

  public static ResponseManager getInstance() {
    if (client == null) {
      synchronized (ResponseManager.class) {
        if (client == null) {
          client = new ResponseManager();
        }
      }
    }
    return client;
  }

  public Observable<List<Document>> getEmulateResponse(int offset, int limit) {
    if (offset == OFFSET_WHEN_FAKE_ERROR && fakeErrorCount < MAX_FAKE_ERROR_COUNT) {
      // emulate fake error in response
      fakeErrorCount++;
      return Observable
        .error(new RuntimeException("fake error"));
    } else {
      return Observable
        .defer(() -> Observable.just(getFakeItemList(offset, limit)))
        .delaySubscription(FAKE_RESPONSE_TIME_IN_MS, TimeUnit.MILLISECONDS);
    }
  }

  private List<Document> getFakeItemList(int offset, int limit) {
    List<Document> list = new ArrayList<>();
    // If offset > MAX_LIMIT then there is no Items in Fake server. So we return empty List
    if (offset > MAX_LIMIT) {
      return list;
    }
    int concreteLimit = offset + limit;
    // In Fake server there are only MAX_LIMIT Items.
    if (concreteLimit > MAX_LIMIT) {
      concreteLimit = MAX_LIMIT;
    }
    // Generate List of Items
    for (int i = offset; i < concreteLimit; i++) {
      String itemStr = String.valueOf(i);
      list.add(new Document());
    }
    return list;
  }
}

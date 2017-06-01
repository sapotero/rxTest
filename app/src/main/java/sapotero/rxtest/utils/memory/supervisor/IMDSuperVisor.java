package sapotero.rxtest.utils.memory.supervisor;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.utils.memory.InMemoryDocumentStorage;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import timber.log.Timber;

public class IMDSuperVisor {

  private final CompositeSubscription subscription;
  private final InMemoryDocumentStorage store;
  private final String TAG = this.getClass().getSimpleName();

  public IMDSuperVisor(InMemoryDocumentStorage inMemoryDocumentStorage) {
    store = inMemoryDocumentStorage;
    subscription = new CompositeSubscription();
  }

  public void start() {
    createObserverable();
  }

  public void stop() {
    subscription.clear();
  }

  private void createObserverable() {
    subscription.add(
      Observable
        .interval(5000, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          this::print,
          Timber::e
        )
    );
  }

  private void print(Long aLong) {
    for (InMemoryDocument doc: store.getDocuments().values() ) {
      Timber.tag(TAG).i( "[ info ] %s : %s / %0.6s", doc.getUid(), doc.getFilter(), doc.getIndex() );
    }
  }


}

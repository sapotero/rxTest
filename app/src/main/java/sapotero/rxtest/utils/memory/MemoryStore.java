package sapotero.rxtest.utils.memory;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.events.adapter.JournalSelectorUpdateCountEvent;
import sapotero.rxtest.events.utils.RecalculateMenuEvent;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.fields.DocumentType;
import sapotero.rxtest.utils.memory.interfaces.Processable;
import sapotero.rxtest.utils.memory.mappers.InMemoryDocumentMapper;
import sapotero.rxtest.utils.memory.models.Counter;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.Processor;
import sapotero.rxtest.utils.memory.utils.Transaction;
import timber.log.Timber;

public class MemoryStore implements Processable{
  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject ISettings settings;

  private String TAG = this.getClass().getSimpleName();

  private final HashMap<String, InMemoryDocument> documents;

  private final CompositeSubscription subscription;
  private final PublishSubject<InMemoryDocument> pub;
  private final PublishSubject<InMemoryDocument> sub;
  private final Counter counter;

  public MemoryStore() {
    this.pub = PublishSubject.create();
    this.sub = PublishSubject.create();
    this.counter = new Counter();

    // FIXME: 06.07.17
    // https://totallylazy.com/
    // коллекции - totallylazy

    this.documents  = new HashMap<>();

    this.subscription = new CompositeSubscription();

    EsdApplication.getManagerComponent().inject(this);
    loadFromDB();

//    log();

    startSub();

  }

  private void startSub() {
    Timber.w("startSub");

    sub
      .buffer( 200, TimeUnit.MILLISECONDS )
      .onBackpressureBuffer(512)
      .onBackpressureDrop()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        docs -> {
          for (InMemoryDocument doc: docs ) {
            documents.put( doc.getUid(), doc );
            Timber.tag("RecyclerViewRefresh").d("MemoryStore: pub.onNext()");
            pub.onNext( doc );
          }

          if (docs.size() > 0){
            EventBus.getDefault().post( new JournalSelectorUpdateCountEvent() );
//            counterRecreate();
          }

        },
        Timber::e
      );
  }

  public Counter getCounter() {
    return counter;
  }

  public void counterRecreate() {
//    counter.recreate(documents);
    EventBus.getDefault().post( new RecalculateMenuEvent());
  }

  private void log() {

    subscription.clear();
    subscription.add(
      Observable
        .interval( 5, TimeUnit.SECONDS )
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          data -> {
            for (InMemoryDocument doc: getDocuments().values() ) {
              Timber.tag(TAG).i("[*] %s@%s : %s / %s", doc.getState(), doc.getUid(), doc.getFilter(), doc.getIndex() );
            }
          },
          Timber::e
        )
    );

  }

  public void clear(){
    documents.clear();
    loadFromDB();
  };

  public void loadFromDB() {
    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.FROM_LINKS.eq(false))
      .and(RDocumentEntity.USER.eq(settings.getLogin()))
//      .and(RDocumentEntity.FROM_PROCESSED_FOLDER.eq(false))
//      .and(RDocumentEntity.FROM_FAVORITES_FOLDER.eq(false))
      .get().toObservable()
      .toList()
      .subscribeOn(Schedulers.immediate())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        docs -> {
          for (RDocumentEntity doc : docs) {
            InMemoryDocument document = InMemoryDocumentMapper.fromDB(doc);
            documents.put(doc.getUid(), document);
//            counter.put( document );
          }
        },
        Timber::e
      );
  }

  public Transaction startTransactionFor(String uid){
    return new Transaction( documents.get(uid) );
  }

  public HashMap<String, InMemoryDocument> getDocuments() {
    return documents;
  }

  public PublishSubject<InMemoryDocument> getPublishSubject(){
    return pub;
  }


  @Override
  public void process(HashMap<String, Document> docs, String filter, String index) {
    Timber.tag("RecyclerViewRefresh").d("MemoryStore: Process documents from HashMap");

    new Processor(sub)
      .withDocuments(docs)
      .withFilter(filter)
      .withIndex(index)
      .execute();

//    counterRecreate();
  }

  @Override
  public void process(HashMap<String, Document> docs, String folderUid, DocumentType documentType ) {
    Timber.tag("RecyclerViewRefresh").d("MemoryStore: Process documents from HashMap");

    new Processor(sub)
      .withDocuments(docs)
      .withFolder(folderUid)
      .withDocumentType(documentType)
      .execute();
  }

  @Override
  public void process(Transaction transaction) {
    Timber.tag("RecyclerViewRefresh").d("MemoryStore: Process transaction");

    new Processor(sub)
      .withTransaction(transaction)
      .execute();

//    counterRecreate();
  }

  @Override
  public void process(RDocumentEntity doc, String filter, String index) {
    Timber.tag("RecyclerViewRefresh").d("MemoryStore: Process document from RDocumentEntity");

    Timber.tag(TAG).e("process: %s %s %s", doc.getUid(), filter, index);

    new Processor(sub)
      .withFilter(filter)
      .withIndex(index)
      .withDocument(doc)
      .execute();

//    counterRecreate();
  }
}

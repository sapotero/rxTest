package sapotero.rxtest.utils.memory;


import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
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
import sapotero.rxtest.events.rx.UpdateCountEvent;
import sapotero.rxtest.events.utils.LoadedFromDbEvent;
import sapotero.rxtest.events.view.UpdateCurrentDocumentEvent;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.ISettings;
import sapotero.rxtest.utils.memory.fields.DocumentType;
import sapotero.rxtest.utils.memory.interfaces.Processable;
import sapotero.rxtest.utils.memory.mappers.InMemoryDocumentMapper;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.models.NotifyMessageModel;
import sapotero.rxtest.utils.memory.utils.Processor;
import sapotero.rxtest.utils.memory.utils.Transaction;
import timber.log.Timber;

public class MemoryStore implements Processable{

  @Inject SingleEntityStore<Persistable> dataStore;
  @Inject ISettings settings;
  private String TAG = this.getClass().getSimpleName();

  private ConcurrentHashMap<String, InMemoryDocument> documents;

  private CompositeSubscription subscription;

  private PublishSubject<InMemoryDocument> pub;
  private PublishSubject<InMemoryDocument> sub;
  private PublishSubject<NotifyMessageModel> notifyPubSubject;
  private boolean withDB = true;

  public MemoryStore() {
  }

  public MemoryStore withDB(Boolean withDB){
    this.withDB = withDB;
    return this;
  }

  private void init() {
    this.pub = PublishSubject.create();
    this.sub = PublishSubject.create();
    this.notifyPubSubject = PublishSubject.create();
    this.documents  = new ConcurrentHashMap<>();

    this.subscription = new CompositeSubscription();

    EsdApplication.getManagerComponent().inject(this);
  }

  public MemoryStore build(){
    init();

    if (withDB){
      loadFromDB();
    }
    startSub();

    return this;



  }

  private void startSub() {
    Timber.w("startSub");

    sub
      .buffer( 500, TimeUnit.MILLISECONDS )
      .onBackpressureBuffer()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        docs -> {
          int count = 0;

          for (InMemoryDocument doc: docs ) {
            // Добавляем в хранилище только те документы, у которых пользователь равен текущему
            // (может отличаться при входе/выходе из режима замещения)
            if ( Objects.equals( doc.getUser(), settings.getLogin() ) ) {
              documents.put( doc.getUid(), doc );
              Timber.tag("RecyclerViewRefresh").d("MemoryStore: pub.onNext() for %s", doc.getUid());
              pub.onNext( doc );
              count++;

              if ( doc.isUpdatedFromDB() ) {
                EventBus.getDefault().post( new UpdateCurrentDocumentEvent( doc.getUid() ) );
              }
            }
          }

          if (count > 0){
            Timber.tag("RecyclerViewRefresh").d("MemoryStore: sending event to update MainActivity");
            EventBus.getDefault().post( new UpdateCountEvent() );
          }

        },
        Timber::e
      );
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

  public void clear() {
    Timber.tag(TAG).d("MEMORY STORE CLEAR");
    documents.clear();
  }

  public void clearAndLoadFromDb() {
    clear();
    loadFromDB();
  }

  private void loadFromDB() {
    EventBus.getDefault().removeStickyEvent(LoadedFromDbEvent.class);

    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.FROM_LINKS.eq(false))
      .and(RDocumentEntity.USER.eq(settings.getLogin()))
      .get().toObservable()
      .toList()
      .subscribeOn(Schedulers.computation())
      .observeOn(Schedulers.computation())
      .subscribe(
        docs -> {
          for (RDocumentEntity doc : docs) {
            InMemoryDocument document = InMemoryDocumentMapper.fromDB(doc);
            documents.put(doc.getUid(), document);
          }

          Timber.tag("LoadFromDb").d("MemoryStore: send LoadedFromDbEvent");
          EventBus.getDefault().postSticky( new LoadedFromDbEvent() );
        },
        Timber::e
      );
  }

  public Transaction startTransactionFor(String uid){
    return new Transaction( documents.get(uid) );
  }

  public ConcurrentHashMap<String, InMemoryDocument> getDocuments() {
    return documents;
  }

  public PublishSubject<InMemoryDocument> getPublishSubject(){
    return pub;
  }


  @Override
  public void process(HashMap<String, Document> docs, String filter, String index, String login, String currentUserId) {
    Timber.tag("RecyclerViewRefresh").d("MemoryStore: Process documents from HashMap");

    new Processor(sub, notifyPubSubject)
      .withDocuments(docs)
      .withFilter(filter)
      .withIndex(index)
      .withLogin(login)
      .withCurrentUserId(currentUserId)
      .execute();

//    counterRecreate();
  }

  @Override
  public void process(HashMap<String, Document> docs, String folderUid, DocumentType documentType, String login, String currentUserId ) {
    Timber.tag("RecyclerViewRefresh").d("MemoryStore: Process documents from HashMap");

    new Processor(sub, notifyPubSubject)
      .withDocuments(docs)
      .withFolder(folderUid)
      .withDocumentType(documentType)
      .withLogin(login)
      .withCurrentUserId(currentUserId)
      .execute();
  }

  @Override
  public void process(Transaction transaction) {
    Timber.tag("RecyclerViewRefresh").d("MemoryStore: Process transaction");

    new Processor(sub, notifyPubSubject)
      .withTransaction(transaction)
      .execute();

//    counterRecreate();
  }

  @Override
  public void process(RDocumentEntity doc, String filter, String index) {
    Timber.tag("RecyclerViewRefresh").d("MemoryStore: Process document from RDocumentEntity");

    Timber.tag(TAG).e("process: %s %s %s", doc.getUid(), filter, index);

    new Processor(sub, notifyPubSubject)
      .withFilter(filter)
      .withIndex(index)
      .withDocument(doc)
      .execute();

//    counterRecreate();
  }
}

package sapotero.rxtest.utils.memory;

import com.birbit.android.jobqueue.JobManager;

import java.util.HashMap;
import java.util.List;
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
import sapotero.rxtest.jobs.bus.CreateDocumentsJob;
import sapotero.rxtest.jobs.bus.CreateProjectsJob;
import sapotero.rxtest.jobs.bus.UpdateDocumentJob;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.interfaces.Processable;
import sapotero.rxtest.utils.memory.mappers.InMemoryDocumentMapper;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.Filter;
import sapotero.rxtest.utils.memory.utils.Processor;
import sapotero.rxtest.utils.memory.utils.Transaction;
import timber.log.Timber;

public class MemoryStore implements Processable{

  @Inject JobManager jobManager;
  @Inject SingleEntityStore<Persistable> dataStore;

  private String TAG = this.getClass().getSimpleName();

  private final HashMap<String, InMemoryDocument> documents;

  private final CompositeSubscription subscription;
  private final PublishSubject<InMemoryDocument> pub;
  private final PublishSubject<InMemoryDocument> sub;

  public MemoryStore() {
    this.pub = PublishSubject.create();
    this.sub = PublishSubject.create();

    this.documents  = new HashMap<>();

    this.subscription = new CompositeSubscription();

    EsdApplication.getManagerComponent().inject(this);
    loadFromDB();

//    log();

    startSub();

    // сразу захерачить стор
    // хранилку для documentTypeItem
    // чтобы всё там счилось по типам документов
    // и чтобы он туда ходил, а не ломился и не считал всё каждый раз
  }

  private void startSub() {
    Timber.w("startSub");

    sub
      .buffer( 200, TimeUnit.MILLISECONDS )
      .onBackpressureBuffer(64)
      .onBackpressureDrop()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        docs -> {
          for (InMemoryDocument doc: docs ) {
            Timber.w("SUB - %s", doc.getUid());
            documents.put( doc.getUid(), doc );
            pub.onNext( doc );
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

  public void invalidate(){
    documents.clear();
    loadFromDB();
  }

  private void loadFromDB() {
    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.FROM_LINKS.eq(false))
      .and(RDocumentEntity.FROM_PROCESSED_FOLDER.eq(false))
      .and(RDocumentEntity.FROM_FAVORITES_FOLDER.eq(false))
      .get().toObservable()
      .toList()
      .subscribeOn(Schedulers.immediate())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        docs -> {
          for (RDocumentEntity doc : docs) {
            documents.put(doc.getUid(), InMemoryDocumentMapper.fromDB(doc));
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

  public PublishSubject<InMemoryDocument> getSubscribeSubject(){
    return sub;
  }


  public void add(Document document, String index, String filter){
    Timber.tag(TAG).e("-> %s / %s@%5.10s  ", document.getUid(), filter, index );

    if ( documents.containsKey(document.getUid()) ){
      InMemoryDocument doc = documents.get(document.getUid());

      Timber.tag(TAG).e("filters : %s | %s", doc.getFilter(), filter);
      Timber.tag(TAG).e("md5     : %s | %s", doc.getMd5(), document.getMd5());


      if ( Filter.isChanged( doc.getMd5(), document.getMd5() ) ){

        // если изменилось md5
        // Починить добавление
        Transaction Transaction = startTransactionFor(doc.getUid());
        InMemoryDocument new_doc = Transaction
          .from(InMemoryDocumentMapper.fromJson(document))
          .withFilter(filter)
          .withIndex(index)
          .setField(FieldType.PROCESSED, false)
          .commit();

        documents.put( doc.getUid(), new_doc );
        updateJob(index, filter, doc);

      } else {

        // если не изменилось md5
        if ( Filter.isChanged( doc.getFilter(), filter) ){

          Transaction Transaction = startTransactionFor(doc.getUid());
          InMemoryDocument new_doc = Transaction
            .setField(FieldType.PROCESSED, false)
            .commit();

          documents.put( doc.getUid(), new_doc );

        }
      }


    } else {
      Timber.tag(TAG).e("new: %s", document.getUid());

      // если нет - эмитим новый документ

      Transaction Transaction = startTransactionFor(document.getUid());
      InMemoryDocument new_doc = Transaction
        .from(InMemoryDocumentMapper.fromJson(document))
        .withFilter(filter)
        .withFilter(index)
        .setField(FieldType.PROCESSED, false)
        .commit();

      documents.put( document.getUid(), new_doc );

      // refactor
      // если указан индекс - создаем честно
      // если нет - то по старому для проектов

      createJob(document, index, filter);

    }

//    log();

  }

  private void updateJob(String index, String filter, InMemoryDocument doc) {
    jobManager.addJobInBackground( new UpdateDocumentJob( doc.getUid(), index, filter ) );
  }

  private void createJob(Document document, String index, String filter) {
    if (index != null) {
      jobManager.addJobInBackground( new CreateDocumentsJob(document.getUid(), index, filter, false) );
    } else {
      jobManager.addJobInBackground( new CreateProjectsJob(document.getUid(), filter, false) );
    }
  }





  @Override
  public void process(Observable<List<String>> api, String filter, String index) {
    new Processor(sub)
      .withFilter(filter)
      .withIndex(index)
      .withApi(api)
      .execute();
  }

  @Override
  public void process(Observable<List<String>> api, String filter) {
    new Processor(sub)
      .withFilter(filter)
      .withApi(api)
      .execute();
  }

  @Override
  public void process(Document doc) {
    new Processor(sub)
      .withDocument(doc)
      .execute();
  }

  @Override
  public void process(RDocumentEntity doc) {
    new Processor(sub)
      .withDocument(doc)
      .execute();
  }

  @Override
  public void process(RDocumentEntity doc, String filter, String index) {
    Timber.tag(TAG).e("process: %s %s %s", doc.getUid(), filter, index);

    new Processor(sub)
      .withFilter(filter)
      .withFilter(index)
      .withDocument(doc)
      .execute();
  }
}

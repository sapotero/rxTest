package sapotero.rxtest.utils.memory.utils;

import com.birbit.android.jobqueue.JobManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.jobs.bus.CreateDocumentsJob;
import sapotero.rxtest.jobs.bus.CreateProjectsJob;
import sapotero.rxtest.jobs.bus.UpdateDocumentJob;
import sapotero.rxtest.jobs.bus.UpsertDocumentJob;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.Settings;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.InMemoryState;
import sapotero.rxtest.utils.memory.mappers.InMemoryDocumentMapper;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import timber.log.Timber;

public class Processor {
  @Inject MemoryStore store;
  @Inject JobManager jobManager;
  @Inject Settings settings;

  enum Source {
    EMPTY,
    DB,
    TRANSACTION,
    INTERSECT;
  }

  private final String TAG = this.getClass().getSimpleName();
  private final PublishSubject<InMemoryDocument> sub;

  private String filter;
  private String index;

  private RDocumentEntity document_from_db;
  private HashMap<String, Document> documents;
  private Transaction transaction;
  private Source source = Source.EMPTY;

  public Processor(PublishSubject<InMemoryDocument> subscribeSubject) {
    EsdApplication.getManagerComponent().inject(this);

    this.filter = null;
    this.index  = null;

    this.sub = subscribeSubject;
  }

  public Processor withFilter(String filter){
    if (filter != null) {
      this.filter = filter;
    }
    return this;
  }

  public Processor withIndex(String index){
    if (index != null) {
      this.index = index;
    }
    return this;
  }

  public Processor withDocument(RDocumentEntity document) {
    if (document != null) {
      this.document_from_db = document;
      this.source = Source.DB;
    }
    return this;
  }

  public Processor withTransaction(Transaction transaction) {
    if (transaction != null) {
      this.transaction = transaction;
      this.source = Source.TRANSACTION;
    }
    return this;
  }

  public Processor withDocuments(HashMap<String, Document> docs) {
    this.documents = docs;
    this.source = Source.INTERSECT;
    return this;
  }

  public void execute() {

    Transaction transaction = new Transaction();

    switch (source){
      case DB:
        Timber.w("process as db");
        transaction
          .from(InMemoryDocumentMapper.fromDB(document_from_db))
          .setState(InMemoryState.READY);

        commit( transaction );
        break;
      case TRANSACTION:
        commit( this.transaction );
        break;
      case INTERSECT:
        intersect();
        break;
      case EMPTY:
        break;
    }

  }

  private void commit( Transaction transaction){

    if (filter != null) {
      transaction.withFilter(filter);
    }

    if (index != null) {
      transaction.withFilter(index);
    }


    sub.onNext( transaction.commit() );
  }

  private void validate(Document document){
    Timber.tag(TAG).e("->      : %s / %s@%5.10s  ", document.getUid(), filter, index );

    upsert( document );

//    // new upsert job
//    if ( store.getDocuments().keySet().contains( document.getUid() ) ){
//      InMemoryDocument doc = store.getDocuments().get( document.getUid() );
//
//      Timber.tag(TAG).e("filters : %s | %s", doc.getFilter(), filter);
//
//      // изменилось MD5
//      if ( Filter.isChanged( doc.getMd5(), document.getMd5() ) ){
//        Timber.tag(TAG).e("md5     : %s | %s", doc.getMd5(), document.getMd5());
//        updateJob( doc.getUid() );
//      }
//
//    } else {
//      Timber.tag(TAG).e("new: %s", document.getUid());
//      createJob(document.getUid());
//    }

  }

  private ArrayList<String> intersect(){

    Filter imdFilter = new Filter( conditions() );

    Observable<List<String>> docs = Observable
      .from(documents.keySet())
      .toList();

    Observable<List<String>> imd = Observable
      .from( store.getDocuments().values() )
      .filter(imdFilter::byType)
      .filter(imdFilter::byStatus)
      .map(InMemoryDocument::getUid)
      .toList();

    Observable
      .zip(imd, docs, (memory, api) -> {



        Timber.tag(TAG).e("memory: %s", memory.size());
        Timber.tag(TAG).e("api: %s", api.size());

        List<String> add = new ArrayList<>(api);
        add.removeAll(memory);

        List<String> remove = new ArrayList<>(memory);
        remove.removeAll(api);

        Timber.tag(TAG).e("add: %s", add.size());
        Timber.tag(TAG).e("rem: %s", remove.size());


        for (String uid : remove) {
          updateAndSetProcessed( uid );
        }

        for ( String doc : api ) {
          setAsUnprocessed( doc );
        }

        for ( Document doc : documents.values() ) {
          validate( doc );
        }

        return Collections.singletonList("");
      })
      .buffer(200, TimeUnit.MILLISECONDS)
      .subscribeOn(Schedulers.immediate())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        data -> {
          Timber.tag(TAG).e("processed");
        },
        Timber::e
      );


    return new ArrayList<>();
  }

  private void setAsUnprocessed(String uid) {

    if (store.getDocuments().containsKey(uid)){
      store.process(
        store.startTransactionFor(uid)
          .setField(FieldType.PROCESSED, false)
      );
    }

  }

  private ArrayList<ConditionBuilder> conditions() {
    ArrayList<ConditionBuilder> conditions = new ArrayList<>();

    if (filter != null) {
      Timber.i("filter: %s", filter);
      conditions.add( new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq( filter )  ) );
    }
    if (index != null) {
      Timber.i("index: %s", index);
      conditions.add( new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.DOCUMENT_TYPE.eq( index )  ) );
    }

    return conditions;
  }


  private void createJob(String uid) {
    settings.addJobCount(1);
    if (index != null) {
      jobManager.addJobInBackground( new CreateDocumentsJob(uid, index, filter, false) );
    } else {
      jobManager.addJobInBackground( new CreateProjectsJob(uid, filter, false) );
    }
  }

  private void updateJob(String uid) {
    settings.addJobCount(1);
    jobManager.addJobInBackground( new UpdateDocumentJob( uid, index, filter ) );
  }

  private void updateAndSetProcessed(String uid) {
    settings.addJobCount(1);
    jobManager.addJobInBackground( new UpdateDocumentJob( uid, index, filter, true ) );
  }

  private void upsert(Document uid) {
    settings.addJobCount(1);
    jobManager.addJobInBackground( new UpsertDocumentJob( uid, index, filter) );
  }


}
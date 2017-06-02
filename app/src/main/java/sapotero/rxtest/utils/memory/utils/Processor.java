package sapotero.rxtest.utils.memory.utils;

import com.birbit.android.jobqueue.JobManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.jobs.bus.CreateDocumentsJob;
import sapotero.rxtest.jobs.bus.CreateProjectsJob;
import sapotero.rxtest.jobs.bus.UpdateDocumentJob;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.MemoryStore;
import sapotero.rxtest.utils.memory.mappers.InMemoryDocumentMapper;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import timber.log.Timber;

public class Processor {
  @Inject MemoryStore store;
  @Inject JobManager jobManager;

  enum Source {
    EMPTY,
    JSON,
    DB,
    INTERSECT
  }

  private final String TAG = this.getClass().getSimpleName();
  private final PublishSubject<InMemoryDocument> sub;

  private String filter;
  private String index;
  private Observable<List<String>> api;
  private Document document_from_api;
  private RDocumentEntity document_from_db;
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

  public Processor withApi(Observable<List<String>> api) {
    this.api = api;
    this.source = Source.INTERSECT;
    return this;
  }

  public Processor withDocument(Document document) {
    if (document != null) {
      this.document_from_api = document;
      this.source = Source.JSON;
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

  public void execute() {

    Transaction transaction = new Transaction();

    switch (source){
      case JSON:
        validate(document_from_api);
        transaction.from(document_from_api);
        commit( transaction );
        break;
      case DB:
        transaction.from(InMemoryDocumentMapper.fromDB(document_from_db));
        commit( transaction );
        break;
      case INTERSECT:
        intersect(api);
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

  private ArrayList<String> intersect(Observable<List<String>> api){

    ArrayList<String> uids = new ArrayList<>();
    ArrayList<ConditionBuilder> conditions = new ArrayList<>();

    if (filter != null) {
      conditions.add( new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq( filter )  ) );
    }
    if (index != null) {
      conditions.add( new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.DOCUMENT_TYPE.eq( index )  ) );
    }

    Filter imdFilter = new Filter(conditions);

    Observable<List<String>> memory = Observable
      .from( store.getDocuments().values() )
      .filter(imdFilter::byType)
      .filter(imdFilter::byStatus)
      .map(InMemoryDocument::getUid)
      .toList();

    Subscription subscribe = Observable
      .zip(memory, api, (original, selected) -> {
        Timber.tag(TAG).e("original: %s", original.size());
        Timber.tag(TAG).e("selected: %s", selected.size());

        List<String> add = new ArrayList<>(selected);
        add.removeAll(original);

        List<String> remove = new ArrayList<>(original);
        remove.removeAll(selected);

        Timber.tag(TAG).e("add: %s", add.size());
        Timber.tag(TAG).e("rem: %s", remove.size());


        for (String uid : remove) {
          update(uid, filter, index, true);
        }

        return Collections.singletonList("");
      })
      .buffer(500, TimeUnit.MILLISECONDS)
      .subscribeOn(Schedulers.immediate())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        data -> {
//          EventBus.getDefault().post(new RecalculateMenuEvent());
        },
        Timber::e
      );


    return uids;
  }

  private void update(String uid, String filter, String index, Boolean processed){
//    Transaction Transaction = startTransactionFor(uid);
//    InMemoryDocument new_doc = Transaction
//      .withFilter(filter)
//      .withFilter(index)
//      .setField(FieldType.PROCESSED, processed)
//      .setState(InMemoryState.READY)
//      .commit();

  }


  private void validate(Document document){
    Timber.tag(TAG).e("-> %s / %s@%5.10s  ", document.getUid(), filter, index );

    HashMap<String, InMemoryDocument> documents = store.getDocuments();

    if ( documents.containsKey(document.getUid()) ){
      InMemoryDocument doc = documents.get(document.getUid());

      Timber.tag(TAG).e("filters : %s | %s", doc.getFilter(), filter);
      Timber.tag(TAG).e("md5     : %s | %s", doc.getMd5(), document.getMd5());

      // изменилось MD5
      if ( Filter.isChanged( doc.getMd5(), document.getMd5() ) ){
        updateJob(doc);
      }

    } else {
      Timber.tag(TAG).e("new: %s", document.getUid());
      createJob(document);
    }

  }


  private void updateJob(InMemoryDocument doc) {
    jobManager.addJobInBackground( new UpdateDocumentJob( doc.getUid(), index, filter ) );
  }

  private void createJob(Document document) {
    if (index != null) {
      jobManager.addJobInBackground( new CreateDocumentsJob(document.getUid(), index, filter, false) );
    } else {
      jobManager.addJobInBackground( new CreateProjectsJob(document.getUid(), filter, false) );
    }
  }



}

package sapotero.rxtest.utils.memory.utils;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.mappers.InMemoryDocumentMapper;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import timber.log.Timber;

public class Processor {
  private final String TAG = this.getClass().getSimpleName();

//  private final PublishSubject<InMemoryDocument> pub;
  private final PublishSubject<InMemoryDocument> sub;

  private String filter;
  private String index;
  private Observable<List<String>> api;
  private Document json;
  private RDocumentEntity db;

  public Processor(PublishSubject<InMemoryDocument> subscribeSubject) {
    Timber.tag(TAG).w("new");

    this.filter = null;
    this.index  = null;

//    this.pub = publishSubject;
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
      this.filter = index;
    }
    return this;
  }

  public Processor withApi(Observable<List<String>> api) {
    this.api = api;
    return this;
  }

  public Processor withDocument(Document json) {
    if (json != null) {
      this.json = json;
    }
    return this;
  }

  public Processor withDocument(RDocumentEntity db) {
    if (db != null) {
      this.db = db;
    }
    return this;
  }

  public void execute() {
    Transaction transaction = new Transaction();

    if (db != null) {
      Timber.tag(TAG).w("execute: %s", db.getUid());

      transaction.from(InMemoryDocumentMapper.fromDB(db));
    }

    if (filter != null) {
      transaction.withFilter(filter);
    }

    if (index != null) {
      transaction.withFilter(index);
    }


    sub.onNext( transaction.commit() );
  }


//  private ArrayList<String> intersect(Observable<List<String>> api, String filter, String index){
//
//    ArrayList<String> uids = new ArrayList<>();
//    ArrayList<ConditionBuilder> conditions = new ArrayList<>();
//
//    if (filter != null) {
//      conditions.add( new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq( filter )  ) );
//    }
//    if (index != null) {
//      conditions.add( new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.DOCUMENT_TYPE.eq( index )  ) );
//    }
//
//    Filter imdFilter = new Filter(conditions);
//
//    Observable<List<String>> memory = Observable
//      .from(documents.values())
//      .filter(imdFilter::byType)
//      .filter(imdFilter::byStatus)
//      .map(InMemoryDocument::getUid)
//      .toList();
//
//    Observable
//      .zip(memory, api, (original, selected) -> {
//        Timber.tag(TAG).e("original: %s", original.size() );
//        Timber.tag(TAG).e("selected: %s", selected.size() );
//
//        List<String> add = new ArrayList<>(selected);
//        add.removeAll(original);
//
//        List<String> remove = new ArrayList<>(original);
//        remove.removeAll(selected);
//
//        Timber.tag(TAG).e("add: %s", add.size() );
//        Timber.tag(TAG).e("rem: %s", remove.size() );
//
//
//        for (String uid: remove) {
//          update(uid, filter, index, true);
//        }
//
//        return Collections.singletonList("");
//      })
//      .buffer(500, TimeUnit.MILLISECONDS)
//      .subscribeOn(Schedulers.immediate())
//      .observeOn(AndroidSchedulers.mainThread())
//      .subscribe(
//        data -> {
//          EventBus.getDefault().post( new RecalculateMenuEvent() );
//        },
//        Timber::e
//      );
//
//
//    return uids;
//  }
//
//  private void intersect(Observable<List<String>> api, String filter) {
//    intersect(api, filter, null);
//  }

  private void update(String uid, String filter, String index, Boolean processed){
//    Transaction Transaction = startTransactionFor(uid);
//    InMemoryDocument new_doc = Transaction
//      .withFilter(filter)
//      .withFilter(index)
//      .setField(FieldType.PROCESSED, processed)
//      .setState(InMemoryState.READY)
//      .commit();
//
//    documents.put( new_doc.getUid(), new_doc);
  }

}

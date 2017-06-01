package sapotero.rxtest.utils.memory;

import com.birbit.android.jobqueue.JobManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
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
import sapotero.rxtest.events.utils.RecalculateMenuEvent;
import sapotero.rxtest.jobs.bus.CreateDocumentsJob;
import sapotero.rxtest.jobs.bus.CreateProjectsJob;
import sapotero.rxtest.jobs.bus.UpdateDocumentJob;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.InMemoryState;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.mappers.InMemoryDocumentMapper;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.IMDFilter;
import sapotero.rxtest.utils.memory.utils.Transaction;
import sapotero.rxtest.views.menu.builders.ConditionBuilder;
import timber.log.Timber;

public class InMemoryDocumentStorage {

  @Inject JobManager jobManager;
  @Inject SingleEntityStore<Persistable> dataStore;

  private String TAG = this.getClass().getSimpleName();

  private final HashMap<String, InMemoryDocument> documents;

  private final CompositeSubscription subscription;
  private final PublishSubject<InMemoryDocument> pub;
  private final PublishSubject<InMemoryDocument> sub;

  public InMemoryDocumentStorage() {
    this.pub = PublishSubject.create();
    this.sub = PublishSubject.create();
    this.documents  = new HashMap<>();

    this.subscription = new CompositeSubscription();

    EsdApplication.getManagerComponent().inject(this);
    loadFromDB();

    log();
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
    return new Transaction( documents.get(uid), pub);
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


      if ( IMDFilter.isChanged( doc.getMd5(), document.getMd5() ) ){

        // если изменилось md5
        // Починить добавление
        Transaction transaction = startTransactionFor(doc.getUid());
        InMemoryDocument new_doc = transaction
          .from(InMemoryDocumentMapper.fromJson(document))
          .withFilter(filter)
          .withIndex(index)
          .setField(FieldType.PROCESSED, false)
          .commit();

        documents.put( doc.getUid(), new_doc );
        updateJob(index, filter, doc);

      } else {

        // если не изменилось md5
        if ( IMDFilter.isChanged( doc.getFilter(), filter) ){

          Transaction transaction = startTransactionFor(doc.getUid());
          InMemoryDocument new_doc = transaction
            .setField(FieldType.PROCESSED, false)
            .commit();

          documents.put( doc.getUid(), new_doc );

        }
      }


    } else {
      Timber.tag(TAG).e("new: %s", document.getUid());

      // если нет - эмитим новый документ

      Transaction transaction = startTransactionFor(document.getUid());
      InMemoryDocument new_doc = transaction
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

    log();

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

  // v4.0 start
  public void updateFromJob(RDocumentEntity db, String filter, String index){

    Transaction transaction = startTransactionFor(db.getUid());
    InMemoryDocument new_doc = transaction
      .from( InMemoryDocumentMapper.fromDB(db) )
      .withFilter(filter)
      .withFilter(index)
      .setState(InMemoryState.READY)
      .removeLabel(LabelType.SYNC)
      .commit();

    documents.put( new_doc.getUid(), new_doc);
  }

  private void update(String uid, String filter, String index, Boolean processed){
    Transaction transaction = startTransactionFor(uid);
    InMemoryDocument new_doc = transaction
      .withFilter(filter)
      .withFilter(index)
      .setField(FieldType.PROCESSED, processed)
      .setState(InMemoryState.READY)
      .commit();

    documents.put( new_doc.getUid(), new_doc);
  }

  public ArrayList<String> intersect(Observable<List<String>> api, String filter, String index){

    ArrayList<String> uids = new ArrayList<>();
    ArrayList<ConditionBuilder> conditions = new ArrayList<>();

    if (filter != null) {
      conditions.add( new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.FILTER.eq( filter )  ) );
    }
    if (index != null) {
      conditions.add( new ConditionBuilder( ConditionBuilder.Condition.AND, RDocumentEntity.DOCUMENT_TYPE.eq( index )  ) );
    }

    IMDFilter imdFilter = new IMDFilter(conditions);

    Observable<List<String>> memory = Observable
      .from(documents.values())
      .filter(imdFilter::byType)
      .filter(imdFilter::byStatus)
      .map(InMemoryDocument::getUid)
      .toList();

    Observable
      .zip(memory, api, (original, selected) -> {
        Timber.tag(TAG).e("original: %s", original.size() );
        Timber.tag(TAG).e("selected: %s", selected.size() );

        List<String> add = new ArrayList<>(selected);
        add.removeAll(original);

        List<String> remove = new ArrayList<>(original);
        remove.removeAll(selected);

        Timber.tag(TAG).e("add: %s", add.size() );
        Timber.tag(TAG).e("rem: %s", remove.size() );


        for (String uid: remove) {
          update(uid, filter, index, true);
        }

        return Collections.singletonList("");
      })
      .buffer(500, TimeUnit.MILLISECONDS)
      .subscribeOn(Schedulers.immediate())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        data -> {
          EventBus.getDefault().post( new RecalculateMenuEvent() );
        },
        Timber::e
      );


    return uids;
  }


  public void intersect(Observable<List<String>> api, String filter) {
    intersect(api, filter, null);
  }
}

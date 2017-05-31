package sapotero.rxtest.utils.memory;

import com.birbit.android.jobqueue.JobManager;

import java.util.HashMap;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.jobs.bus.CreateDocumentsJob;
import sapotero.rxtest.jobs.bus.CreateProjectsJob;
import sapotero.rxtest.jobs.bus.UpdateDocumentJob;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.InMemoryState;
import sapotero.rxtest.utils.memory.mappers.InMemoryDocumentMapper;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.IMDFilter;
import sapotero.rxtest.utils.memory.utils.Transaction;
import timber.log.Timber;

public class InMemoryDocumentStorage {

  @Inject JobManager jobManager;
  @Inject SingleEntityStore<Persistable> dataStore;

  private String TAG = this.getClass().getSimpleName();

  private final PublishSubject<InMemoryDocument> publish;
  private final HashMap<String, InMemoryDocument> documents;

  public InMemoryDocumentStorage() {
    this.publish    = PublishSubject.create();
    this.documents  = new HashMap<>();

    EsdApplication.getManagerComponent().inject(this);
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
    return new Transaction( documents.get(uid), publish );
  }

  public HashMap<String, InMemoryDocument> getDocuments() {
    return documents;
  }

  public PublishSubject<InMemoryDocument> getPublishSubject(){
    return publish;
  }


  public void add(Document document, String index, String filter){
    Timber.tag(TAG).e("-> %s / %s@%5.10s  ", document.getUid(), filter, index );

    if ( documents.containsKey(document.getUid()) ){
      InMemoryDocument doc = documents.get(document.getUid());
      Timber.tag(TAG).e("filters : %s | %s", doc.getFilter(), filter);
      Timber.tag(TAG).e("md5     : %s | %s", doc.getMd5(), document.getMd5());


      if ( IMDFilter.isChanged( doc.getMd5(), document.getMd5() ) ){
        Timber.tag(TAG).e("update: %s", document.getUid());

        // если изменилось md5

        Transaction transaction = startTransactionFor(doc.getUid());
        InMemoryDocument new_doc = transaction
          .from(InMemoryDocumentMapper.fromJson(document))
          .withFilter(filter)
          .withFilter(index)
          .setField(FieldType.PROCESSED, false)
          .commit();

        documents.put( doc.getUid(), new_doc );

        jobManager.addJobInBackground( new UpdateDocumentJob( doc.getUid(), index, filter ) );
//        if (index != null) {
//          jobManager.addJobInBackground( new UpdateDocumentJob(document.getUid(), index, filter, false) );
//        } else {
//          jobManager.addJobInBackground( new UpdateDocumentJob(document.getUid(), filter, false) );
//        }

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
      documents.put(document.getUid(), InMemoryDocumentMapper.fromJson(document));
      InMemoryDocument doc = documents.get(document.getUid());

      Transaction transaction = startTransactionFor(doc.getUid());
      InMemoryDocument new_doc = transaction
        .withFilter(filter)
        .withFilter(index)
        .setField(FieldType.PROCESSED, false)
        .commit();

      documents.put( doc.getUid(), new_doc );

      // refactor
      // если указан индекс - создаем честно
      // если нет - то по старому для проектов



      if (index != null) {
        jobManager.addJobInBackground( new CreateDocumentsJob(doc.getUid(), index, filter, false) );
      } else {
        jobManager.addJobInBackground( new CreateProjectsJob(doc.getUid(), filter, false) );
      }

    }

  }

  // v4.0 start
  public void update(RDocumentEntity db, String filter, String index){

    Transaction transaction = startTransactionFor(db.getUid());
    InMemoryDocument new_doc = transaction
      .from( InMemoryDocumentMapper.fromDB(db) )
      .withFilter(filter)
      .withFilter(index)
      .setField(FieldType.PROCESSED, false)
      .setState(InMemoryState.READY)
      .commit();

    documents.put( new_doc.getUid(), new_doc);


  }


}

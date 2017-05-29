package sapotero.rxtest.utils.memory;

import com.birbit.android.jobqueue.JobManager;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.requery.Persistable;
import io.requery.rx.SingleEntityStore;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.db.requery.models.RDocumentEntity;
import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.jobs.bus.CreateDocumentsJob;
import sapotero.rxtest.jobs.bus.UpdateDocumentJob;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.mappers.InMemoryDocumentMapper;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.GenerateRandomDocument;
import sapotero.rxtest.utils.memory.utils.IMDValidation;
import sapotero.rxtest.utils.memory.utils.InMemoryLogger;
import timber.log.Timber;

public class InMemoryDocumentStorage {

  @Inject JobManager jobManager;
  @Inject SingleEntityStore<Persistable> dataStore;

  private final ScheduledThreadPoolExecutor scheduller;
  private String TAG = this.getClass().getSimpleName();

  private final InMemoryLogger logger;

  private final PublishSubject<InMemoryDocument> publish;
  private final HashMap<String, InMemoryDocument> documents;
  private Subscription loggerSubscription;

  public InMemoryDocumentStorage() {

    Timber.tag(TAG).e("initialize");

    this.publish    = PublishSubject.create();
    this.documents  = new HashMap<>();
    this.logger     = new InMemoryLogger();
    this.scheduller = new ScheduledThreadPoolExecutor(1);

    EsdApplication.getManagerComponent().inject(this);

//    initScheduller();
    loadFromDB();
    initLogger();
  }

  public HashMap<String, InMemoryDocument> getDocuments() {
    return documents;
  }

  private void initScheduller() {
    scheduller.scheduleWithFixedDelay( new GenerateRandomDocument(this), 0 ,1, TimeUnit.SECONDS );
  }

  private void initLogger(){
    publish
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        logger::log,
        Timber::e
      );
  }

  public PublishSubject<InMemoryDocument> getPublishSubject(){
    return publish;
  }

  public void generateFakeDocumentEntity(){

    Timber.tag(TAG).e("total: %s", documents.size());

    String uid = UUID.randomUUID().toString();

    InMemoryDocument fake = new InMemoryDocument();
    fake.setUid(uid);
    fake.setMd5("fake_md5");
    fake.setAsLoading();
    fake.setStatus( Fields.Status.getRandom() );

    documents.put(uid, fake);
    publish.onNext(documents.get(uid));

  }

  public void add(Document document, String index, String filter){

    if ( documents.containsKey(document.getUid()) ){
      Timber.tag(TAG).e("contains: %s | %s %s", document.getUid(), index, filter );


      // если есть - проводим инвалидацию
      InMemoryDocument inMemoryDocument = documents.get(document.getUid());
      if ( IMDValidation.isMd5Changed( inMemoryDocument.getMd5(), document.getMd5() ) ){
        Timber.tag(TAG).e("update: %s", document.getUid());

        inMemoryDocument = InMemoryDocumentMapper.fromJson(document);
        inMemoryDocument.setFilter(filter);
        inMemoryDocument.setIndex(index);

        if (index != null) {
          jobManager.addJobInBackground( new UpdateDocumentJob(document.getUid(), index, filter, false) );
        } else {
          jobManager.addJobInBackground( new UpdateDocumentJob(document.getUid(), filter, false) );
        }
        publish.onNext(inMemoryDocument);

      }

    } else {
      Timber.tag(TAG).e("new: %s", document.getUid());

      // если нет - эмитим новый документ
      documents.put(document.getUid(), InMemoryDocumentMapper.fromJson(document));

      InMemoryDocument inMemoryDocument = documents.get(document.getUid());
      inMemoryDocument.setFilter(filter);
      inMemoryDocument.setIndex(index);

      publish.onNext( documents.get(document.getUid()) );

      // refactor
      // если указан индекс - создаем честно
      // если нет - то по старому для проектов
      if (index != null) {
        jobManager.addJobInBackground( new CreateDocumentsJob(document.getUid(), index, filter, false) );
      } else {
        jobManager.addJobInBackground( new UpdateDocumentJob(document.getUid(), filter, false) );
      }
    }

  }



  private void loadFromDB() {
    dataStore
      .select(RDocumentEntity.class)
      .where(RDocumentEntity.FROM_LINKS.eq(false))
      .and(RDocumentEntity.FROM_PROCESSED_FOLDER.eq(false))
      .and(RDocumentEntity.FROM_FAVORITES_FOLDER.eq(false))
      .get().toObservable()
      .subscribeOn(Schedulers.computation())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        doc -> {
          documents.put(doc.getUid(), InMemoryDocumentMapper.fromDB(doc));
        },
        Timber::e
      );
  }

  public InMemoryDocument get(String uid){
    return documents.get(uid);
  }

  public void update(RDocumentEntity db, String filter, String index){
    InMemoryDocument inMemoryDocument = documents.get( db.getUid() );

    if (inMemoryDocument != null) {
      InMemoryDocument doc = InMemoryDocumentMapper.fromDB(db);

      documents.remove( doc.getUid() );
      documents.put( doc.getUid(), doc);

      doc.setFilter(filter);
      doc.setIndex(index);

      publish.onNext( doc );
    }
  }





  public void setField(FieldType type, Boolean value, String uid) {
    InMemoryDocument doc = documents.get(uid);
    if (doc != null) {

      switch (type){
        case PROCESSED:
          doc.getDocument().setProcessed(value);
          break;
      }

      publish.onNext( doc );

    }
  }

  private void changeLabel(LabelType type, Boolean value, String uid) {
    InMemoryDocument doc = documents.get(uid);
    if (doc != null) {

      switch (type){
        case CONTROL:
          doc.getDocument().setControl(value);
          break;
        case LOCK:
          doc.getDocument().setFromProcessedFolder(value);
          break;
        case SYNC:
          doc.getDocument().setChanged(value);
          break;
        case FAVORITES:
          doc.getDocument().setFavorites(value);
          break;
      }

      publish.onNext( doc );

    }
  }

  public void setLabel(LabelType type, String uid) {
    changeLabel(type, true, uid);
  }

  public void removeLabel(LabelType type, String uid) {
    changeLabel(type, false, uid);
  }
}

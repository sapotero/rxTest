package sapotero.rxtest.utils.memory;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.mappers.InMemoryDocumentMapper;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.GenerateRandomDocument;
import sapotero.rxtest.utils.memory.utils.InMemoryLogger;
import timber.log.Timber;

public class InMemoryDocumentStorage {

  private final ScheduledThreadPoolExecutor scheduller;
  private String TAG = this.getClass().getSimpleName();

  private final InMemoryLogger logger;

  private final PublishSubject<InMemoryDocument> publish;
  private final HashMap<String, InMemoryDocument> documents;

  public InMemoryDocumentStorage() {

    Timber.tag(TAG).e("initialize");

    this.publish    = PublishSubject.create();
    this.documents  = new HashMap<>();
    this.logger     = new InMemoryLogger();
    this.scheduller = new ScheduledThreadPoolExecutor(1);

    initScheduller();
    initLogger();
  }

  private void initScheduller() {
    scheduller.scheduleWithFixedDelay( new GenerateRandomDocument(this), 0 ,10, TimeUnit.SECONDS );
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

    String uid = UUID.randomUUID().toString();

    InMemoryDocument fake = new InMemoryDocument();
    fake.setUid(uid);
    fake.setMd5("fake_md5");
    fake.setAsLoading();

    documents.put(uid, fake);
    publish.onNext(documents.get(uid));

  }

  public void add(Document document){

    Timber.tag(TAG).e("recv: %s", document.getUid());

    if ( documents.containsKey(document.getUid()) ){

      // если есть - проводим инвалидацию
      InMemoryDocument inMemoryDocument = documents.get(document.getUid());
      if (!Objects.equals(document.getMd5(), inMemoryDocument.getMd5())){
        inMemoryDocument.setAsLoading();
        publish.onNext(inMemoryDocument);
      }

    } else {

      // если нет - эмитим новый документ
      documents.put(document.getUid(), InMemoryDocumentMapper.toMemoryModel(document));
      publish.onNext( documents.get(document.getUid()) );
    }

  }

}

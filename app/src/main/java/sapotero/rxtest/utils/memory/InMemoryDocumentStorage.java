package sapotero.rxtest.utils.memory;

import java.util.HashMap;
import java.util.Objects;

import rx.subjects.PublishSubject;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.mappers.InMemoryDocumentMapper;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.InMemoryLogger;
import timber.log.Timber;

public class InMemoryDocumentStorage {

  private String TAG = this.getClass().getSimpleName();

  private final InMemoryLogger logger;

  private final PublishSubject<InMemoryDocument>  publish;
  private final HashMap<String, InMemoryDocument> documents;

  public InMemoryDocumentStorage() {

    Timber.tag(TAG).e("initialize");

    this.publish   = PublishSubject.create();
    this.documents = new HashMap<>();
    this.logger    = new InMemoryLogger();
  }

  public PublishSubject<InMemoryDocument> getPublishSubject(){
    return publish;
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

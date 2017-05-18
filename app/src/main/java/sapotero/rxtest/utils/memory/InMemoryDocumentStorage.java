package sapotero.rxtest.utils.memory;

import java.util.HashMap;
import java.util.Objects;

import rx.subjects.PublishSubject;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.mappers.InMemoryDocumentMapper;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.memory.utils.InMemoryLogger;

public class InMemoryDocumentStorage {

  private final PublishSubject<InMemoryDocument>  publish = PublishSubject.create();
  private final HashMap<String, InMemoryDocument> documents  = new HashMap<>();
  private final InMemoryLogger logger  = new InMemoryLogger();

  public InMemoryDocumentStorage() {
    initInMemoryLogger();
  }

  private void initInMemoryLogger() {

    publish.subscribe(logger::log);
  }

  public void add(Document document){

    if ( documents.containsKey(document.getUid()) ){

      // если есть - проводим инвалидацию
      InMemoryDocument inMemoryDocument = documents.get(document.getUid());
      if (!Objects.equals(document.getMd5(), inMemoryDocument.getMd5())){
        inMemoryDocument.setAsLoading();
        publish.onNext(inMemoryDocument);
      }

    } else {

      // если нет - эмитим новый документ
      InMemoryDocument inMemoryDocument = documents.put(document.getUid(), InMemoryDocumentMapper.toMemoryModel(document));
      publish.onNext(inMemoryDocument);
    }

  }

}

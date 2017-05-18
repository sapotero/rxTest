package sapotero.rxtest.utils.memory;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;
import sapotero.rxtest.retrofit.models.documents.Document;

public class DocumentMemoryStore {

  private final List<Document> list;
  private final PublishSubject<Document> subject;

  public DocumentMemoryStore() {
    this.list = new ArrayList<>();
    this.subject = PublishSubject.create();
  }

  public void add(Document value) {
    list.add(value);
    subject.onNext(value);
  }

  public void remove(Document value) {
    list.remove(value);
    subject.onNext(value);
  }

  public Observable<Document> getObservable() {
    return subject;
  }

  public Observable<Document> getCurrentList() {
    return Observable.from(list);
  }
}

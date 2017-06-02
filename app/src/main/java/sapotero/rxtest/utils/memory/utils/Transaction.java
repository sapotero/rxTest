package sapotero.rxtest.utils.memory.utils;

import sapotero.rxtest.utils.memory.fields.FieldType;
import sapotero.rxtest.utils.memory.fields.InMemoryState;
import sapotero.rxtest.utils.memory.fields.LabelType;
import sapotero.rxtest.utils.memory.models.InMemoryDocument;

public class Transaction {
//  private final PublishSubject<InMemoryDocument> publish;
  private InMemoryDocument document;


  public Transaction(InMemoryDocument inMemoryDocument) {
    this.document = inMemoryDocument;
//    this.publish = publish;
  }

  public Transaction() {
    this.document = new InMemoryDocument();
  }


  public Transaction setField(FieldType type, Boolean value) {
    switch (type){
      case PROCESSED:
        document.getDocument().setProcessed(value);
        document.setProcessed(value);
        break;
    }

    return this;
  }

  public Transaction setField(FieldType type, String value) {
    switch (type){
      case MD5:
        document.getDocument().setMd5(value);
        document.setMd5(value);
        break;
      case FILTER:
        document.setFilter(value);
        break;

    }
    return this;
  }

  private void changeLabel(LabelType type, Boolean value) {
    switch (type){
      case CONTROL:
        document.getDocument().setControl(value);
        break;
      case LOCK:
        document.getDocument().setFromProcessedFolder(value);
        break;
      case SYNC:
        document.getDocument().setChanged(value);
        break;
      case FAVORITES:
        document.getDocument().setFavorites(value);
        break;
    }
  }

  public Transaction setLabel(LabelType type) {
    changeLabel(type, true);
    return this;
  }

  public Transaction removeLabel(LabelType type) {
    changeLabel(type, false);
    return this;
  }

  public Transaction withFilter(String filter) {
    if (filter != null) {
      document.setFilter(filter);
    }
    return this;
  }

  public Transaction withIndex(String index) {
    if (index != null) {
      document.setIndex(index);
    }
    return this;
  }

  public Transaction from(InMemoryDocument document) {
    this.document = document;
    return this;
  }

  public Transaction setState(InMemoryState state) {
    switch ( state ){
      case READY:
        document.setAsReady();
        break;
      case LOADING:
        document.setAsLoading();
        break;
    }
    return this;
  }

  public InMemoryDocument commit(){
//    publish.onNext(document);
    return document;
  }
}

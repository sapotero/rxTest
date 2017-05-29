package sapotero.rxtest.utils.memory.models;

import java.io.Serializable;

import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.fields.InMemoryState;

/**
 * In Memory Document Storage
 */



public class InMemoryDocument implements Serializable {
  public String uid;
  public String md5;
  public String index;
  public String filter;
  public Boolean hasDecision = false;
  public Boolean processed   = false;

  public Document document;
  private InMemoryState action = InMemoryState.NEW;

  public InMemoryDocument() {
  }

  public void setProcessed(Boolean processed) {
    this.processed = processed;
  }

  public Boolean isProcessed() {
    return processed;
  }

  public Boolean hasDecision() {
    return hasDecision;
  }

  public void setHasDecision(Boolean hasDecision) {
    this.hasDecision = hasDecision;
  }

  public Document getDocument() {
    return document;
  }

  public void setDocument(Document document) {
    this.document = document;
  }

  public String getIndex() {
    return index;
  }

  public void setIndex(String index) {
    this.index = index;
  }

  public String getFilter() {
    return filter;
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  public void setAsLoading(){
    action = InMemoryState.LOADING;
  }

  public void setAsDeleted(){
    action = InMemoryState.DELETE;
  }

  public void setAsNew(){
    action = InMemoryState.NEW;
  }

  public String getMd5() {
    return md5;
  }

  public void setMd5(String md5) {
    this.md5 = md5;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  @Override
  public String toString() {
    return "InMemoryDocument { " +
      "uid='" + uid + '\'' +
      ", md5='" + md5 + '\'' +
      ", action=" + action +
      " }";
  }
}

package sapotero.rxtest.utils.memory.models;

import java.io.Serializable;

import sapotero.rxtest.db.requery.utils.Fields;
import sapotero.rxtest.utils.memory.fields.InMemoryState;

/**
 * In Memory Document Storage
 */



public class InMemoryDocument implements Serializable {
  public String uid;
  public String md5;
  public Fields.Status status;
  private InMemoryState action = InMemoryState.NEW;

  public InMemoryDocument() {
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

  public Fields.Status getStatus() {
    return status;
  }

  public void setStatus(Fields.Status status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "InMemoryDocument { " +
      "uid='" + uid + '\'' +
      ", md5='" + md5 + '\'' +
      ", status=" + status +
      ", action=" + action +
      " }";
  }
}

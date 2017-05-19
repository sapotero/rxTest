package sapotero.rxtest.utils.memory.models;

import sapotero.rxtest.utils.memory.fields.InMemoryStatus;

/**
 * In Memory Document Storage
 */



public class InMemoryDocument {
  public String uid;
  public String md5;
  public InMemoryStatus action = InMemoryStatus.NEW;

  public void setAsLoading(){
    action = InMemoryStatus.LOADING;
  }

  public void setAsDeleted(){
    action = InMemoryStatus.DELETE;
  }

  public void setAsNew(){
    action = InMemoryStatus.NEW;
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
    return "InMemoryDocument{" +
      "uid='" + uid + '\'' +
      ", md5='" + md5 + '\'' +
      ", action=" + action +
      '}';
  }
}

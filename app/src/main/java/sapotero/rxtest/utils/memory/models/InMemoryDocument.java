package sapotero.rxtest.utils.memory.models;

import java.io.Serializable;

import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.fields.InMemoryState;

public class InMemoryDocument implements Serializable {
  public String uid;
  public String md5;
  public String index;
  public String filter;
  public Integer year;
  public String updatedAt;
  public Boolean hasDecision = false;
  public Boolean processed   = false;
  public Boolean project     = false;

  public Document document;
  public InMemoryState state = InMemoryState.LOADING;

  public String user;

  public InMemoryDocument() {
  }

  public Boolean isProject() {
    return project;
  }

  public InMemoryDocument setProject(Boolean project) {
    this.project = project;
    return this;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
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

  public InMemoryState getState() {
    return state;
  }

  public void setAsLoading(){
    state = InMemoryState.LOADING;
  }

  public void setAsReady(){
    state = InMemoryState.READY;
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

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  @Override
  public String toString() {
    return "InMemoryDocument { " +
      "uid='" + uid + '\'' +
      ", md5='" + md5 + '\'' +
      ", index='" + index + '\'' +
      ", filter='" + filter + '\'' +
      ", year=" + year +
      ", hasDecision=" + hasDecision +
      ", processed=" + processed +
      ", document=" + document +
      ", state=" + state +
      "}";
  }
}

package sapotero.rxtest.utils.memory.models;

import java.io.Serializable;
import java.util.List;

import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.retrofit.models.document.DocumentInfoAction;
import sapotero.rxtest.retrofit.models.document.Image;
import sapotero.rxtest.retrofit.models.documents.Document;
import sapotero.rxtest.utils.memory.fields.InMemoryState;

public class InMemoryDocument implements Serializable {
  public String uid;
  public String md5;
  public String index;
  public String filter;
  public Integer year;
  public String updatedAt;
  public Integer createdAt = 0;
  public Boolean hasDecision = false;
  public Boolean processed   = false;
  public Boolean project     = false;

  public Document document;
  public List<Decision> decisions;
  public List<DocumentInfoAction> actions;
  public List<Image> images;
  public InMemoryState state = InMemoryState.LOADING;

  public String user;

  public boolean updatedFromDB = false;

  public InMemoryDocument() {
  }

  public Boolean isProject() {
    return project;
  }

  public InMemoryDocument setProject(Boolean project) {
    this.project = project;
    return this;
  }

  public Integer getCreatedAt() {
    return createdAt;
  }

  public InMemoryDocument setCreatedAt(Integer createdAt) {
    this.createdAt = createdAt;
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

  public List<Decision> getDecisions() {
    return decisions;
  }

  public void setDecisions(List<Decision> decisions) {
    this.decisions = decisions;
  }

  public List<DocumentInfoAction> getActions() {
    return actions;
  }

  public void setActions(List<DocumentInfoAction> actions) {
    this.actions = actions;
  }

  public boolean isUpdatedFromDB() {
    return updatedFromDB;
  }

  public void setUpdatedFromDB(boolean updatedFromDB) {
    this.updatedFromDB = updatedFromDB;
  }

  public List<Image> getImages() {
    return images;
  }

  public void setImages(List<Image> images) {
    this.images = images;
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

package sapotero.rxtest.managers.menu.utils;

import java.io.Serializable;
import java.util.UUID;

import javax.inject.Inject;

import sapotero.rxtest.application.EsdApplication;
import sapotero.rxtest.retrofit.models.document.Decision;
import sapotero.rxtest.utils.ISettings;

public class CommandParams implements Serializable {

  @Inject transient ISettings settings;

  private String host;
  private String login;
  private String token;
  private String currentUserId;
  private String pin;
  private String document;
  private String statusCode;
  private String person;
  private String decisionId;
  private Decision decisionModel;
  private String comment;
  private boolean assignment = false;
  private String folder;
  private String imageId;
  private String uuid;
  private String label;

  public CommandParams() {
    EsdApplication.getDataComponent().inject(this);

    setHost( settings.getHost() );
    setLogin( settings.getLogin() );
    setToken( settings.getToken() );
    setCurrentUserId( settings.getCurrentUserId() );
    setPin( settings.getPin() );
    setDocument( settings.getUid() );
    setStatusCode( settings.getStatusCode() );
    setUuid( UUID.randomUUID().toString() );
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getCurrentUserId() {
    return currentUserId;
  }

  public void setCurrentUserId(String currentUserId) {
    this.currentUserId = currentUserId;
  }

  public String getPin() {
    return pin;
  }

  public void setPin(String pin) {
    this.pin = pin;
  }

  public String getDocument() {
    return document;
  }

  public void setDocument(String document) {
    this.document = document;
  }

  public String getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(String statusCode) {
    this.statusCode = statusCode;
  }

  public String getPerson() {
    return person;
  }

  public void setPerson(String person) {
    this.person = person;
  }

  public String getDecisionId() {
    return decisionId;
  }

  public void setDecisionId(String decisionId) {
    this.decisionId = decisionId;
  }

  public void setDecisionModel(Decision decisionModel) {
    this.decisionModel = decisionModel;
  }

  public Decision getDecisionModel() {
    return decisionModel;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void setAssignment(boolean assignment) {
    this.assignment = assignment;
  }

  public boolean isAssignment() {
    return assignment;
  }

  public String getFolder() {
    return folder;
  }

  public void setFolder(String folder) {
    this.folder = folder;
  }

  public String getImageId() {
    return imageId;
  }

  public void setImageId(String imageId) {
    this.imageId = imageId;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getUuid() {
    return uuid;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
}

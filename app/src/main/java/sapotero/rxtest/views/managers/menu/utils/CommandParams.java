package sapotero.rxtest.views.managers.menu.utils;

import java.io.Serializable;
import java.util.UUID;

import sapotero.rxtest.db.requery.models.decisions.RDecisionEntity;
import sapotero.rxtest.retrofit.models.document.Decision;

public class CommandParams implements Serializable {
  public String person;
  public String folder;
  public String label;
  public String sign;
  public String document;
  public RDecisionEntity decision;
  public String decisionId;
  public String uuid;
  public String user;
  public String comment;
  public Decision decisionModel;
  public Boolean activeDecision;

  public Boolean getActiveDecision() {
    return activeDecision;
  }

  public void setActiveDecision(Boolean activeDecision) {
    this.activeDecision = activeDecision;
  }

  public CommandParams() {
    setUuid( UUID.randomUUID().toString() );
  }

  public String getDecisionId() {
    return decisionId;
  }

  public void setDecisionId(String decisionId) {
    this.decisionId = decisionId;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getUuid() {
    return uuid;
  }

  public RDecisionEntity getDecision() {
    return decision;
  }

  public void setDecision(RDecisionEntity decision) {
    this.decision = decision;
  }

  public String getPerson() {
    return person;
  }

  public CommandParams setPerson(String person) {
    this.person = person;
    return this;
  }

  public String getFolder() {
    return folder;
  }

  public CommandParams setFolder(String folder) {
    this.folder = folder;
    return this;
  }

  public String getLabel() {
    return label;
  }

  public CommandParams setLabel(String label) {
    this.label = label;
    return this;
  }

  public String getSign() {
    return sign;
  }

  public CommandParams setSign(String sign) {
    this.sign = sign;
    return this;
  }

  public String getDocument() {
    return document;
  }

  public void setDocument(String document) {
    this.document = document;
  }

  public void setDecisionModel(Decision decisionModel) {
    this.decisionModel = decisionModel;
  }

  public Decision getDecisionModel() {
    return decisionModel;
  }
}

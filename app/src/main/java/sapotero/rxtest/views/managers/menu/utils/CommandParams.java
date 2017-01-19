package sapotero.rxtest.views.managers.menu.utils;

import java.io.Serializable;
import java.util.UUID;

public class CommandParams implements Serializable {
  public String person;
  public String folder;
  public String label;
  public String sign;
  public String document;
  public String decision;
  public String user;
  public String uuid;

  public CommandParams() {
    setUuid( UUID.randomUUID().toString() );
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

  public String getDecision() {
    return decision;
  }

  public void setDecision(String decision) {
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
}

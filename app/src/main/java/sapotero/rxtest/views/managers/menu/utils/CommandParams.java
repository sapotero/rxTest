package sapotero.rxtest.views.managers.menu.utils;

public class CommandParams {
  public String person;
  public String folder;
  public String label;
  public String sign;

  public CommandParams() {
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
}

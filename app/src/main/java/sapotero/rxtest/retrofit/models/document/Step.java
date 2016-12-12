package sapotero.rxtest.retrofit.models.document;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Step {

  @SerializedName("number")
  @Expose
  private String number;
  @SerializedName("title")
  @Expose
  private String title;
  @SerializedName("people")
  @Expose
  private List<Person> people = null;
  @SerializedName("cards")
  @Expose
  private List<Card> cards = null;
  @SerializedName("another_approvals")
  @Expose
  private List<AnotherApproval> anotherApprovals = null;

  /**
   *
   * @return
   * The number
   */
  public String getNumber() {
    return number;
  }

  /**
   *
   * @param number
   * The number
   */
  public void setNumber(String number) {
    this.number = number;
  }

  /**
   *
   * @return
   * The title
   */
  public String getTitle() {
    return title;
  }

  /**
   *
   * @param title
   * The title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   *
   * @return
   * The people
   */
  public List<Person> getPeople() {
    return people;
  }

  /**
   *
   * @param people
   * The people
   */
  public void setPeople(List<Person> people) {
    this.people = people;
  }

  /**
   *
   * @return
   * The cards
   */
  public List<Card> getCards() {
    return cards;
  }

  /**
   *
   * @param cards
   * The cards
   */
  public void setCards(List<Card> cards) {
    this.cards = cards;
  }

  /**
   *
   * @return
   * The anotherApprovals
   */
  public List<AnotherApproval> getAnotherApprovals() {
    return anotherApprovals;
  }

  /**
   *
   * @param anotherApprovals
   * The another_approvals
   */
  public void setAnotherApprovals(List<AnotherApproval> anotherApprovals) {
    this.anotherApprovals = anotherApprovals;
  }

}
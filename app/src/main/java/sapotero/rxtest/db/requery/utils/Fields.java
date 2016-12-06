package sapotero.rxtest.db.requery.utils;

import java.util.Objects;

import timber.log.Timber;

public class Fields {

  public enum Status {
    SENT_TO_THE_REPORT      ("sent_to_the_report",      "Отправлен на исполнение"),
    SENT_TO_THE_PERFORMANCE ("sent_to_the_performance", "Отправлен на доклад"),
    PRIMARY_CONSIDERATION   ("primary_consideration",   "Первичное рассмотрение"),
    APPROVAL                ("approval",                "Согласование проектов документов"),
    SIGNING                 ("signing",                 "Подписание проектов документов"),
    PROCESSED               ("processed",               "Рассмотренные");

    private final String value;
    private final String name;

    Status(final String value, final String name) {
      this.value = value;
      this.name = name;
    }

    public String getValue(){
      return value;
    }
    public String getName(){
      return name;
    }

    @Override
    public String toString() {
      return value;
    }

    public static Status[] INDEX = new Status[] { SENT_TO_THE_REPORT, SENT_TO_THE_PERFORMANCE, PRIMARY_CONSIDERATION, APPROVAL, SIGNING, PROCESSED };
  }

  public enum Journal {
    ALL_JOURNALS              (  0, "",   "Все документы"),
    CITIZEN_REQUESTS          (  1, "01", "Обращения граждан"),
    INCOMING_DOCUMENTS        (  2, "02", "Входящие документы"),
    INCOMING_ORDERS           (  3, "03", "НПА"),
    ORDERS                    (  4, "04", "Приказы старые"),
    OUTGOING_DOCUMENTS        (  5, "05", "Исходящие документы"),
    SECRET_INCOMING_DOCUMENTS (  6, "06", "Входящие секретные"),
    SECRET_INCOMING_ORDERS    (  7, "07", "НПА секретные"),
    SECRET_ORDERS             (  8, "08", "Пр. старые секретные"),
    SECRET_OUTGOING_DOCUMENTS (  9, "09", "Исходящие секретные"),
    ORDERS_DDO                ( 10, "10", "Приказы"),
    CONTROL                   ( 97, "97", "На контроле"),
    SIGN                      ( 98, "98", "Подписание"),
    APPROVE                   ( 99, "99", "Согласование");

    private final String value;
    private final String name;
    private final Integer type;

    Journal(final int index, final String text, final String name) {
      this.type = index;
      this.value = text;
      this.name = name;
    }

    public Integer getType(){
      return type;
    }
    public String getValue(){
      return value;
    }
    public String getName(){
      return name;
    }

    @Override
    public String toString() {
      return value;
    }

    public static Journal[] INDEX = new Journal[] {  ALL_JOURNALS, CITIZEN_REQUESTS, INCOMING_DOCUMENTS, INCOMING_ORDERS, ORDERS, OUTGOING_DOCUMENTS, SECRET_INCOMING_DOCUMENTS, SECRET_INCOMING_ORDERS, SECRET_ORDERS, SECRET_OUTGOING_DOCUMENTS, ORDERS_DDO, CONTROL, SIGN, APPROVE };

  }

  public static Status getStatus(String type ){
    Status status = null;

    for ( Status value: Status.values()){
      if ( Objects.equals(type, value.toString()) ){
        status = value;
        break;
      }
    }

    return status;
  }

  public static Journal getJournal(String type ){
    Journal journal = null;

    for ( Journal value: Journal.values()){
      if ( Objects.equals(type, value.toString()) ){
        journal = value;
        break;
      }
    }

    return journal;
  }

  public static Journal getJournalByUid(String uid ){
    Journal result = null;

    for ( Journal journal: Journal.values()){
      if (journal.getValue().startsWith( uid.substring(0, 2))) {
        Timber.tag("JOURNAL_TYPE").d("%s == %s [%s]", journal.getValue(), uid.substring(0, 1), uid);
        result = journal;
        break;
      }
    }

    return result;
  }
}

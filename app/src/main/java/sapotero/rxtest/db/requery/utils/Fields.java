package sapotero.rxtest.db.requery.utils;

import java.util.Objects;

import timber.log.Timber;

public class Fields {

  public enum Status {
    SENT_TO_THE_REPORT      ("sent_to_the_report",      "Отправлен на исполнение"),
//    SENT_TO_THE_PERFORMANCE ("sent_to_the_performance", "Отправлен на доклад"),
    PRIMARY_CONSIDERATION   ("primary_consideration",   "Первичное рассмотрение"),
    APPROVAL                ("approval",                "Согласование проектов документов"),
    SIGNING                 ("signing",                 "Подписание проектов документов"),
    LINK                    ("link",                    "Связанный документ"),
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

    public static Status[] INDEX = new Status[] { SENT_TO_THE_REPORT, PRIMARY_CONSIDERATION, APPROVAL, SIGNING };

    public static Status findStatus( String string){
      Status status = null;

      for ( Status item: Status.values()  ){
        if ( Objects.equals(item.getValue(), string) ){
          status = item;
          break;
        }
      }

      return status;
    }

    public static Status getRandom() {
      return values()[(int) (Math.random() * values().length)];
    }
  }

  public enum Journal {
    ALL_JOURNALS              (  0, "90",   "Все документы",        " "),
    CITIZEN_REQUESTS          (  1, "01", "Обращения граждан",    "Обращение граждан"),
    INCOMING_DOCUMENTS        (  2, "02", "Входящие документы",   "Входящий документ"),
    INCOMING_ORDERS           (  3, "03", "НПА",                  "НПА"),
    ORDERS                    (  4, "04", "Приказы старые",       "Приказ старый"),
    OUTGOING_DOCUMENTS        (  5, "05", "Исходящие документы",  "Исходящий документ"),
    SECRET_INCOMING_DOCUMENTS (  6, "06", "Входящие секретные",   "Входящий секретный"),
    SECRET_INCOMING_ORDERS    (  7, "07", "НПА секретные",        "НПА секретный"),
    SECRET_ORDERS             (  8, "08", "Пр. старые секретные", "Пр. старый секретный"),
    SECRET_OUTGOING_DOCUMENTS (  9, "09", "Исходящие секретные",  "Исходящий секретный"),
    ORDERS_DDO                ( 10, "10", "Приказы",              "Приказ"),
    CONTROL                   ( 97, "97", "На контроле",          " "),
    SIGN                      ( 98, "98", "Подписание",           "Подписание"),
    APPROVE                   ( 99, "99", "Согласование",         "Согласование");

    private final String value;
    private final String name;
    private final String single;
    private final Integer type;

    Journal(final int index, final String value, final String name, final String single) {
      this.type  = index;
      this.value = value;
      this.name  = name;
      this.single  = single;
    }

    public Integer getType(){
      return type;
    }
    public String getValue(){
      return value;
    }
    public String getName() {
      return name;
    }
    public String getSingle() {
      return single;
    }

    @Override
    public String toString() {
      return value;
    }

    public static Journal[] INDEX = new Journal[] {  ALL_JOURNALS, CITIZEN_REQUESTS, INCOMING_DOCUMENTS, INCOMING_ORDERS, ORDERS, OUTGOING_DOCUMENTS, SECRET_INCOMING_DOCUMENTS, SECRET_INCOMING_ORDERS, SECRET_ORDERS, SECRET_OUTGOING_DOCUMENTS, ORDERS_DDO, CONTROL, SIGN, APPROVE };

  }

  public static String getJournalName( String raw ){
    String journal = "";

    switch (raw){
      case "incoming_documents":
        journal = "Входящий документ";
        break;

      case "outgoing_documents":
        journal = "Исходящий документ";
        break;

      case "orders":
        journal = "Приказ";
        break;

      case "incoming_orders":
        journal = "НПА";
        break;

      case "citizen_requests":
        journal = "Обращение граждан";
        break;

      case "secret_incoming_documents":
        journal = "Входящие секретное";
        break;

      case "secret_outgoing_documents":
        journal = "Исходящие секретное";
        break;

      case "secret_orders":
        journal = "Секретный приказ";
        break;

      case "secret_incoming_orders":
        journal = "Секретный НПА";
        break;

      case "orders_ddo":
        journal = "Приказ ДДО";
        break;

      case "attorney_letters":
        journal = "Доверенность";
        break;

    }

    return journal;
  }


  public enum Menu {
    ALL                ( 0  , "ALL"                , "Документы / Проекты" ),
    INCOMING_DOCUMENTS ( 1  , "INCOMING_DOCUMENTS" , "Входящие документы" ),
    CITIZEN_REQUESTS   ( 2  , "CITIZEN_REQUESTS"   , "Обращения граждан" ),
    APPROVE_ASSIGN     ( 3  , "APPROVE_ASSIGN"     , "Подписание/Согласование" ),
    INCOMING_ORDERS    ( 4  , "INCOMING_ORDERS"    , "НПА" ),
    ORDERS             ( 5  , "ORDERS"             , "Приказы" ),
    ORDERS_DDO         ( 6  , "ORDERS_DDO"         , "Приказы ДДО" ),
    IN_DOCUMENTS       ( 7  , "IN_DOCUMENTS"       , "Внутренние документы" ),
    ON_CONTROL         ( 8  , "ON_CONTROL"         , "На контроле" ),
    PROCESSED          ( 9  , "PROCESSED"          , "Обработанное" ),
    FAVORITES          ( 10 , "FAVORITES"          , "Избранное" );

    private final Integer index;
    private final String value;
    private final String title;

    Menu(final int index, final String value, final String title) {
      this.index  = index;
      this.value = value;
      this.title  = title;
    }

    public Integer getIndex() {
      return index;
    }

    public String getValue() {
      return value;
    }

    public String getTitle() {
      return title;
    }

    @Override
    public String toString() {
      return value;
    }

  }

  public static Status  getStatus(String type ){
    Status status = null;


    for ( Status value: Status.values()){
      if ( Objects.equals(type, value.getValue()) ){
        status = value;
        break;
      }
    }

    Timber.e("getStatus: %s | %s", type, status );

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


    if (uid != null) {

      if (uid.startsWith("p")){
        uid = uid.substring(1, uid.length()-1 );
      }

      for ( Journal journal: Journal.values()){
        if (journal.getValue().startsWith( uid.substring(0, 2))) {
          Timber.tag("JOURNAL_TYPE").d("%s == %s [%s]", journal.getValue(), uid.substring(0, 1), uid);
          result = journal;
          break;
        }
      }
    }

    return result;
  }
}

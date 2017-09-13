package sapotero.rxtest.db.requery.utils;

import java.util.Objects;

import timber.log.Timber;

public class Fields {

  public enum Status {
    SENT_TO_THE_REPORT      (V2FilterType.FOR_REPORT.getName(), "Отправлен на исполнение"),
//    SENT_TO_THE_PERFORMANCE ("sent_to_the_performance", "Отправлен на доклад"),
    PRIMARY_CONSIDERATION   (V2FilterType.PRIMARY.getName(),    "Первичное рассмотрение"),
    APPROVAL                (V2FilterType.APPROVAL.getName(),   "Согласование проектов документов"),
    SIGNING                 (V2FilterType.SIGNING.getName(),    "Подписание проектов документов"),
    LINK                    (V2FilterType.LINK.getName(),       "Связанный документ"),
    PROJECTS                (V2FilterType.PROJECTS.getName(),   "Проекты"),
    PROCESSED               (V2FilterType.PROCESSED.getName(),  "Рассмотренные");

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
    ALL_JOURNALS              (  0, "90", "Все документы",        " ", ""),
    CITIZEN_REQUESTS          (  1, "01", "Обращения граждан",    "Обращение граждан",     "Вам поступило "),
    INCOMING_DOCUMENTS        (  2, "02", "Входящие документы",   "Входящий документ",     "Вам поступил "),
    INCOMING_ORDERS           (  3, "03", "НПА",                  "НПА",                   "Вам поступил "),
    ORDERS                    (  4, "04", "Приказы старые",       "Приказ старый",         "Вам поступил "),
    OUTGOING_DOCUMENTS        (  5, "05", "Исходящие документы",  "Исходящий документ",    "Вам поступил "),
    SECRET_INCOMING_DOCUMENTS (  6, "06", "Входящие секретные",   "Входящий секретный",    "Вам поступил "),
    SECRET_INCOMING_ORDERS    (  7, "07", "НПА секретные",        "НПА секретный",         "Вам поступил "),
    SECRET_ORDERS             (  8, "08", "Пр. старые секретные", "Пр. старый секретный",  "Вам поступил "),
    SECRET_OUTGOING_DOCUMENTS (  9, "09", "Исходящие секретные",  "Исходящий секретный",   "Вам поступил "),
    ORDERS_DDO                ( 10, "10", "Приказы",              "Приказ",                "Вам поступил "),
    CONTROL                   ( 97, "97", "На контроле",          " ",                     "Вам поступил "),
    SIGN                      ( 98, "98", "Подписание",           "Подписание",            "Вам поступил документ на "),
    APPROVE                   ( 99, "99", "Согласование",         "Согласование",          "Вам поступил документ на ");

    private final String value;
    private final String name;
    private final String single;
    private final Integer type;
    private final String formattedName;

    Journal(final int index, final String value, final String name, final String single, final  String formattedName) {
      this.type  = index;
      this.value = value;
      this.name  = name;
      this.single  = single;
      this.formattedName = formattedName;
    }

    public String getFormattedName() {
      return formattedName;
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

    public static Menu getMenu( String string){
      Menu menu = null;

      for ( Menu item: Menu.values()  ){
        if ( Objects.equals( item.getIndex() , Integer.valueOf(string)) ){
          menu = item;
          break;
        }
      }

      return menu;
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

package sapotero.rxtest.db.requery.utils;

import java.util.Objects;

public enum V2DocumentType {
  ALL                ("0", "Документы / Проекты",     "",                     "",                                                                     "",                   ""),
  INCOMING_DOCUMENTS ("1", "Входящие документы",      "incoming_documents",   "incoming_documents_production_db_core_cards_incoming_documents_cards", "Входящий документ",  "Вам поступил "),
  CITIZEN_REQUESTS   ("2", "Обращения граждан",       "citizen_requests",     "citizen_requests_production_db_core_cards_citizen_requests_cards",     "Обращение граждан",  "Вам поступило "),
  APPROVE_ASSIGN     ("3", "Подписание/Согласование", "",                     "",                                                                     "",                   ""),
  INCOMING_ORDERS    ("4", "НПА",                     "incoming_orders",      "incoming_orders_production_db_core_cards_incoming_orders_cards",       "НПА",                "Вам поступил "),
  ORDERS             ("5", "Приказы",                 "orders",               "orders_production_db_core_cards_orders_cards",                         "Приказ",             "Вам поступил "),
  ORDERS_DDO         ("6", "Приказы ДДО",             "orders_ddo",           "orders_ddo_production_db_core_cards_orders_ddo_cards",                 "Приказ ДДО",         "Вам поступил "),
  OUTGOING_DOCUMENTS ("7", "Внутренние документы",    "outgoing_documents",   "outgoing_documents_production_db_core_cards_outgoing_documents_cards", "Исходящий документ", "Вам поступил "),
  ON_CONTROL         ("8", "На контроле",             "",                     "",                                                                     "",                   ""),
  PROCESSED          ("9", "Обработанное",            "",                     "",                                                                     "",                   ""),
  FAVORITES          ("10", "Избранное",              "",                     "",                                                                     "",                   ""),
  SIGNING            ("98", "",                       "",                     "",                                                                     "Подписание",         "Вам поступил документ на "),
  APPROVAL           ("99", "",                       "",                     "",                                                                     "Согласование",       "Вам поступил документ на ");

  private final String index;
  private final String journal;
  private final String name;
  private final String nameForApi;
  private final String single;
  private final String formattedName;

  V2DocumentType(String index, String journal, String name, String nameForApi, String single, String formattedName) {
    this.index = index;
    this.journal = journal;
    this.name = name;
    this.nameForApi = nameForApi;
    this.single = single;
    this.formattedName = formattedName;
  }

  public String getIndex() {
    return index;
  }

  public int getIntIndex() {
    return Integer.valueOf( getIndex() );
  }

  public String getJournal() {
    return journal;
  }

  public String getName() {
    return name;
  }

  public String getNameForApi() {
    return nameForApi;
  }

  public String getSingle() {
    return single;
  }

  public String getFormattedName() {
    return formattedName;
  }

  public static V2DocumentType getDocumentTypeByName(String typeName) {
    V2DocumentType type = null;

    for ( V2DocumentType item : V2DocumentType.values() ) {
      if ( Objects.equals( item.getName(), typeName ) ) {
        type = item;
        break;
      }
    }

    return type;
  }

  public static String getDocumentName(String typeName) {
    String documentName = "";

    V2DocumentType type = getDocumentTypeByName( typeName );

    if ( type != null ) {
      documentName = type.getSingle();
    }

    return documentName;
  }

  public static V2DocumentType getDocumentTypeByIndex(String index) {
    V2DocumentType type = null;

    for ( V2DocumentType item : V2DocumentType.values() ) {
      if ( Objects.equals( item.getIndex(), index ) ) {
        type = item;
        break;
      }
    }

    return type;
  }
}

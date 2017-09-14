package sapotero.rxtest.db.requery.utils;

import java.util.Objects;

public enum V2DocumentType {
  ALL                (Journals.ALL,                "Документы / Проекты",     "",                      "",                                                                     "",                   ""),
  INCOMING_DOCUMENTS (Journals.INCOMING_DOCUMENTS, "Входящие документы",      "incoming_documents",    "incoming_documents_production_db_core_cards_incoming_documents_cards", "Входящий документ",  "Вам поступил "),
  CITIZEN_REQUESTS   (Journals.CITIZEN_REQUESTS,   "Обращения граждан",       "citizen_requests",      "citizen_requests_production_db_core_cards_citizen_requests_cards",     "Обращение граждан",  "Вам поступило "),
  APPROVE_ASSIGN     (Journals.APPROVE_ASSIGN,     "Подписание/Согласование", "",                      "",                                                                     "",                   ""),
  INCOMING_ORDERS    (Journals.INCOMING_ORDERS,    "НПА",                     "incoming_orders",       "incoming_orders_production_db_core_cards_incoming_orders_cards",       "НПА",                "Вам поступил "),
  ORDERS             (Journals.ORDERS,             "Приказы",                 "orders",                "orders_production_db_core_cards_orders_cards",                         "Приказ",             "Вам поступил "),
  ORDERS_DDO         (Journals.ORDERS_DDO,         "Приказы ДДО",             "orders_ddo",            "orders_ddo_production_db_core_cards_orders_ddo_cards",                 "Приказ ДДО",         "Вам поступил "),
  OUTGOING_DOCUMENTS (Journals.IN_DOCUMENTS,       "Внутренние документы",    "outgoing_documents",    "outgoing_documents_production_db_core_cards_outgoing_documents_cards", "Исходящий документ", "Вам поступил "),
  ON_CONTROL         (Journals.ON_CONTROL,         "На контроле",             "",                      "",                                                                     "",                   ""),
  PROCESSED          (Journals.PROCESSED,          "Обработанное",            "",                      "",                                                                     "",                   ""),
  FAVORITES          (Journals.FAVORITES,          "Избранное",               "",                      "",                                                                     "",                   ""),
  SIGNING            (98,                          "",                        "signing",               "signing",                                                              "Подписание",         "Вам поступил документ на "),
  APPROVAL           (99,                          "",                        "approval",              "approval",                                                             "Согласование",       "Вам поступил документ на "),
  FOR_REPORT         (97,                          "",                        "sent_to_the_report",    "sent_to_the_report",                                                   "",                   ""),
  PRIMARY            (96,                          "",                        "primary_consideration", "primary_consideration",                                                "",                   ""),
  LINK               (95,                          "",                        "link",                  "link",                                                                 "",                   "");

  private final int index;
  private final String journal;
  private final String name;
  private final String nameForApi;
  private final String single;
  private final String formattedName;

  V2DocumentType(int index, String journal, String name, String nameForApi, String single, String formattedName) {
    this.index = index;
    this.journal = journal;
    this.name = name;
    this.nameForApi = nameForApi;
    this.single = single;
    this.formattedName = formattedName;
  }

  public int getIndex() {
    return index;
  }

  public String getStringIndex() {
    return String.valueOf( getIndex() );
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
      if ( Objects.equals( item.getStringIndex(), index ) ) {
        type = item;
        break;
      }
    }

    return type;
  }
}

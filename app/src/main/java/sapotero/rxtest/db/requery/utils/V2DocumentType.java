package sapotero.rxtest.db.requery.utils;

import java.util.Objects;

public enum V2DocumentType {
  ALL                ("90", "",                     "",                                                                     "",                   ""),
  INCOMING_DOCUMENTS ("02", "incoming_documents",   "incoming_documents_production_db_core_cards_incoming_documents_cards", "Входящий документ",  "Вам поступил "),
  CITIZEN_REQUESTS   ("01", "citizen_requests",     "citizen_requests_production_db_core_cards_citizen_requests_cards",     "Обращение граждан",  "Вам поступило "),
  INCOMING_ORDERS    ("03", "incoming_orders",      "incoming_orders_production_db_core_cards_incoming_orders_cards",       "НПА",                "Вам поступил "),
  ORDERS             ("04", "orders",               "orders_production_db_core_cards_orders_cards",                         "Приказ",             "Вам поступил "),
  ORDERS_DDO         ("10", "orders_ddo",           "orders_ddo_production_db_core_cards_orders_ddo_cards",                 "Приказ ДДО",         "Вам поступил "),
  OUTGOING_DOCUMENTS ("05", "outgoing_documents",   "outgoing_documents_production_db_core_cards_outgoing_documents_cards", "Исходящий документ", "Вам поступил "),
  SIGNING            ("98", "",                     "",                                                                     "Подписание",         "Вам поступил документ на "),
  APPROVAL           ("99", "",                     "",                                                                     "Согласование",       "Вам поступил документ на ");

  private final String value;
  private final String name;
  private final String nameForApi;
  private final String single;
  private final String formattedName;

  V2DocumentType(String value, String name, String nameForApi, String single, String formattedName) {
    this.value = value;
    this.name = name;
    this.nameForApi = nameForApi;
    this.single = single;
    this.formattedName = formattedName;
  }

  public String getValue() {
    return value;
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

  public static V2DocumentType findDocumentType(String typeName ) {
    V2DocumentType type = null;

    for ( V2DocumentType item : V2DocumentType.values() ) {
      if ( Objects.equals( item.getName(), typeName ) ) {
        type = item;
        break;
      }
    }

    return type;
  }

  public static String getDocumentName( String typeName ) {
    String documentName = "";

    V2DocumentType type = findDocumentType( typeName );

    if ( type != null ) {
      documentName = type.getSingle();
    }

    return documentName;
  }
}

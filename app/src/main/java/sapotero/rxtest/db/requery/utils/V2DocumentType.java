package sapotero.rxtest.db.requery.utils;

import java.util.Objects;

public enum V2DocumentType {
  INCOMING_DOCUMENTS ("incoming_documents",   "incoming_documents_production_db_core_cards_incoming_documents_cards", "Входящий документ"),
  CITIZEN_REQUESTS   ("citizen_requests",     "citizen_requests_production_db_core_cards_citizen_requests_cards",     "Обращение граждан"),
  INCOMING_ORDERS    ("incoming_orders",      "incoming_orders_production_db_core_cards_incoming_orders_cards",       "НПА"),
  ORDERS             ("orders",               "orders_production_db_core_cards_orders_cards",                         "Приказ"),
  ORDERS_DDO         ("orders_ddo",           "orders_ddo_production_db_core_cards_orders_ddo_cards",                 "Приказ ДДО"),
  OUTGOING_DOCUMENTS ("outgoing_documents",   "outgoing_documents_production_db_core_cards_outgoing_documents_cards", "Исходящий документ");

  private final String name;
  private final String nameForApi;
  private final String documentName;

  V2DocumentType(String name, String nameForApi, String documentName) {
    this.name = name;
    this.nameForApi = nameForApi;
    this.documentName = documentName;
  }

  public String getName() {
    return name;
  }

  public String getNameForApi() {
    return nameForApi;
  }

  public String getDocumentName() {
    return documentName;
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
      documentName = type.getDocumentName();
    }

    return documentName;
  }
}

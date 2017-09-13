package sapotero.rxtest.db.requery.utils;

public enum V2DocumentType {
  INCOMING_DOCUMENTS ("incoming_documents",   "incoming_documents_production_db_core_cards_incoming_documents_cards"),
  CITIZEN_REQUESTS   ("citizen_requests",     "citizen_requests_production_db_core_cards_citizen_requests_cards"),
  INCOMING_ORDERS    ("incoming_orders",      "incoming_orders_production_db_core_cards_incoming_orders_cards"),
  ORDERS             ("orders",               "orders_production_db_core_cards_orders_cards"),
  ORDERS_DDO         ("orders_ddo",           "orders_ddo_production_db_core_cards_orders_ddo_cards"),
  OUTGOING_DOCUMENTS ("outgoing_documents",   "outgoing_documents_production_db_core_cards_outgoing_documents_cards");

  private final String name;
  private final String nameForApi;

  V2DocumentType(String name, String nameForApi) {
    this.name = name;
    this.nameForApi = nameForApi;
  }

  public String getName() {
    return name;
  }

  public String getNameForApi() {
    return nameForApi;
  }
}

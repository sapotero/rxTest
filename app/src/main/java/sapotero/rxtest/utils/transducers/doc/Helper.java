package sapotero.rxtest.utils.transducers.doc;


import java.util.ArrayList;
import java.util.Arrays;

import sapotero.rxtest.db.requery.utils.JournalStatus;

public class Helper {

  private static ArrayList<String> order = new ArrayList<>(
    Arrays.asList(
      JournalStatus.INCOMING_DOCUMENTS.getName().toLowerCase(),
      JournalStatus.CITIZEN_REQUESTS.getName(),
      JournalStatus.INCOMING_ORDERS.getName(),
      JournalStatus.ORDERS.getName(),
      JournalStatus.ORDERS_DDO.getName(),
      JournalStatus.OUTGOING_DOCUMENTS.getName()
    ));

  public static Integer getJournal(String journal){
    int result = order.indexOf(journal.toLowerCase());
    return result == -1 ? 6 : result;
  }
}

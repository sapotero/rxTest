package sapotero.rxtest.utils.transducers.doc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import sapotero.rxtest.db.requery.utils.JournalStatus;
import sapotero.rxtest.utils.transducers.Reducable;
import sapotero.rxtest.utils.transducers.Reducer;

public class HashReader implements Reducable<String> {

  private final HashMap<String, List<String>>  docs;
  private final List<String> order;

  HashReader(HashMap<String, List<String>> docs) {
    this.docs = docs;

    this.order = new ArrayList<>();
    order.add( JournalStatus.INCOMING_DOCUMENTS.getName() );
    order.add( JournalStatus.CITIZEN_REQUESTS.getName() );
    order.add( JournalStatus.INCOMING_ORDERS.getName() );
    order.add( JournalStatus.ORDERS.getName() );
    order.add( JournalStatus.ORDERS_DDO.getName() );
    order.add( JournalStatus.OUTGOING_DOCUMENTS.getName() );
  }

  public <R> R reduce(Reducer<String, R> reducer) {
    R acc = reducer.init();

    Iterable<String> array = () -> new Iterator<String>() {
      private int pos=0;

      public boolean hasNext() {
        return docs.size() > pos;
      }

      public String next() {
        return "";
//        return docs.get(pos++);
      }
    };

    Iterator<String> iterator = array.iterator();

    while ( iterator.hasNext() ) {
      acc = reducer.step(acc, iterator.next());
    }
    return acc;
  }
}
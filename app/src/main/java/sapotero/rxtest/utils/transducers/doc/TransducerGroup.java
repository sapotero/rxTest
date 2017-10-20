package sapotero.rxtest.utils.transducers.doc;

import android.util.Pair;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.transducers.Function;
import sapotero.rxtest.utils.transducers.Reducer;

public class TransducerGroup implements Serializable {

  public static HashMap<String, List<String>> group(List<InMemoryDocument> docs) {

    Function<InMemoryDocument, Pair<String, InMemoryDocument>> decode
      = inMemoryDocument -> new Pair<>(inMemoryDocument.getIndex(), inMemoryDocument);

    Reducer<Pair<String, InMemoryDocument>, HashMap<String, List<InMemoryDocument>>> sort = new Sort();

    Reducer<InMemoryDocument, HashMap<String, List<String>>> groupBy = new Mapper(decode).transduce(sort);

    HashMap<String, List<String>> intermediate = new DocsReader(docs).reduce(groupBy);

//    Reducer<String, List<String>> order = new OrderHash();
//    HashMap<String, List<String>> result = new HashReader(intermediate).reduce(order);

    return intermediate;
  }
}
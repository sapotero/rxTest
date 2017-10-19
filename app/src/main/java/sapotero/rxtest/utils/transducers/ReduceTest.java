package sapotero.rxtest.utils.transducers;

import android.util.Pair;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.transducers.doc.SortUid;

public class ReduceTest implements Serializable {

  public static HashMap<String, List<String>> group(List<InMemoryDocument> docs) {

    Function<InMemoryDocument, Pair<String, InMemoryDocument>> decode =
      inMemoryDocument -> new Pair<>(inMemoryDocument.getIndex(), inMemoryDocument);

    Reducer<Pair<String, InMemoryDocument>, HashMap<String, List<InMemoryDocument>>> sortUid = new SortUid();

    Reducer<InMemoryDocument, HashMap<String, List<String>>> groupBy = new Mapping(decode).transduce(sortUid);

    return new DocsReader(docs).reduce(groupBy);
  }
}
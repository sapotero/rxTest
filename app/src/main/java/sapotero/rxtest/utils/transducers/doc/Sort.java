package sapotero.rxtest.utils.transducers.doc;

import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.transducers.Reducer;

public class Sort implements Reducer<Pair<String, InMemoryDocument>, HashMap<String, List<InMemoryDocument>>> {

  public HashMap<String, List<InMemoryDocument>> init() {
    return new HashMap<>();
  }

  public HashMap<String, List<InMemoryDocument>> step ( HashMap<String, List<InMemoryDocument>> acc, Pair<String, InMemoryDocument> item ) {

    if ( !acc.containsKey(item.first) ){
      acc.put(item.first, new ArrayList<>());
    }
    acc.get(item.first).add(item.second);

    return acc;
  }
}
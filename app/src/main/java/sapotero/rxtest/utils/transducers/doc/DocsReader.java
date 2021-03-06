package sapotero.rxtest.utils.transducers.doc;

import java.util.Iterator;
import java.util.List;

import sapotero.rxtest.utils.memory.models.InMemoryDocument;
import sapotero.rxtest.utils.transducers.Reducable;
import sapotero.rxtest.utils.transducers.Reducer;

public class DocsReader implements Reducable<InMemoryDocument> {

  private final List<InMemoryDocument>  docs;

  public DocsReader(List<InMemoryDocument> docs) {
    this.docs = docs;
  }

  public <R> R reduce(Reducer<InMemoryDocument, R> reducer) {
    R acc = reducer.init();

    Iterable<InMemoryDocument> array = () -> new Iterator<InMemoryDocument>() {
      private int pos=0;

      public boolean hasNext() {
        return docs.size() > pos;
      }

      public InMemoryDocument next() {
        return docs.get(pos++);
      }
    };

    Iterator<InMemoryDocument> iterator = array.iterator();

    while ( iterator.hasNext() ) {
      acc = reducer.step(acc, iterator.next());
    }
    return acc;
  }
}
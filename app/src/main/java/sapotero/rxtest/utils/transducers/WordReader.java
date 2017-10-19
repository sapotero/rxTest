package sapotero.rxtest.utils.transducers;

import java.util.Iterator;

class WordReader implements Reducable<String> {

  private final String[] words;

  WordReader(String[] words) {
    this.words = words;
  }

  public <R> R reduce(Reducer<String,R> reducer) {
    R acc = reducer.init();

    Iterable<String> array = () -> new Iterator<String>() {
      private int pos=0;

      public boolean hasNext() {
        return words.length>pos;
      }

      public String next() {
        return words[pos++];
      }

      public void remove() {
        throw new UnsupportedOperationException("Cannot remove an element of an array.");
      }
    };

    Iterator<String> iterator = array.iterator();

    while ( iterator.hasNext() ) {
      acc = reducer.step(acc, iterator.next());
    }
    return acc;
  }
}
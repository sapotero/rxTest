package sapotero.rxtest.utils.transducers;

public interface Transducer<A,B> {
  <R> Reducer<B,R> transduce(Reducer<A,R> reducer);
}